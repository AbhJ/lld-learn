/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/ConcurrentLRUCache.java — ReentrantReadWriteLock based concurrent LRU cache

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.atomic.AtomicLong;

class ConcurrentLRUCache<V> {
    private final int capacity;                  // final = immutable config; safe for all threads
    private final long ttlMillis;                // final = set once; no synchronization needed to read
    private final LinkedHashMap<String, CacheEntry<V>> cache; // LinkedHashMap = accessOrder LRU; guarded by rwLock
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(); // RWLock = many readers OR one writer; better than synchronized
    private final AtomicLong hits = new AtomicLong(0);  // AtomicLong = thread-safe counter without locks
    private final AtomicLong misses = new AtomicLong(0); // AtomicLong = concurrent miss tracking

    public ConcurrentLRUCache(int capacity, long ttlMillis) {
        this.capacity = capacity;
        this.ttlMillis = ttlMillis;
        this.cache = new LinkedHashMap<String, CacheEntry<V>>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, CacheEntry<V>> eldest) {
                return size() > ConcurrentLRUCache.this.capacity;
            }
        };
    }

    public V get(String key) {
        rwLock.readLock().lock();
        try {
            CacheEntry<V> entry = cache.get(key);
            if (entry == null) {
                misses.incrementAndGet();
                return null;
            }
            if (entry.isExpired()) {
                misses.incrementAndGet();
                return null; // Will be cleaned up on next write
            }
            entry.touch();
            hits.incrementAndGet();
            return entry.getValue();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public void put(String key, V value) {
        rwLock.writeLock().lock();
        try {
            CacheEntry<V> existing = cache.get(key);
            if (existing != null) {
                existing.setValue(value);
            } else {
                cache.put(key, new CacheEntry<>(key, value, ttlMillis));
            }
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public void remove(String key) {
        rwLock.writeLock().lock();
        try {
            cache.remove(key);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public int size() {
        rwLock.readLock().lock();
        try {
            return cache.size();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public long getHits() { return hits.get(); }
    public long getMisses() { return misses.get(); }

    public void evictExpired() {
        rwLock.writeLock().lock();
        try {
            cache.entrySet().removeIf(e -> e.getValue().isExpired());
        } finally {
            rwLock.writeLock().unlock();
        }
    }
}

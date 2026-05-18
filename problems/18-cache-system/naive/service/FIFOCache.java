/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/FIFOCache.java — First-In-First-Out eviction using insertion order queue

import java.util.*;

public class FIFOCache<V> implements Cache<V> {  // implements = fulfills the Cache interface contract
    private int capacity;                        // private = internal limit hidden from callers
    private Map<String, CacheEntry<V>> cache;
    private Queue<String> insertionOrder;        // Queue tracks insertion order for FIFO eviction
    private CacheStats stats;

    public FIFOCache(int capacity) {
        this.capacity = capacity;
        this.cache = new HashMap<>();
        this.insertionOrder = new LinkedList<>();
        this.stats = new CacheStats();
    }

    @Override
    public void put(String key, V value) {
        stats.recordPut();

        if (cache.containsKey(key)) {
            cache.get(key).setValue(value);
            return;
        }

        if (cache.size() >= capacity) {
            evict();
        }

        CacheEntry<V> entry = new CacheEntry<>(key, value);
        cache.put(key, entry);
        insertionOrder.offer(key);
    }

    @Override
    public V get(String key) {
        CacheEntry<V> entry = cache.get(key);
        if (entry == null) {
            stats.recordMiss();
            return null;
        }
        stats.recordHit();
        entry.access();
        return entry.getValue();
    }

    private void evict() {
        String evictKey = insertionOrder.poll();
        if (evictKey != null) {
            cache.remove(evictKey);
            stats.recordEviction();
        }
    }

    @Override
    public boolean containsKey(String key) { return cache.containsKey(key); }

    @Override
    public void remove(String key) {
        cache.remove(key);
        insertionOrder.remove(key);
    }

    @Override
    public int size() { return cache.size(); }

    @Override
    public int capacity() { return capacity; }

    @Override
    public void clear() {
        cache.clear();
        insertionOrder.clear();
    }

    @Override
    public CacheStats getStats() { return stats; }

    @Override
    public String getPolicyName() { return "FIFO"; }
}

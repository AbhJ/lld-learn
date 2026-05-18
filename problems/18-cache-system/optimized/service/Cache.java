/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/Cache.java — Generic Cache<K,V> with pluggable eviction strategy

import java.util.*;

/**
 * Optimized: Generic Cache<K,V> that accepts any eviction strategy.
 * Decouples cache storage from eviction policy via strategy pattern.
 */
public class Cache<K, V> {
    private int capacity;
    private Map<K, V> store;                     // HashMap = O(1) get/put; fast key-value storage
    private EvictionStrategy<K> evictionStrategy; // strategy pattern = pluggable eviction algorithm
    private CacheStats stats;

    public Cache(int capacity, EvictionStrategy<K> evictionStrategy) {
        this.capacity = capacity;
        this.store = new HashMap<>();
        this.evictionStrategy = evictionStrategy;
        this.stats = new CacheStats();
    }

    public void put(K key, V value) {
        stats.recordPut();
        if (store.containsKey(key)) {
            store.put(key, value);
            evictionStrategy.onAccess(key);
            return;
        }
        if (store.size() >= capacity) {
            K evictKey = evictionStrategy.evict();
            if (evictKey != null) {
                store.remove(evictKey);
                stats.recordEviction();
            }
        }
        store.put(key, value);
        evictionStrategy.onInsert(key);
    }

    public V get(K key) {
        if (!store.containsKey(key)) {
            stats.recordMiss();
            return null;
        }
        stats.recordHit();
        evictionStrategy.onAccess(key);
        return store.get(key);
    }

    public boolean containsKey(K key) { return store.containsKey(key); }

    public void remove(K key) {
        store.remove(key);
        evictionStrategy.onRemove(key);
    }

    public int size() { return store.size(); }
    public int capacity() { return capacity; }
    public void clear() { store.clear(); evictionStrategy.clear(); }
    public CacheStats getStats() { return stats; }
    public String getPolicyName() { return evictionStrategy.getName(); }
}

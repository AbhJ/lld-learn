/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/LRUCache.java — Least Recently Used eviction using LinkedHashMap

import java.util.*;

public class LRUCache<V> implements Cache<V> {   // implements = fulfills the Cache interface contract
    private int capacity;
    private LinkedHashMap<String, CacheEntry<V>> map; // LinkedHashMap with accessOrder=true gives LRU ordering
    private CacheStats stats;
    private List<EvictionListener<V>> listeners;  // private = observer list for eviction events

    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.stats = new CacheStats();
        this.listeners = new ArrayList<>();
        this.map = new LinkedHashMap<String, CacheEntry<V>>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, CacheEntry<V>> eldest) {
                if (size() > LRUCache.this.capacity) {
                    stats.recordEviction();
                    notifyEviction(eldest.getValue());
                    return true;
                }
                return false;
            }
        };
    }

    public void addEvictionListener(EvictionListener<V> listener) {
        listeners.add(listener);
    }

    private void notifyEviction(CacheEntry<V> entry) {
        for (EvictionListener<V> l : listeners) {
            l.onEviction(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void put(String key, V value) {
        stats.recordPut();
        CacheEntry<V> entry = new CacheEntry<>(key, value);
        map.put(key, entry);
    }

    @Override
    public V get(String key) {
        CacheEntry<V> entry = map.get(key);
        if (entry == null) {
            stats.recordMiss();
            return null;
        }
        entry.access();
        stats.recordHit();
        return entry.getValue();
    }

    @Override
    public boolean containsKey(String key) { return map.containsKey(key); }

    @Override
    public void remove(String key) { map.remove(key); }

    @Override
    public int size() { return map.size(); }

    @Override
    public int capacity() { return capacity; }

    @Override
    public void clear() { map.clear(); }

    @Override
    public CacheStats getStats() { return stats; }

    @Override
    public String getPolicyName() { return "LRU"; }

    public interface EvictionListener<V> {        // interface = callback contract for eviction observers
        void onEviction(String key, V value);
    }
}

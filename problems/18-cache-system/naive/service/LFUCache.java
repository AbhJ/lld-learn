/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/LFUCache.java — Least Frequently Used eviction with frequency buckets

import java.util.*;

public class LFUCache<V> implements Cache<V> {   // implements = fulfills the Cache interface contract
    private int capacity;
    private Map<String, CacheEntry<V>> cache;
    private Map<String, Integer> frequencies;    // private = tracks how often each key is accessed
    private Map<Integer, LinkedHashSet<String>> frequencyBuckets; // groups keys by frequency for O(1) eviction
    private int minFrequency;                    // private = tracks lowest frequency for eviction target
    private CacheStats stats;

    public LFUCache(int capacity) {
        this.capacity = capacity;
        this.cache = new HashMap<>();
        this.frequencies = new HashMap<>();
        this.frequencyBuckets = new HashMap<>();
        this.minFrequency = 0;
        this.stats = new CacheStats();
    }

    @Override
    public void put(String key, V value) {
        if (capacity <= 0) return;
        stats.recordPut();

        if (cache.containsKey(key)) {
            cache.get(key).setValue(value);
            get(key); // update frequency
            return;
        }

        if (cache.size() >= capacity) {
            evict();
        }

        CacheEntry<V> entry = new CacheEntry<>(key, value);
        cache.put(key, entry);
        frequencies.put(key, 1);
        frequencyBuckets.computeIfAbsent(1, k -> new LinkedHashSet<>()).add(key);
        minFrequency = 1;
    }

    @Override
    public V get(String key) {
        if (!cache.containsKey(key)) {
            stats.recordMiss();
            return null;
        }
        stats.recordHit();

        int freq = frequencies.get(key);
        frequencies.put(key, freq + 1);
        frequencyBuckets.get(freq).remove(key);

        if (frequencyBuckets.get(freq).isEmpty()) {
            frequencyBuckets.remove(freq);
            if (minFrequency == freq) minFrequency++;
        }

        frequencyBuckets.computeIfAbsent(freq + 1, k -> new LinkedHashSet<>()).add(key);
        cache.get(key).access();
        return cache.get(key).getValue();
    }

    private void evict() {
        LinkedHashSet<String> bucket = frequencyBuckets.get(minFrequency);
        if (bucket == null || bucket.isEmpty()) return;

        String evictKey = bucket.iterator().next();
        bucket.remove(evictKey);
        if (bucket.isEmpty()) {
            frequencyBuckets.remove(minFrequency);
        }
        cache.remove(evictKey);
        frequencies.remove(evictKey);
        stats.recordEviction();
    }

    @Override
    public boolean containsKey(String key) { return cache.containsKey(key); }

    @Override
    public void remove(String key) {
        if (!cache.containsKey(key)) return;
        int freq = frequencies.get(key);
        frequencyBuckets.get(freq).remove(key);
        if (frequencyBuckets.get(freq).isEmpty()) frequencyBuckets.remove(freq);
        cache.remove(key);
        frequencies.remove(key);
    }

    @Override
    public int size() { return cache.size(); }

    @Override
    public int capacity() { return capacity; }

    @Override
    public void clear() {
        cache.clear();
        frequencies.clear();
        frequencyBuckets.clear();
        minFrequency = 0;
    }

    @Override
    public CacheStats getStats() { return stats; }

    @Override
    public String getPolicyName() { return "LFU"; }
}

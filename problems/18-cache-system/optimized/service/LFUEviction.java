/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/LFUEviction.java — O(log n) LFU eviction using HashMap + TreeMap of frequencies

import java.util.*;

/**
 * O(log n) LFU eviction: HashMap for O(1) frequency lookup + TreeMap for O(log n) min-frequency access.
 * TreeMap<frequency, LinkedHashSet<key>> maintains insertion order within same frequency.
 */
public class LFUEviction<K> implements EvictionStrategy<K> { // implements = fulfills eviction strategy
    private Map<K, Integer> frequencies;         // HashMap = O(1) frequency lookup per key
    private TreeMap<Integer, LinkedHashSet<K>> frequencyBuckets; // TreeMap = O(log n) access to lowest frequency via firstEntry()

    public LFUEviction() {
        this.frequencies = new HashMap<>();
        this.frequencyBuckets = new TreeMap<>();
    }

    @Override
    public void onAccess(K key) {
        Integer freq = frequencies.get(key);
        if (freq == null) return;

        // Remove from current frequency bucket
        LinkedHashSet<K> bucket = frequencyBuckets.get(freq);
        if (bucket != null) {
            bucket.remove(key);
            if (bucket.isEmpty()) frequencyBuckets.remove(freq);
        }

        // Add to next frequency bucket
        int newFreq = freq + 1;
        frequencies.put(key, newFreq);
        frequencyBuckets.computeIfAbsent(newFreq, k -> new LinkedHashSet<>()).add(key);
    }

    @Override
    public void onInsert(K key) {
        frequencies.put(key, 1);
        frequencyBuckets.computeIfAbsent(1, k -> new LinkedHashSet<>()).add(key);
    }

    @Override
    public K evict() {
        if (frequencyBuckets.isEmpty()) return null;

        // TreeMap.firstEntry() gives the lowest frequency in O(log n)
        Map.Entry<Integer, LinkedHashSet<K>> lowest = frequencyBuckets.firstEntry();
        LinkedHashSet<K> bucket = lowest.getValue();
        K evictKey = bucket.iterator().next(); // FIFO within same frequency
        bucket.remove(evictKey);
        if (bucket.isEmpty()) frequencyBuckets.remove(lowest.getKey());
        frequencies.remove(evictKey);
        return evictKey;
    }

    @Override
    public void onRemove(K key) {
        Integer freq = frequencies.remove(key);
        if (freq != null) {
            LinkedHashSet<K> bucket = frequencyBuckets.get(freq);
            if (bucket != null) {
                bucket.remove(key);
                if (bucket.isEmpty()) frequencyBuckets.remove(freq);
            }
        }
    }

    @Override
    public void clear() {
        frequencies.clear();
        frequencyBuckets.clear();
    }

    @Override
    public String getName() { return "LFU (O(log n) TreeMap)"; }
}

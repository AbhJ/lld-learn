/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/TTLCache.java — Time-to-live eviction wrapping another cache implementation

import java.util.*;

public class TTLCache<V> implements Cache<V> {   // implements = fulfills Cache; adds TTL via decorator pattern
    private Cache<V> wrappedCache;               // private = hides the underlying cache (decorator pattern)
    private long defaultTtlMillis;
    private Map<String, Long> expiryTimes;       // private = tracks when each key expires

    public TTLCache(Cache<V> cache, long defaultTtlMillis) {
        this.wrappedCache = cache;
        this.defaultTtlMillis = defaultTtlMillis;
        this.expiryTimes = new HashMap<>();
    }

    @Override
    public void put(String key, V value) {
        wrappedCache.put(key, value);
        expiryTimes.put(key, System.currentTimeMillis() + defaultTtlMillis);
    }

    public void put(String key, V value, long ttlMillis) {
        wrappedCache.put(key, value);
        expiryTimes.put(key, System.currentTimeMillis() + ttlMillis);
    }

    @Override
    public V get(String key) {
        Long expiry = expiryTimes.get(key);
        if (expiry != null && System.currentTimeMillis() > expiry) {
            // Entry expired
            remove(key);
            wrappedCache.getStats().recordMiss();
            return null;
        }
        return wrappedCache.get(key);
    }

    @Override
    public boolean containsKey(String key) {
        Long expiry = expiryTimes.get(key);
        if (expiry != null && System.currentTimeMillis() > expiry) {
            remove(key);
            return false;
        }
        return wrappedCache.containsKey(key);
    }

    @Override
    public void remove(String key) {
        wrappedCache.remove(key);
        expiryTimes.remove(key);
    }

    @Override
    public int size() { return wrappedCache.size(); }

    @Override
    public int capacity() { return wrappedCache.capacity(); }

    @Override
    public void clear() {
        wrappedCache.clear();
        expiryTimes.clear();
    }

    @Override
    public CacheStats getStats() { return wrappedCache.getStats(); }

    @Override
    public String getPolicyName() { return "TTL(" + wrappedCache.getPolicyName() + ")"; }

    // Clean up all expired entries
    public int cleanUp() {
        int removed = 0;
        long now = System.currentTimeMillis();
        List<String> expired = new ArrayList<>();
        for (Map.Entry<String, Long> entry : expiryTimes.entrySet()) {
            if (now > entry.getValue()) {
                expired.add(entry.getKey());
            }
        }
        for (String key : expired) {
            remove(key);
            removed++;
        }
        return removed;
    }
}

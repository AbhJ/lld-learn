/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/CachingProxy.java — Proxy pattern: cache-aside layer in front of a real DataSource

public class CachingProxy<V> implements DataSource<String, V> {
    private final DataSource<String, V> inner;   // real backend; proxy delegates on miss
    private final Cache<V> cache;                // existing Cache<V> (String keys) used as storage

    public CachingProxy(DataSource<String, V> inner, Cache<V> cache) {
        this.inner = inner;
        this.cache = cache;
    }

    @Override
    public V load(String key) {
        if (cache.containsKey(key)) {
            System.out.println("[proxy] HIT " + key);
            return cache.get(key);
        }
        System.out.println("[proxy] MISS " + key);
        V value = inner.load(key);               // delegate to real source on miss
        cache.put(key, value);                   // populate cache for future hits (cache-aside)
        return value;
    }
}

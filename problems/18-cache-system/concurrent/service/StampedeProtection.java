/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/StampedeProtection.java — Singleflight pattern: only one thread recomputes expired key

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Cache stampede protection using the "singleflight" pattern.
 *
 * PROBLEM: When a popular cache key expires, 100 threads all see the miss
 * and all try to recompute the value simultaneously — hammering the backend.
 *
 * SOLUTION: Only ONE thread computes the new value. All other threads wait
 * for that computation to complete, then share the result.
 */
class StampedeProtection<V> {
    private final ConcurrentLRUCache<V> cache;   // final = reference never changes; safe publication
    private final ConcurrentHashMap<String, ReentrantLock> computeLocks = new ConcurrentHashMap<>(); // ConcurrentHashMap = per-key lock registry; thread-safe
    private final AtomicInteger computeCount = new AtomicInteger(0); // AtomicInteger = tracks computations across threads
    private final AtomicInteger waitCount = new AtomicInteger(0); // AtomicInteger = tracks threads that shared a result

    public StampedeProtection(ConcurrentLRUCache<V> cache) {
        this.cache = cache;
    }

    /**
     * Get value from cache, computing if missing/expired.
     * Only ONE thread per key will invoke the computeFunction.
     * Others wait and get the computed result.
     */
    public V getOrCompute(String key, Function<String, V> computeFunction) {
        // Fast path: cache hit
        V value = cache.get(key);
        if (value != null) {
            return value;
        }

        // Slow path: cache miss — use per-key lock to prevent stampede
        ReentrantLock lock = computeLocks.computeIfAbsent(key, k -> new ReentrantLock());
        lock.lock();
        try {
            // Double-check after acquiring lock (another thread may have computed it)
            value = cache.get(key);
            if (value != null) {
                waitCount.incrementAndGet();
                return value;
            }

            // We're the chosen one — compute the value
            computeCount.incrementAndGet();
            value = computeFunction.apply(key);
            cache.put(key, value);
            return value;
        } finally {
            lock.unlock();
            // Clean up lock entry to prevent memory leak
            computeLocks.remove(key, lock);
        }
    }

    public int getComputeCount() { return computeCount.get(); }
    public int getWaitCount() { return waitCount.get(); }
}

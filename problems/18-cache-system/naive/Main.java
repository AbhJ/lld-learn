/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating the cache system

/*
 * VARIATIONS FREQUENTLY ASKED:
 * 1. Distributed cache (Redis-like) - Consistent hashing, replication, partitioning
 * 2. Write-through/write-back - Different write policies, dirty bit tracking
 * 3. Multi-level cache (L1/L2) - In-process + remote, promotion/demotion
 * 4. Cache warming - Pre-load on startup, predictive caching
 * 5. Cache stampede prevention - Singleflight, probabilistic early expiry, locking
 *
 * See VARIATIONS.md for full solution approaches.
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Cache System Demo ===\n");

        // --- Test 1: LRU Cache ---
        System.out.println("--- Test 1: LRU Cache (capacity=3) ---");
        LRUCache<String> lru = new LRUCache<>(3);
        lru.addEvictionListener((key, value) ->
                System.out.println("  [Evicted] key=" + key + ", value=" + value));

        lru.put("A", "Apple");
        lru.put("B", "Banana");
        lru.put("C", "Cherry");
        System.out.println("Get A: " + lru.get("A")); // A is now most recently used
        System.out.println("Put D (should evict B - least recently used):");
        lru.put("D", "Date"); // B should be evicted (A was accessed recently)
        System.out.println("Get B: " + lru.get("B")); // Should be null
        System.out.println("Get A: " + lru.get("A")); // Should still be there
        System.out.println("Stats: " + lru.getStats());
        System.out.println();

        // --- Test 2: LFU Cache ---
        System.out.println("--- Test 2: LFU Cache (capacity=3) ---");
        LFUCache<String> lfu = new LFUCache<>(3);
        lfu.put("X", "X-ray");
        lfu.put("Y", "Yacht");
        lfu.put("Z", "Zebra");

        // Access X and Y multiple times
        lfu.get("X"); lfu.get("X"); lfu.get("X"); // X: freq=4
        lfu.get("Y"); lfu.get("Y");                 // Y: freq=3
        // Z has freq=1 (only inserted)

        System.out.println("Frequencies: X=4, Y=3, Z=1");
        System.out.println("Put W (should evict Z - least frequently used):");
        lfu.put("W", "Walrus");
        System.out.println("Get Z: " + lfu.get("Z")); // Should be null (evicted)
        System.out.println("Get X: " + lfu.get("X")); // Should still work
        System.out.println("Stats: " + lfu.getStats());
        System.out.println();

        // --- Test 3: FIFO Cache ---
        System.out.println("--- Test 3: FIFO Cache (capacity=3) ---");
        FIFOCache<Integer> fifo = new FIFOCache<>(3);
        fifo.put("1", 100);
        fifo.put("2", 200);
        fifo.put("3", 300);
        System.out.println("Access order doesn't matter for FIFO");
        fifo.get("1"); fifo.get("1"); fifo.get("1"); // Even though 1 is accessed most
        System.out.println("Put 4 (should evict 1 - first inserted):");
        fifo.put("4", 400);
        System.out.println("Get 1: " + fifo.get("1")); // Should be null (evicted)
        System.out.println("Get 2: " + fifo.get("2")); // Should still work
        System.out.println("Stats: " + fifo.getStats());
        System.out.println();

        // --- Test 4: TTL Cache ---
        System.out.println("--- Test 4: TTL Cache (200ms default TTL) ---");
        LRUCache<String> baseLru = new LRUCache<>(10);
        TTLCache<String> ttlCache = new TTLCache<>(baseLru, 200);

        ttlCache.put("session", "abc123");
        System.out.println("Immediately after put - Get session: " + ttlCache.get("session"));

        Thread.sleep(100);
        System.out.println("After 100ms - Get session: " + ttlCache.get("session"));

        Thread.sleep(150);
        System.out.println("After 250ms (expired) - Get session: " + ttlCache.get("session"));
        System.out.println();

        // --- Test 5: TTL with different TTLs ---
        System.out.println("--- Test 5: Different TTLs per entry ---");
        LRUCache<String> baseLru2 = new LRUCache<>(10);
        TTLCache<String> ttlCache2 = new TTLCache<>(baseLru2, 5000);
        ttlCache2.put("short", "expires soon", 100);
        ttlCache2.put("long", "stays longer", 5000);

        Thread.sleep(150);
        System.out.println("After 150ms:");
        System.out.println("  Short-lived entry: " + ttlCache2.get("short")); // expired
        System.out.println("  Long-lived entry: " + ttlCache2.get("long"));   // still valid
        System.out.println();

        // --- Test 6: Eviction Notification ---
        System.out.println("--- Test 6: Eviction Notification (Observer) ---");
        LRUCache<String> observedCache = new LRUCache<>(2);
        observedCache.addEvictionListener((key, value) ->
                System.out.println("  >> Eviction event: key=" + key));
        observedCache.put("first", "1st");
        observedCache.put("second", "2nd");
        observedCache.put("third", "3rd"); // evicts "first"
        observedCache.put("fourth", "4th"); // evicts "second"
        System.out.println();

        // --- Test 7: Cache Statistics Comparison ---
        System.out.println("--- Test 7: Statistics Comparison ---");
        Cache<String>[] caches = new Cache[]{
                new LRUCache<String>(3),
                new LFUCache<String>(3),
                new FIFOCache<String>(3)
        };

        for (Cache<String> cache : caches) {
            // Same workload on each
            cache.put("A", "1"); cache.put("B", "2"); cache.put("C", "3");
            cache.get("A"); cache.get("A"); cache.get("B");
            cache.put("D", "4"); // Triggers eviction
            cache.get("A"); cache.get("C");
            System.out.printf("  %s: %s%n", cache.getPolicyName(), cache.getStats());
        }
        System.out.println();

        // --- Test 8: Cache Operations ---
        System.out.println("--- Test 8: Other Cache Operations ---");
        LRUCache<String> opCache = new LRUCache<>(5);
        opCache.put("k1", "v1");
        opCache.put("k2", "v2");
        opCache.put("k3", "v3");
        System.out.println("Size: " + opCache.size() + "/" + opCache.capacity());
        System.out.println("Contains k2: " + opCache.containsKey("k2"));
        opCache.remove("k2");
        System.out.println("After remove k2 - Contains k2: " + opCache.containsKey("k2"));
        System.out.println("Size after remove: " + opCache.size());
        opCache.clear();
        System.out.println("Size after clear: " + opCache.size());
        System.out.println();

        // --- Test 9: CachingProxy fronting a slow DataSource (Proxy pattern) ---
        System.out.println("--- Test 9: CachingProxy over SlowDataSource ---");
        SlowDataSource slow = new SlowDataSource();
        Cache<String> proxyCache = new LRUCache<>(8);
        CachingProxy<String> proxy = new CachingProxy<>(slow, proxyCache);

        long t0 = System.currentTimeMillis();
        String first = proxy.load("user:42");                  // MISS — hits slow backend
        long firstMs = System.currentTimeMillis() - t0;
        System.out.println("First load=" + first + " took " + firstMs + "ms");

        long t1 = System.currentTimeMillis();
        String second = proxy.load("user:42");                 // HIT — served from cache
        long secondMs = System.currentTimeMillis() - t1;
        System.out.println("Second load=" + second + " took " + secondMs + "ms");
        System.out.println();

        System.out.println("=== Cache System Demo Complete ===");
    }
}

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating generic Cache<K,V> with pluggable O(1) LRU and O(log n) LFU

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Cache System (Optimized) ===\n");

        // --- Test 1: LRU Cache with O(1) operations ---
        System.out.println("--- Test 1: LRU Cache (O(1) HashMap + DoublyLinkedList) ---");
        Cache<String, String> lru = new Cache<>(3, new LRUEviction<>());
        lru.put("A", "Apple");
        lru.put("B", "Banana");
        lru.put("C", "Cherry");
        System.out.println("Get A: " + lru.get("A")); // A becomes MRU
        lru.put("D", "Date"); // Evicts B (LRU)
        System.out.println("Get B (evicted): " + lru.get("B"));
        System.out.println("Get A (still there): " + lru.get("A"));
        System.out.println("Stats: " + lru.getStats());
        System.out.println("Policy: " + lru.getPolicyName());
        System.out.println();

        // --- Test 2: LFU Cache with O(log n) operations ---
        System.out.println("--- Test 2: LFU Cache (O(log n) TreeMap frequencies) ---");
        Cache<String, String> lfu = new Cache<>(3, new LFUEviction<>());
        lfu.put("X", "X-ray");
        lfu.put("Y", "Yacht");
        lfu.put("Z", "Zebra");
        lfu.get("X"); lfu.get("X"); lfu.get("X"); // X: freq=4
        lfu.get("Y"); lfu.get("Y");                 // Y: freq=3
        // Z: freq=1 (only insert)
        System.out.println("Put W (evicts Z - least frequent):");
        lfu.put("W", "Walrus");
        System.out.println("Get Z (evicted): " + lfu.get("Z"));
        System.out.println("Get X (still there): " + lfu.get("X"));
        System.out.println("Stats: " + lfu.getStats());
        System.out.println("Policy: " + lfu.getPolicyName());
        System.out.println();

        // --- Test 3: Same API, different strategy ---
        System.out.println("--- Test 3: Pluggable Eviction (same Cache class) ---");
        Cache<Integer, String> cache1 = new Cache<>(2, new LRUEviction<>());
        Cache<Integer, String> cache2 = new Cache<>(2, new LFUEviction<>());

        cache1.put(1, "one"); cache1.put(2, "two");
        cache2.put(1, "one"); cache2.put(2, "two");

        cache1.get(1); // Makes 1 most recently used
        cache2.get(1); cache2.get(1); // Makes 1 most frequently used

        cache1.put(3, "three"); // LRU evicts 2 (least recently used)
        cache2.put(3, "three"); // LFU evicts 2 (least frequently used, freq=1)

        System.out.println("LRU: get(2)=" + cache1.get(2) + " (evicted, was LRU)");
        System.out.println("LFU: get(2)=" + cache2.get(2) + " (evicted, was LFU)");
        System.out.println("LRU: get(1)=" + cache1.get(1));
        System.out.println("LFU: get(1)=" + cache2.get(1));
        System.out.println();

        // --- Test 4: Statistics ---
        System.out.println("--- Test 4: Cache Statistics ---");
        Cache<String, Integer> statsCache = new Cache<>(3, new LRUEviction<>());
        statsCache.put("a", 1); statsCache.put("b", 2); statsCache.put("c", 3);
        statsCache.get("a"); statsCache.get("a"); statsCache.get("b");
        statsCache.get("z"); // miss
        statsCache.put("d", 4); // eviction
        statsCache.get("c"); // miss (evicted)
        System.out.println("Stats: " + statsCache.getStats());
        System.out.println();

        // --- Test 5: CachingProxy fronting a slow DataSource (Proxy pattern) ---
        System.out.println("--- Test 5: CachingProxy over SlowDataSource ---");
        SlowDataSource slow = new SlowDataSource();
        Cache<String, String> proxyCache = new Cache<>(8, new LRUEviction<>());
        CachingProxy<String, String> proxy = new CachingProxy<>(slow, proxyCache);

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

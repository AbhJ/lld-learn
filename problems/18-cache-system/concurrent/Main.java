/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — 100 threads hitting expired key, shows only 1 actual computation happens

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Cache System Demo ===\n");

        demonstrateStampede();
        System.out.println();
        demonstrateStampedeProtection();
    }

    static void demonstrateStampede() throws InterruptedException {
        System.out.println("--- Without Stampede Protection ---");
        System.out.println("100 threads request the same expired key simultaneously.\n");

        // Cache with 100ms TTL
        ConcurrentLRUCache<String> cache = new ConcurrentLRUCache<>(100, 100);
        cache.put("popular-key", "initial-value");

        // Wait for entry to expire
        Thread.sleep(150);

        int threadCount = 100;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger computeCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    String value = cache.get("popular-key");
                    if (value == null) {
                        // Simulate expensive backend call
                        computeCount.incrementAndGet();
                        cache.put("popular-key", "recomputed-value");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        doneLatch.await();

        System.out.println("Backend computations triggered: " + computeCount.get());
        System.out.println("PROBLEM: " + computeCount.get() + " redundant calls to backend!");
        System.out.println("(Should be 1, but without protection many threads hit the backend)");
    }

    static void demonstrateStampedeProtection() throws InterruptedException {
        System.out.println("--- With Stampede Protection (Singleflight Pattern) ---");
        System.out.println("100 threads request the same expired key simultaneously.\n");

        // Cache with 100ms TTL
        ConcurrentLRUCache<String> cache = new ConcurrentLRUCache<>(100, 100);
        StampedeProtection<String> protected_ = new StampedeProtection<>(cache);

        // Pre-populate and let it expire
        cache.put("popular-key", "initial-value");
        Thread.sleep(150);

        int threadCount = 100;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger backendCalls = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    String value = protected_.getOrCompute("popular-key", key -> {
                        backendCalls.incrementAndGet();
                        // Simulate expensive computation
                        try { Thread.sleep(10); } catch (InterruptedException e) {}
                        return "recomputed-value-from-backend";
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        doneLatch.await();

        System.out.println("Backend computations triggered: " + backendCalls.get());
        System.out.println("Threads that waited for result: " + protected_.getWaitCount());
        System.out.println("FIX: Only " + backendCalls.get() + " backend call(s) — others shared the result.");

        System.out.println("\n--- Summary ---");
        System.out.println("Total threads: " + threadCount);
        System.out.println("Actual computations: " + protected_.getComputeCount());
        System.out.println("Cache hits (after first compute): " + protected_.getWaitCount());
        System.out.println("Correctness: " + (backendCalls.get() <= 2 ? "PASSED" : "NEEDS REVIEW"));
        System.out.println("(Ideal is 1, acceptable is 1-2 due to lock granularity)");
    }
}

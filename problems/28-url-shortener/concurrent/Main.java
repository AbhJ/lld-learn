/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — 1000 concurrent URL creations, verify all unique short codes

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent URL Shortener Demo ===\n");

        UrlShortenerService service = new UrlShortenerService();
        int urlCount = 1000;
        int threadCount = 50;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        ConcurrentHashMap<String, String> results = new ConcurrentHashMap<>(); // ConcurrentHashMap = thread-safe result collector
        AtomicBoolean collisionFound = new AtomicBoolean(false);             // AtomicBoolean = thread-safe flag; visible to all threads

        System.out.println("Scenario: 1000 unique URLs shortened by 50 threads concurrently.");
        System.out.println("Expected: All 1000 get unique short codes, no collisions.\n");

        // Pre-generate URLs
        String[] urls = new String[urlCount];
        for (int i = 0; i < urlCount; i++) {
            urls[i] = "https://example.com/page/" + i + "/" + UUID.randomUUID();
        }

        int urlsPerThread = urlCount / threadCount;
        for (int t = 0; t < threadCount; t++) {
            final int start = t * urlsPerThread;
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int i = start; i < start + urlsPerThread; i++) {
                        String code = service.shorten(urls[i]);
                        String prev = results.putIfAbsent(code, urls[i]);
                        if (prev != null && !prev.equals(urls[i])) {
                            collisionFound.set(true);
                            System.err.println("COLLISION: " + code + " -> " + prev + " AND " + urls[i]);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }, "Shortener-" + t).start();
        }

        startLatch.countDown();
        doneLatch.await();

        // Verify all codes are unique and resolvable
        Set<String> uniqueCodes = results.keySet();
        boolean allUnique = uniqueCodes.size() == urlCount;
        boolean allResolvable = true;
        int checked = 0;
        for (Map.Entry<String, String> entry : results.entrySet()) {
            String resolved = service.resolve(entry.getKey());
            if (!entry.getValue().equals(resolved)) {
                allResolvable = false;
                break;
            }
            checked++;
        }

        System.out.println("--- Results ---");
        System.out.println("URLs shortened: " + service.getUrlCount());
        System.out.println("Unique codes generated: " + uniqueCodes.size());
        System.out.println("All unique: " + allUnique);
        System.out.println("All resolvable: " + allResolvable);
        System.out.println("Collisions found: " + collisionFound.get());

        boolean passed = allUnique && allResolvable && !collisionFound.get();
        System.out.println("\nCorrectness check: " + (passed ? "PASSED" : "FAILED"));
    }
}

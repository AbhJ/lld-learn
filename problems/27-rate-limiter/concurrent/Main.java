/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — 100 concurrent requests with limit=10/second, exactly 10 pass

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Rate Limiter Demo ===\n");

        int limit = 10;
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(limit);

        int requestCount = 100;
        CountDownLatch startLatch = new CountDownLatch(1);    // CountDownLatch = barrier; threads wait until released
        CountDownLatch doneLatch = new CountDownLatch(requestCount); // counts down as threads complete
        AtomicInteger allowed = new AtomicInteger(0);        // AtomicInteger = thread-safe counter
        AtomicInteger rejected = new AtomicInteger(0);       // AtomicInteger = no lost increments under contention

        System.out.println("Scenario: 100 concurrent requests against a rate limit of 10 tokens.");
        System.out.println("Expected: Exactly 10 requests pass, 90 are rejected.\n");

        for (int t = 0; t < requestCount; t++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    RateLimitResult result = limiter.tryAcquire();
                    if (result.isAllowed()) {
                        allowed.incrementAndGet();
                    } else {
                        rejected.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }, "Req-" + t).start();
        }

        startLatch.countDown();
        doneLatch.await();

        System.out.println("--- Results ---");
        System.out.println("Total requests: " + requestCount);
        System.out.println("Allowed: " + allowed.get());
        System.out.println("Rejected: " + rejected.get());
        System.out.println("Remaining tokens: " + limiter.getAvailableTokens());

        boolean passed = allowed.get() == limit && rejected.get() == (requestCount - limit);
        System.out.println("\nCorrectness check: " + (passed ? "PASSED" : "FAILED"));
    }
}

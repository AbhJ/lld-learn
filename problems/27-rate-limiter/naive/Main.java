/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Demonstrates rate limiting with synchronized counters
public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Rate Limiter Demo (Naive) ===\n");

        RateLimiter limiter = RateLimiterService.createSlidingWindow(5, 2000);
        System.out.println("--- Sliding Window: 5 requests per 2s (synchronized) ---");

        for (int i = 1; i <= 7; i++) {
            boolean allowed = limiter.allowRequest(new Request("client1"));
            System.out.println("Request " + i + ": " + (allowed ? "ALLOWED" : "DENIED"));
        }

        System.out.println("\nWaiting 2.1 seconds...");
        Thread.sleep(2100);

        boolean allowed = limiter.allowRequest(new Request("client1"));
        System.out.println("After window: " + (allowed ? "ALLOWED" : "DENIED"));

        System.out.println("\n--- Multi-client isolation ---");
        System.out.println("ClientA: " + (limiter.allowRequest(new Request("A")) ? "ALLOWED" : "DENIED"));
        System.out.println("ClientB: " + (limiter.allowRequest(new Request("B")) ? "ALLOWED" : "DENIED"));

        System.out.println("\n=== Demo Complete ===");
    }
}

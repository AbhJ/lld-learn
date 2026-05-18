/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — 50 threads making requests, circuit trips correctly even under concurrency

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Circuit Breaker Demo ===\n");

        // Fast config: trips after 3 failures, resets after 200ms
        CircuitBreakerConfig config = CircuitBreakerConfig.fastConfig();
        ConcurrentCircuitBreaker breaker = new ConcurrentCircuitBreaker(config);

        System.out.println("Config: threshold=" + config.getFailureThreshold() +
                " failures, resetTimeout=" + config.getResetTimeoutMs() + "ms");
        System.out.println("\n--- Phase 1: 50 threads hitting a FAILING service ---");

        int threadCount = 50;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger succeeded = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);
        AtomicInteger rejected = new AtomicInteger(0);

        // All requests will fail — simulating a backend outage
        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    breaker.execute(() -> {
                        throw new RuntimeException("Backend unavailable");
                    });
                    succeeded.incrementAndGet();
                } catch (CircuitOpenException e) {
                    rejected.incrementAndGet(); // Circuit was open — fast fail!
                } catch (RuntimeException e) {
                    failed.incrementAndGet(); // Request went through but failed
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        doneLatch.await();

        System.out.println("Results:");
        System.out.println("  Requests that hit backend (failed): " + failed.get());
        System.out.println("  Requests fast-rejected by circuit: " + rejected.get());
        System.out.println("  Circuit state: " + breaker.getState());
        System.out.println("  Total failures recorded: " + breaker.getFailureCount());
        System.out.println("\n  KEY INSIGHT: Only ~" + failed.get() + " requests hit the failing backend.");
        System.out.println("  The other " + rejected.get() + " were fast-rejected, protecting the backend.");

        // Phase 2: Wait for reset timeout, then send successful requests
        System.out.println("\n--- Phase 2: After reset timeout, service recovers ---");
        Thread.sleep(config.getResetTimeoutMs() + 50);

        CountDownLatch phase2Done = new CountDownLatch(5);
        AtomicInteger phase2Success = new AtomicInteger(0);

        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                try {
                    String result = breaker.execute(() -> "OK");
                    phase2Success.incrementAndGet();
                } catch (CircuitOpenException e) {
                    // May happen for threads that arrive while still HALF_OPEN
                } catch (Exception e) {
                    // Shouldn't happen
                } finally {
                    phase2Done.countDown();
                }
            }).start();
        }

        phase2Done.await();

        System.out.println("After recovery:");
        System.out.println("  Successful requests: " + phase2Success.get());
        System.out.println("  Circuit state: " + breaker.getState());

        // Summary
        System.out.println("\n--- Summary ---");
        System.out.println("Total requests: " + breaker.getTotalRequests());
        System.out.println("Successes: " + breaker.getSuccessCount());
        System.out.println("Failures: " + breaker.getFailureCount());
        System.out.println("Fast-rejected: " + breaker.getRejectedCount());
        System.out.println("Final state: " + breaker.getState());

        boolean circuitProtected = rejected.get() > 0; // Some requests were fast-failed
        boolean stateCorrect = breaker.getState() == CircuitState.CLOSED; // Recovery worked
        System.out.println("\nCircuit protected backend: " + (circuitProtected ? "PASSED" : "FAILED"));
        System.out.println("Circuit recovered correctly: " + (stateCorrect ? "PASSED" : "FAILED"));
    }
}

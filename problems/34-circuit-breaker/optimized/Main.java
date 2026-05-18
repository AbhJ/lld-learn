/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Demonstrates circuit breaker with sliding window and atomic state transitions
public class Main {
    private static boolean serviceHealthy = true;

    public static void main(String[] args) throws Exception {
        System.out.println("=== Circuit Breaker (Optimized: Sliding Window) Demo ===\n");

        // Window of 10, trip at 50% failure rate, 500ms open timeout, 2 probes required
        CircuitBreakerConfig config = new CircuitBreakerConfig(10, 0.5, 500, 2);
        CircuitBreaker cb = new CircuitBreaker("PaymentService", config);
        ServiceCall<String> call = () -> {
            if (!serviceHealthy) throw new RuntimeException("Service down");
            return "OK";
        };

        // --- Test 1: Normal Operation ---
        System.out.println("--- Test 1: Normal (stays CLOSED) ---");
        serviceHealthy = true;
        for (int i = 0; i < 5; i++) System.out.println("  " + cb.execute(call));
        System.out.println("  State: " + cb.getStateName());

        // --- Test 2: Intermittent Failures (below threshold) ---
        System.out.println("\n--- Test 2: Intermittent Failures (below 50%) ---");
        // 3 failures out of 10 = 30% < 50% threshold — should stay closed
        for (int i = 0; i < 3; i++) {
            serviceHealthy = false;
            try { cb.execute(call); } catch (Exception e) { System.out.println("  Fail: " + e.getMessage()); }
        }
        serviceHealthy = true;
        for (int i = 0; i < 2; i++) cb.execute(call);
        System.out.println("  State (should be CLOSED): " + cb.getStateName());

        // --- Test 3: Sustained Failures Trip Circuit ---
        System.out.println("\n--- Test 3: Sustained failures trip to OPEN ---");
        serviceHealthy = false;
        for (int i = 0; i < 10; i++) {
            try { cb.execute(call); } catch (Exception e) { /* expected */ }
        }
        System.out.println("  State: " + cb.getStateName());

        // --- Test 4: Fast-Fail While Open ---
        System.out.println("\n--- Test 4: Fast-Fail while OPEN ---");
        try { cb.execute(call); } catch (Exception e) { System.out.println("  " + e.getMessage()); }

        // --- Test 5: Recovery with Probes ---
        System.out.println("\n--- Test 5: Recovery (HALF_OPEN probes) ---");
        Thread.sleep(600);
        serviceHealthy = true;
        System.out.println("  Probe 1: " + cb.execute(call));
        System.out.println("  Probe 2: " + cb.execute(call));
        System.out.println("  State: " + cb.getStateName());

        System.out.println("\n  " + cb.getMetrics());
        System.out.println("\n=== Circuit Breaker (Optimized) Demo Complete ===");
    }
}

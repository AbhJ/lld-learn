/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Demonstrates circuit breaker with simple failure counter
public class Main {
    private static boolean serviceHealthy = true;

    public static void main(String[] args) throws Exception {
        System.out.println("=== Circuit Breaker (Naive) Demo ===\n");

        CircuitBreakerConfig config = new CircuitBreakerConfig(3, 500, 2);
        CircuitBreaker cb = new CircuitBreaker("PaymentService", config);
        ServiceCall<String> call = () -> {
            if (!serviceHealthy) throw new RuntimeException("Service unavailable");
            return "OK";
        };

        // --- Test 1: Normal Operation ---
        System.out.println("--- Test 1: Normal (CLOSED) ---");
        System.out.println("  Result: " + cb.execute(call));
        System.out.println("  State: " + cb.getStateName());

        // --- Test 2: Failures Trip Circuit ---
        System.out.println("\n--- Test 2: Failures Trip Circuit ---");
        serviceHealthy = false;
        for (int i = 1; i <= 3; i++) {
            try { cb.execute(call); } catch (Exception e) { System.out.println("  Fail " + i + ": " + e.getMessage()); }
        }
        System.out.println("  State: " + cb.getStateName());

        // --- Test 3: Open Fails Fast ---
        System.out.println("\n--- Test 3: Open Fails Fast ---");
        try { cb.execute(call); } catch (Exception e) { System.out.println("  " + e.getMessage()); }

        // --- Test 4: Recovery ---
        System.out.println("\n--- Test 4: Recovery via HALF_OPEN ---");
        Thread.sleep(600);
        serviceHealthy = true;
        System.out.println("  Result: " + cb.execute(call));
        System.out.println("  Result: " + cb.execute(call));
        System.out.println("  State: " + cb.getStateName());

        System.out.println("\n  Metrics: " + cb.getMetrics());
        System.out.println("\n=== Circuit Breaker (Naive) Demo Complete ===");
    }
}

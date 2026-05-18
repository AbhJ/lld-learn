/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — Same payment submitted 10 times concurrently, exactly 1 processed

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Payment Gateway Demo ===\n");

        PaymentGateway gateway = new PaymentGateway();
        int duplicateSubmissions = 10;
        CountDownLatch startLatch = new CountDownLatch(1);           // CountDownLatch = barrier; all threads wait until released
        CountDownLatch doneLatch = new CountDownLatch(duplicateSubmissions); // counts down as threads finish
        AtomicInteger successCount = new AtomicInteger(0);          // AtomicInteger = thread-safe counter without locks

        String idempotencyKey = "pay-order-12345-abc";
        String orderId = "ORDER-12345";
        long amount = 9999L; // $99.99

        System.out.println("Scenario: Same payment (key=" + idempotencyKey + ") submitted 10 times concurrently.");
        System.out.println("Expected: Exactly 1 payment processed, 9 rejected as duplicates.\n");

        for (int t = 0; t < duplicateSubmissions; t++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    Transaction result = gateway.processPayment(idempotencyKey, orderId, amount);
                    if (result != null) {
                        successCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }, "PayThread-" + t).start();
        }

        startLatch.countDown();
        doneLatch.await();

        // Also test that different idempotency keys work independently
        gateway.processPayment("pay-order-99999-xyz", "ORDER-99999", 5000L);

        System.out.println("--- Results ---");
        System.out.println("Total attempts for same key: " + duplicateSubmissions);
        System.out.println("Successful processes: " + successCount.get());
        System.out.println("Duplicates rejected: " + gateway.getDuplicatesRejected());
        System.out.println("Total unique payments in system: " + gateway.getProcessedCount());

        boolean passed = successCount.get() == 1 && gateway.getProcessedCount() == 2;
        System.out.println("\nCorrectness check: " + (passed ? "PASSED" : "FAILED"));
    }
}

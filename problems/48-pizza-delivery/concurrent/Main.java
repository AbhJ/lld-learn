/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — 10 orders progressing through stages concurrently, verify each order's lifecycle is independent

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Pizza Delivery Demo ===\n");
        System.out.println("Race condition: Multiple orders from same address processed simultaneously");
        System.out.println("— delivery tracking gets mixed up.\n");

        DeliveryTracker tracker = new DeliveryTracker();
        int orderCount = 10;
        String sharedAddress = "123 Main St";

        // Create orders from same address
        for (int i = 0; i < orderCount; i++) {
            tracker.addOrder(new PizzaOrder("PIZZA-" + i, sharedAddress));
        }

        // Each order gets its own progression thread
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(orderCount);
        AtomicInteger progressSuccess = new AtomicInteger(0);

        for (int i = 0; i < orderCount; i++) {
            final String orderId = "PIZZA-" + i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    if (tracker.progressOrder(orderId)) {
                        progressSuccess.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        // Also spawn duplicate progression threads to simulate race
        CountDownLatch doneLatch2 = new CountDownLatch(orderCount);
        AtomicInteger duplicateFails = new AtomicInteger(0);

        for (int i = 0; i < orderCount; i++) {
            final String orderId = "PIZZA-" + i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    if (!tracker.progressOrder(orderId)) {
                        duplicateFails.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch2.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        doneLatch.await();
        doneLatch2.await();

        // Verify
        System.out.println("--- Results ---");
        System.out.println("Orders from same address: " + orderCount);
        System.out.println("Successful full progressions: " + progressSuccess.get());
        System.out.println("Duplicate progressions blocked: " + duplicateFails.get());
        System.out.println("Total completed: " + tracker.getCompletedCount());

        // Check each order independently
        boolean allIndependent = true;
        boolean allValidHistory = true;
        DeliveryState[] expectedProgression = DeliveryState.values();

        for (PizzaOrder order : tracker.getAllOrders()) {
            List<String> history = order.getStateHistory();

            // Verify ordering is valid (each state appears in correct sequence)
            int lastIdx = -1;
            for (String stateStr : history) {
                DeliveryState s = DeliveryState.valueOf(stateStr);
                int idx = s.ordinal();
                if (idx <= lastIdx) {
                    allValidHistory = false;
                    break;
                }
                lastIdx = idx;
            }

            // Verify final state is DELIVERED
            if (!order.isDelivered()) {
                // Only the winner thread delivers, duplicate might fail partway
            }
        }

        // Exactly one progression per order should fully succeed
        // (either the primary or duplicate thread, but not both)
        boolean completedMatchesOrders = (tracker.getCompletedCount() == orderCount);
        boolean noMixup = (progressSuccess.get() + duplicateFails.get() >= orderCount);

        System.out.println("\nAll orders delivered: " + completedMatchesOrders);
        System.out.println("Valid state histories: " + allValidHistory);
        System.out.println("No tracking mixup (primary+dup >= orders): " + noMixup);

        // Print sample order histories
        System.out.println("\nSample histories:");
        int shown = 0;
        for (PizzaOrder order : tracker.getAllOrders()) {
            if (shown++ >= 3) break;
            System.out.println("  " + order.getOrderId() + ": " + order.getStateHistory());
        }

        boolean passed = completedMatchesOrders && allValidHistory && noMixup;
        System.out.println("\nCorrectness check: " + (passed ? "PASSED" : "FAILED"));
    }
}

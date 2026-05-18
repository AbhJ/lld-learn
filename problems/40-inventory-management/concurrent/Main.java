/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — 20 orders of 3 units each for product with stock=10, only first 3 orders (9 units) succeed

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Inventory Management Demo ===\n");

        InventoryService inventory = new InventoryService();
        int initialStock = 10;
        int orderQuantity = 3;
        int orderCount = 20;
        String productId = "WIDGET-001";

        inventory.addProduct(new Product(productId, "Widget", initialStock));

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(orderCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        System.out.println("Scenario: 20 concurrent orders, each requesting 3 units.");
        System.out.println("  Stock = 10. Only 3 orders (9 units) can succeed.");
        System.out.println("Expected: Exactly 3 orders succeed, stock ends at 1. No overselling.\n");

        for (int t = 0; t < orderCount; t++) {
            final int orderId = t;
            new Thread(() -> {
                try {
                    startLatch.await();
                    boolean success = inventory.processOrder(productId, orderQuantity);
                    if (success) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }, "Order-" + t).start();
        }

        startLatch.countDown();
        doneLatch.await();

        int finalStock = inventory.getStock(productId);
        int expectedSuccesses = initialStock / orderQuantity; // 10 / 3 = 3
        int expectedFinalStock = initialStock - (expectedSuccesses * orderQuantity); // 10 - 9 = 1

        System.out.println("--- Results ---");
        System.out.println("Initial stock: " + initialStock);
        System.out.println("Order quantity: " + orderQuantity);
        System.out.println("Orders attempted: " + orderCount);
        System.out.println("Orders succeeded: " + successCount.get());
        System.out.println("Orders failed: " + failCount.get());
        System.out.println("Final stock: " + finalStock);
        System.out.println("Stock non-negative: " + (finalStock >= 0));

        boolean passed = successCount.get() == expectedSuccesses
                && finalStock == expectedFinalStock
                && finalStock >= 0;
        System.out.println("\nCorrectness check: " + (passed ? "PASSED" : "FAILED"));
    }
}

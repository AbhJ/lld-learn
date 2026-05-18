/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — Waiter adding items while kitchen polls queue, verify no partial orders

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Restaurant Ordering Demo ===\n");

        Restaurant restaurant = new Restaurant();
        int orderCount = 20;
        int itemsPerOrder = 5;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch waiterDone = new CountDownLatch(1);
        AtomicBoolean partialOrderServed = new AtomicBoolean(false);
        AtomicInteger kitchenProcessed = new AtomicInteger(0);

        System.out.println("Scenario: Waiter builds 20 orders (5 items each).");
        System.out.println("  Kitchen thread polls simultaneously trying to pick up orders.");
        System.out.println("Expected: Kitchen never gets a partial order (fewer than 5 items).\n");

        // Kitchen thread — aggressively polls for orders
        Thread kitchenThread = new Thread(() -> {
            try {
                startLatch.await();
                while (!Thread.currentThread().isInterrupted()) {
                    Order order = restaurant.kitchenPickup();
                    if (order != null) {
                        int items = order.getItemCount();
                        if (items < itemsPerOrder) {
                            partialOrderServed.set(true);
                            System.err.println("PARTIAL ORDER! Order " + order.getId() +
                                " has " + items + " items (expected " + itemsPerOrder + ")");
                        }
                        restaurant.verifyOrder(order, itemsPerOrder);
                        kitchenProcessed.incrementAndGet();
                    }
                    if (kitchenProcessed.get() >= orderCount) break;
                    Thread.yield();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Kitchen");

        // Waiter thread — builds orders and submits them
        Thread waiterThread = new Thread(() -> {
            try {
                startLatch.await();
                for (int o = 0; o < orderCount; o++) {
                    Order order = restaurant.createOrder(o);
                    for (int i = 0; i < itemsPerOrder; i++) {
                        order.addItem("Item-" + o + "-" + i);
                        Thread.yield(); // Maximize race window
                    }
                    restaurant.submitOrder(order);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                waiterDone.countDown();
            }
        }, "Waiter");

        kitchenThread.start();
        waiterThread.start();
        startLatch.countDown();

        waiterDone.await();
        kitchenThread.join(3000); // Wait up to 3s for kitchen to finish
        if (kitchenThread.isAlive()) kitchenThread.interrupt();

        System.out.println("--- Results ---");
        System.out.println("Orders created: " + orderCount);
        System.out.println("Orders processed by kitchen: " + kitchenProcessed.get());
        System.out.println("Partial orders detected: " + restaurant.getPartialOrdersDetected());
        System.out.println("Partial order served: " + partialOrderServed.get());

        boolean passed = !partialOrderServed.get() && kitchenProcessed.get() == orderCount;
        System.out.println("\nCorrectness check: " + (passed ? "PASSED" : "FAILED"));
    }
}

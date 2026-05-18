/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — Pay thread + cancel thread racing, verify exactly one wins

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Order Management Demo ===\n");
        System.out.println("Race condition: Payment confirmation and user cancellation arrive simultaneously");
        System.out.println("— order is both paid and cancelled.\n");

        int orderCount = 100; // Test with 100 orders for statistical confidence
        OrderService service = new OrderService();
        List<Order> orders = new ArrayList<>();
        for (int i = 0; i < orderCount; i++) {
            orders.add(new Order("ORD-" + i));
        }

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(orderCount * 2); // 2 threads per order

        AtomicInteger payWins = new AtomicInteger(0);
        AtomicInteger cancelWins = new AtomicInteger(0);

        for (int i = 0; i < orderCount; i++) {
            final Order order = orders.get(i);

            // Payment thread
            new Thread(() -> {
                try {
                    startLatch.await();
                    if (service.pay(order, "PaymentGateway")) {
                        payWins.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }).start();

            // Cancel thread
            new Thread(() -> {
                try {
                    startLatch.await();
                    if (service.cancel(order, "UserAction")) {
                        cancelWins.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        doneLatch.await();

        // Verify correctness
        int bothState = 0;
        int neitherState = 0;
        int paidCount = 0;
        int cancelledCount = 0;

        for (Order order : orders) {
            OrderState state = order.getState();
            if (state == OrderState.PAID) paidCount++;
            else if (state == OrderState.CANCELLED) cancelledCount++;
            else if (state == OrderState.CREATED) neitherState++; // Should never happen
        }

        System.out.println("--- Results ---");
        System.out.println("Orders tested: " + orderCount);
        System.out.println("Payment wins: " + payWins.get());
        System.out.println("Cancel wins: " + cancelWins.get());
        System.out.println("Orders in PAID state: " + paidCount);
        System.out.println("Orders in CANCELLED state: " + cancelledCount);
        System.out.println("Orders still CREATED (bug): " + neitherState);

        // Each order must be in exactly one terminal state
        boolean eachOrderOneState = (paidCount + cancelledCount == orderCount);
        boolean winsMatchStates = (payWins.get() == paidCount && cancelWins.get() == cancelledCount);
        boolean totalWinsCorrect = (payWins.get() + cancelWins.get() == orderCount);
        boolean noContradiction = (neitherState == 0);

        System.out.println("\n--- Consistency Checks ---");
        System.out.println("Each order in exactly one state: " + eachOrderOneState);
        System.out.println("Win counts match states: " + winsMatchStates);
        System.out.println("Total winners = order count: " + totalWinsCorrect);
        System.out.println("No contradictory state: " + noContradiction);

        boolean passed = eachOrderOneState && winsMatchStates && totalWinsCorrect && noContradiction;
        System.out.println("\nCorrectness check: " + (passed ? "PASSED" : "FAILED"));
    }
}

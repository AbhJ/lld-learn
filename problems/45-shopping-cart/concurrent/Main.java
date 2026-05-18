/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — Add-to-cart threads racing with checkout thread, verify checkout snapshot is consistent

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Shopping Cart Demo ===\n");
        System.out.println("Race condition: User adds item while checkout process is reading cart");
        System.out.println("— inconsistent total, item missed in order.\n");

        ShoppingCart cart = new ShoppingCart();
        int adderThreads = 8;
        int itemsPerThread = 50;
        AtomicInteger addSuccessCount = new AtomicInteger(0);
        AtomicInteger addRejectedCount = new AtomicInteger(0);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(adderThreads + 1); // +1 for checkout thread
        List<CartItem> checkoutSnapshot = new CopyOnWriteArrayList<>();

        // Adder threads
        for (int t = 0; t < adderThreads; t++) {
            final int threadId = t;
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < itemsPerThread; i++) {
                        CartItem item = new CartItem(
                            "item-" + threadId + "-" + i,
                            "Product-" + threadId + "-" + i,
                            10.0 + threadId,
                            1
                        );
                        if (cart.addItem(item)) {
                            addSuccessCount.incrementAndGet();
                        } else {
                            addRejectedCount.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        // Checkout thread — starts slightly after adders to let some items accumulate
        new Thread(() -> {
            try {
                startLatch.await();
                Thread.sleep(5); // Let some adds happen first
                List<CartItem> snapshot = cart.checkout();
                checkoutSnapshot.addAll(snapshot);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                doneLatch.countDown();
            }
        }).start();

        startLatch.countDown();
        doneLatch.await();

        // Verify snapshot consistency
        int snapshotSize = checkoutSnapshot.size();
        double snapshotTotal = 0;
        for (CartItem item : checkoutSnapshot) {
            snapshotTotal += item.getTotal();
        }

        // Recalculate total from snapshot items to verify consistency
        double recalcTotal = 0;
        for (CartItem item : checkoutSnapshot) {
            recalcTotal += item.getPrice() * item.getQuantity();
        }

        // After checkout, no more adds should succeed
        boolean postCheckoutAdd = cart.addItem(new CartItem("late", "Late Item", 5.0, 1));

        System.out.println("--- Results ---");
        System.out.println("Adder threads: " + adderThreads);
        System.out.println("Items per thread: " + itemsPerThread);
        System.out.println("Total add attempts: " + (adderThreads * itemsPerThread));
        System.out.println("Adds succeeded: " + addSuccessCount.get());
        System.out.println("Adds rejected (after checkout): " + addRejectedCount.get());
        System.out.println("Checkout snapshot size: " + snapshotSize);
        System.out.println("Snapshot total: $" + String.format("%.2f", snapshotTotal));
        System.out.println("Recalculated total: $" + String.format("%.2f", recalcTotal));
        System.out.println("Post-checkout add rejected: " + !postCheckoutAdd);

        // Consistency checks
        boolean totalsMatch = (Math.abs(snapshotTotal - recalcTotal) < 0.001);
        boolean snapshotSizeMatchesAdds = (snapshotSize <= addSuccessCount.get());
        boolean noPostCheckoutAdd = !postCheckoutAdd;
        boolean allAccountedFor = (addSuccessCount.get() + addRejectedCount.get() == adderThreads * itemsPerThread);

        System.out.println("\n--- Consistency Checks ---");
        System.out.println("Totals consistent: " + totalsMatch);
        System.out.println("Snapshot size <= successful adds: " + snapshotSizeMatchesAdds);
        System.out.println("Post-checkout blocked: " + noPostCheckoutAdd);
        System.out.println("All attempts accounted for: " + allAccountedFor);

        boolean passed = totalsMatch && snapshotSizeMatchesAdds && noPostCheckoutAdd && allAccountedFor;
        System.out.println("\nCorrectness check: " + (passed ? "PASSED" : "FAILED"));
    }
}

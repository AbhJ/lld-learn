/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — 10 threads buying product with stock=3, exactly 3 succeed

import model.Product;
import service.VendingMachine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Vending Machine Demo ===\n");

        final int NUM_BUYERS = 10;
        final int STOCK = 3;

        VendingMachine machine = new VendingMachine();
        Product cola = new Product("Cola", 1.50, STOCK);
        machine.addProduct(cola);

        System.out.println("Product: " + cola);
        System.out.println("Buyers attempting simultaneously: " + NUM_BUYERS + "\n");

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(NUM_BUYERS);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < NUM_BUYERS; i++) {
            final String userId = "User-" + i;
            Thread t = new Thread(() -> {
                try {
                    startLatch.await();
                } catch (InterruptedException ignored) {}

                boolean success = machine.purchase(userId, "Cola");
                if (success) {
                    successCount.incrementAndGet();
                    System.out.println("  [SUCCESS] " + userId + " got a Cola");
                } else {
                    failCount.incrementAndGet();
                    System.out.println("  [REJECTED] " + userId + " — out of stock");
                }
                doneLatch.countDown();
            });
            threads.add(t);
            t.start();
        }

        // Release all threads simultaneously
        startLatch.countDown();
        doneLatch.await();

        System.out.println("\n--- Results ---");
        System.out.println("Product after attempts: " + cola);
        System.out.println("Remaining stock: " + cola.getQuantity());

        int succeeded = successCount.get();
        int rejected = failCount.get();

        System.out.println("\n" + NUM_BUYERS + " threads attempted, " + succeeded + " succeeded, " + rejected + " correctly rejected");

        boolean passed = (succeeded == STOCK) && (rejected == NUM_BUYERS - STOCK) && (cola.getQuantity() == 0);
        System.out.println("Correctness check: " + (passed ? "PASSED" : "FAILED"));
    }
}

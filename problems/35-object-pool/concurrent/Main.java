/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — 20 threads borrowing from pool of 5, verify proper blocking and no double-borrow

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Object Pool Demo ===\n");

        int poolSize = 5;
        int threadCount = 20;
        ObjectPool pool = new ObjectPool(poolSize);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicBoolean doubleBorrow = new AtomicBoolean(false);
        AtomicInteger successfulBorrows = new AtomicInteger(0);
        ConcurrentHashMap<Integer, AtomicInteger> concurrentUsers = new ConcurrentHashMap<>();

        System.out.println("Scenario: 20 threads borrow from a pool of 5 objects.");
        System.out.println("  Each thread holds the object for 50ms then returns it.");
        System.out.println("Expected: No double-borrow, proper blocking, all threads served.\n");

        for (int t = 0; t < threadCount; t++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    PooledObject obj = pool.borrow(5000); // 5s timeout
                    if (obj != null) {
                        // Track concurrent usage of same object
                        AtomicInteger users = concurrentUsers.computeIfAbsent(
                            obj.getId(), k -> new AtomicInteger(0));
                        int active = users.incrementAndGet();
                        if (active > 1) {
                            doubleBorrow.set(true);
                        }

                        obj.use();
                        successfulBorrows.incrementAndGet();

                        // Simulate work
                        Thread.sleep(50);

                        users.decrementAndGet();
                        pool.returnObject(obj);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }, "Worker-" + t).start();
        }

        startLatch.countDown();
        doneLatch.await();

        System.out.println("--- Results ---");
        System.out.println("Pool capacity: " + poolSize);
        System.out.println("Threads: " + threadCount);
        System.out.println("Successful borrows: " + successfulBorrows.get());
        System.out.println("Total borrows: " + pool.getBorrowCount());
        System.out.println("Total returns: " + pool.getReturnCount());
        System.out.println("Double-borrow detected: " + doubleBorrow.get());
        System.out.println("Final available: " + pool.getAvailable());

        boolean passed = !doubleBorrow.get()
                && successfulBorrows.get() == threadCount
                && pool.getAvailable() == poolSize;
        System.out.println("\nCorrectness check: " + (passed ? "PASSED" : "FAILED"));
    }
}

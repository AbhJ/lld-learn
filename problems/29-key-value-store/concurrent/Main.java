/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — 100 threads incrementing same counter key, verify final value = 100

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Key-Value Store Demo ===\n");

        KeyValueStore store = new KeyValueStore();
        int threadCount = 100;
        CountDownLatch startLatch = new CountDownLatch(1);    // CountDownLatch = barrier; threads wait until released
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicBoolean errorFound = new AtomicBoolean(false); // AtomicBoolean = thread-safe flag for error detection

        System.out.println("Scenario: 100 threads each increment the same counter key once.");
        System.out.println("Expected: Final value = 100 (no lost updates).\n");

        for (int t = 0; t < threadCount; t++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    store.increment("counter");
                } catch (Exception e) {
                    errorFound.set(true);
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            }, "Incrementer-" + t).start();
        }

        startLatch.countDown();
        doneLatch.await();

        Long finalValue = store.get("counter");
        long version = store.getVersion("counter");

        System.out.println("--- Results ---");
        System.out.println("Threads: " + threadCount);
        System.out.println("Final counter value: " + finalValue);
        System.out.println("Final version: " + version);
        System.out.println("No errors: " + !errorFound.get());

        boolean passed = finalValue != null && finalValue == threadCount && !errorFound.get();
        System.out.println("\nCorrectness check: " + (passed ? "PASSED" : "FAILED"));
    }
}

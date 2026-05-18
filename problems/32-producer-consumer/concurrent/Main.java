/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — Multiple producers + consumers, verifies no lost items, no duplicates

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Producer-Consumer Demo ===\n");

        demonstrateBoundedBuffer();
        System.out.println();
        demonstrateLockFreeBuffer();
    }

    static void demonstrateBoundedBuffer() throws InterruptedException {
        System.out.println("--- Lock-Based Bounded Buffer (ReentrantLock + Condition) ---");

        int bufferSize = 5;
        int producerCount = 4;
        int consumerCount = 4;
        int itemsPerProducer = 100;
        int totalItems = producerCount * itemsPerProducer;

        BoundedBuffer buffer = new BoundedBuffer(bufferSize);
        Set<Integer> consumedIds = ConcurrentHashMap.newKeySet();
        AtomicInteger duplicates = new AtomicInteger(0);
        CountDownLatch producersDone = new CountDownLatch(producerCount);
        CountDownLatch consumersDone = new CountDownLatch(consumerCount);
        AtomicInteger totalConsumed = new AtomicInteger(0);

        System.out.println("Buffer capacity: " + bufferSize);
        System.out.println("Producers: " + producerCount + " (each produces " + itemsPerProducer + " items)");
        System.out.println("Consumers: " + consumerCount);
        System.out.println("Total items to produce: " + totalItems + "\n");

        // Start producers
        for (int p = 0; p < producerCount; p++) {
            final String name = "Producer-" + p;
            new Thread(() -> {
                try {
                    for (int i = 0; i < itemsPerProducer; i++) {
                        buffer.put(new Item(name));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    producersDone.countDown();
                }
            }, name).start();
        }

        // Start consumers
        for (int c = 0; c < consumerCount; c++) {
            final String name = "Consumer-" + c;
            new Thread(() -> {
                try {
                    while (true) {
                        Item item = buffer.take();
                        if (item == null) break;
                        item.markConsumed(name);
                        if (!consumedIds.add(item.getItemId())) {
                            duplicates.incrementAndGet(); // Duplicate detected!
                        }
                        totalConsumed.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    consumersDone.countDown();
                }
            }, name).start();
        }

        // Wait for all producers to finish, then shutdown
        producersDone.await();
        // Give consumers time to drain the buffer
        while (buffer.size() > 0) {
            Thread.sleep(10);
        }
        buffer.shutdown();
        consumersDone.await();

        System.out.println("Results:");
        System.out.println("  Items produced: " + buffer.getProducedCount());
        System.out.println("  Items consumed: " + totalConsumed.get());
        System.out.println("  Duplicates: " + duplicates.get());
        System.out.println("  Lost items: " + (totalItems - totalConsumed.get()));

        boolean correct = totalConsumed.get() == totalItems && duplicates.get() == 0;
        System.out.println("  Correctness: " + (correct ? "PASSED" : "FAILED") +
                " (no lost items, no duplicates)");
    }

    static void demonstrateLockFreeBuffer() throws InterruptedException {
        System.out.println("--- Lock-Free Ring Buffer (CAS-based) ---");

        int bufferSize = 8; // Must be power of 2
        int producerCount = 4;
        int consumerCount = 4;
        int itemsPerProducer = 100;
        int totalItems = producerCount * itemsPerProducer;

        LockFreeBuffer buffer = new LockFreeBuffer(bufferSize);
        Set<Integer> consumedIds = ConcurrentHashMap.newKeySet();
        AtomicInteger duplicates = new AtomicInteger(0);
        AtomicInteger totalProduced = new AtomicInteger(0);
        AtomicInteger totalConsumed = new AtomicInteger(0);
        CountDownLatch allDone = new CountDownLatch(producerCount + consumerCount);
        AtomicInteger producersDone = new AtomicInteger(0);

        System.out.println("Buffer capacity: " + buffer.getCapacity() + " (power of 2)");
        System.out.println("Producers: " + producerCount + " (each produces " + itemsPerProducer + " items)");
        System.out.println("Consumers: " + consumerCount);
        System.out.println("Total items to produce: " + totalItems + "\n");

        // Start producers
        for (int p = 0; p < producerCount; p++) {
            final String name = "Producer-" + p;
            new Thread(() -> {
                int produced = 0;
                while (produced < itemsPerProducer) {
                    if (buffer.offer(new Item(name))) {
                        produced++;
                        totalProduced.incrementAndGet();
                    } else {
                        Thread.yield(); // Buffer full — yield and retry
                    }
                }
                producersDone.incrementAndGet();
                allDone.countDown();
            }, name).start();
        }

        // Start consumers
        for (int c = 0; c < consumerCount; c++) {
            final String name = "Consumer-" + c;
            final int expected = totalItems;
            new Thread(() -> {
                int emptyPolls = 0;
                while (true) {
                    Item item = buffer.poll();
                    if (item != null) {
                        item.markConsumed(name);
                        if (!consumedIds.add(item.getItemId())) {
                            duplicates.incrementAndGet();
                        }
                        totalConsumed.incrementAndGet();
                        emptyPolls = 0;
                    } else if (producersDone.get() == producerCount
                            && totalConsumed.get() >= expected) {
                        break; // All items produced and consumed
                    } else {
                        emptyPolls++;
                        if (emptyPolls > 1000) {
                            Thread.yield();
                            emptyPolls = 0;
                        }
                    }
                }
                allDone.countDown();
            }, name).start();
        }

        allDone.await();

        System.out.println("Results:");
        System.out.println("  Items produced: " + totalProduced.get());
        System.out.println("  Items consumed: " + totalConsumed.get());
        System.out.println("  Duplicates: " + duplicates.get());
        System.out.println("  Lost items: " + (totalProduced.get() - totalConsumed.get()));

        boolean correct = totalConsumed.get() == totalItems && duplicates.get() == 0;
        System.out.println("  Correctness: " + (correct ? "PASSED" : "FAILED") +
                " (no lost items, no duplicates)");
    }
}

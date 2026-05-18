/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Demonstrates lock-free ring buffer with batch producer/consumer
public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Producer-Consumer (Optimized: Lock-Free Ring Buffer) Demo ===\n");

        // --- Test 1: Single Producer/Consumer with CAS Ring Buffer ---
        System.out.println("--- Test 1: Lock-Free Single Producer/Consumer ---");
        LockFreeRingBuffer buffer1 = new LockFreeRingBuffer(16);
        BatchProducer p1 = new BatchProducer("Producer-1", buffer1, 10, 3);
        BatchConsumer c1 = new BatchConsumer("Consumer-1", buffer1, 10, 3);

        Thread pt1 = new Thread(p1, "Producer-1");
        Thread ct1 = new Thread(c1, "Consumer-1");
        ct1.setDaemon(true);
        pt1.setDaemon(true);
        ct1.start();
        pt1.start();
        pt1.join(3000);
        ct1.join(3000);

        // --- Test 2: Multi-Producer / Multi-Consumer ---
        System.out.println("\n--- Test 2: Multi-Producer / Multi-Consumer ---");
        LockFreeRingBuffer buffer2 = new LockFreeRingBuffer(32);
        int itemsPerActor = 10;

        BatchProducer pa = new BatchProducer("ProdA", buffer2, itemsPerActor, 4);
        BatchProducer pb = new BatchProducer("ProdB", buffer2, itemsPerActor, 4);
        BatchConsumer cx = new BatchConsumer("ConsX", buffer2, itemsPerActor, 4);
        BatchConsumer cy = new BatchConsumer("ConsY", buffer2, itemsPerActor, 4);

        Thread[] threads = {
            new Thread(pa), new Thread(pb), new Thread(cx), new Thread(cy)
        };
        for (Thread t : threads) { t.setDaemon(true); t.start(); }
        for (Thread t : threads) { t.join(5000); }

        // --- Test 3: Throughput Comparison ---
        System.out.println("\n--- Test 3: High-Throughput Batch Test ---");
        LockFreeRingBuffer buffer3 = new LockFreeRingBuffer(1024);
        int total = 5000;
        long start = System.currentTimeMillis();

        BatchProducer fastProd = new BatchProducer("FastProd", buffer3, total, 50);
        BatchConsumer fastCons = new BatchConsumer("FastCons", buffer3, total, 50);
        Thread fp = new Thread(fastProd); fp.setDaemon(true);
        Thread fc = new Thread(fastCons); fc.setDaemon(true);
        fc.start();
        fp.start();
        fp.join(5000);
        fc.join(5000);

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("  " + total + " items in " + elapsed + "ms");
        System.out.println("  Throughput: ~" + (total * 1000L / Math.max(1, elapsed)) + " items/sec");

        System.out.println("\n=== Producer-Consumer (Optimized) Demo Complete ===");
    }
}

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/BatchProducer.java — Produces items in batches to reduce per-item overhead
public class BatchProducer implements Runnable { // Runnable = can be executed by a Thread
    private final String name;
    private final LockFreeRingBuffer buffer; // CAS-based ring buffer = no locks on hot path
    private final int totalItems;
    private final int batchSize;             // batching amortizes CAS cost over multiple items
    private volatile boolean running = true;  // volatile = cancellation flag visible to all threads

    // WHY batch: Amortizes the CAS cost over multiple items,
    // improving throughput by reducing per-item atomic operations
    public BatchProducer(String name, LockFreeRingBuffer buffer, int totalItems, int batchSize) {
        this.name = name;
        this.buffer = buffer;
        this.totalItems = totalItems;
        this.batchSize = batchSize;
    }

    @Override
    public void run() {
        int produced = 0;
        try {
            while (produced < totalItems && running) {
                int batchEnd = Math.min(produced + batchSize, totalItems);
                // WHY: Produce a batch without yielding between items
                for (int i = produced; i < batchEnd; i++) {
                    Item item = new Item(name + "-" + (i + 1), "batch-data");
                    // Spin-wait if buffer is full (backpressure)
                    while (!buffer.offer(item) && running) {
                        Thread.yield();
                    }
                }
                produced = batchEnd;
                System.out.println("  " + name + " produced batch, total=" + produced);
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("  " + name + " done. Produced: " + produced);
    }

    public void stop() { running = false; }
    public String getName() { return name; }
}

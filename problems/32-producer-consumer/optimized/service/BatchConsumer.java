/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/BatchConsumer.java — Consumes items in batches for higher throughput
public class BatchConsumer implements Runnable { // Runnable = can be submitted to a Thread
    private final String name;
    private final LockFreeRingBuffer buffer; // ring buffer = fixed memory, no GC pressure
    private final int totalItems;
    private final int batchSize;             // batch draining reduces per-item CAS overhead
    private volatile boolean running = true;  // volatile = stop signal visible across threads
    private int consumed = 0;

    // WHY batch consume: Reduces contention by draining multiple items
    // per wake cycle rather than one-at-a-time with context switches
    public BatchConsumer(String name, LockFreeRingBuffer buffer, int totalItems, int batchSize) {
        this.name = name;
        this.buffer = buffer;
        this.totalItems = totalItems;
        this.batchSize = batchSize;
    }

    @Override
    public void run() {
        try {
            while (consumed < totalItems && running) {
                int batchConsumed = 0;
                // WHY: Drain up to batchSize in a tight loop without sleeping
                while (batchConsumed < batchSize && consumed < totalItems) {
                    Item item = buffer.poll();
                    if (item != null) {
                        consumed++;
                        batchConsumed++;
                    } else {
                        // Nothing available — brief yield instead of blocking
                        if (batchConsumed > 0) break;
                        Thread.yield();
                    }
                }
                if (batchConsumed > 0) {
                    System.out.println("  " + name + " consumed batch of " + batchConsumed + ", total=" + consumed);
                }
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("  " + name + " done. Consumed: " + consumed);
    }

    public void stop() { running = false; }
    public String getName() { return name; }
    public int getConsumed() { return consumed; }
}

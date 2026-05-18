/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/LockFreeBuffer.java — Lock-free buffer using ConcurrentLinkedQueue with capacity control

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * "Lock-free" bounded buffer using ConcurrentLinkedQueue (Michael-Scott queue)
 * with an AtomicInteger size counter for bounded capacity.
 *
 * ConcurrentLinkedQueue is a proven lock-free data structure (uses CAS internally).
 * We add bounded capacity via AtomicInteger to demonstrate backpressure.
 *
 * This is the pragmatic approach to lock-free producer/consumer in Java.
 * The pure ring-buffer CAS approach (Disruptor-style) is extremely complex
 * to get right and is typically only used in ultra-low-latency systems.
 */
class LockFreeBuffer {
    private final ConcurrentLinkedQueue<Item> queue = new ConcurrentLinkedQueue<>(); // ConcurrentLinkedQueue = lock-free Michael-Scott queue
    private final AtomicInteger size = new AtomicInteger(0);    // AtomicInteger = CAS-based capacity enforcement without locks
    private final int capacity;
    private final AtomicInteger produced = new AtomicInteger(0);
    private final AtomicInteger consumed = new AtomicInteger(0);
    private volatile boolean shutdown = false;                   // volatile = all threads see shutdown flag immediately

    public LockFreeBuffer(int capacity) {
        this.capacity = capacity;
    }

    /**
     * Non-blocking offer. Returns false if buffer is at capacity.
     * Uses CAS on the size counter to enforce bounds without locks.
     */
    public boolean offer(Item item) {
        if (shutdown) return false;
        while (true) {
            int current = size.get();
            if (current >= capacity) {
                return false; // Buffer full — backpressure
            }
            if (size.compareAndSet(current, current + 1)) {
                queue.offer(item);
                produced.incrementAndGet();
                return true;
            }
            // CAS failed — another producer incremented, retry
        }
    }

    /**
     * Non-blocking poll. Returns null if buffer is empty.
     */
    public Item poll() {
        Item item = queue.poll();
        if (item != null) {
            size.decrementAndGet();
            consumed.incrementAndGet();
        }
        return item;
    }

    public void shutdown() { shutdown = true; }
    public int getProducedCount() { return produced.get(); }
    public int getConsumedCount() { return consumed.get(); }
    public int getCapacity() { return capacity; }
    public boolean isShutdown() { return shutdown; }

    public int approximateSize() {
        return size.get();
    }
}

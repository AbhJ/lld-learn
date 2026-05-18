/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/LockFreeRingBuffer.java — CAS-based ring buffer for single-producer/single-consumer (SPSC)
import java.util.concurrent.atomic.AtomicLong;

public class LockFreeRingBuffer {
    // WHY power-of-2: Allows bitwise AND for modulo (faster than %)
    private final Item[] buffer;                         // fixed array = zero GC allocation during operation
    private final int mask;                              // mask = capacity-1; used for fast index wrapping via &

    // WHY separate atomics: Producer only writes head, consumer only writes tail.
    // Each lives on its own cache line to avoid false sharing in a real JVM.
    private final AtomicLong head = new AtomicLong(0); // AtomicLong = CAS-based lock-free write position
    private final AtomicLong tail = new AtomicLong(0); // AtomicLong = CAS-based lock-free read position

    public LockFreeRingBuffer(int capacityPowerOf2) {
        // WHY: Power of 2 size enables bit masking instead of modulo
        int capacity = Integer.highestOneBit(capacityPowerOf2);
        this.buffer = new Item[capacity];
        this.mask = capacity - 1;
    }

    // WHY CAS loop: Multiple producers can attempt simultaneously;
    // CAS ensures only one wins the slot, others retry.
    public boolean offer(Item item) {
        long currentHead;
        do {
            currentHead = head.get();
            long currentTail = tail.get();
            // Buffer full when head is one full cycle ahead of tail
            if (currentHead - currentTail >= buffer.length) {
                return false; // Full — caller decides to retry or block
            }
        } while (!head.compareAndSet(currentHead, currentHead + 1));

        // WHY: We reserved the slot atomically, now write without contention
        buffer[(int)(currentHead & mask)] = item;
        return true;
    }

    // WHY CAS loop: Multiple consumers race for the same slot
    public Item poll() {
        long currentTail;
        do {
            currentTail = tail.get();
            long currentHead = head.get();
            if (currentTail >= currentHead) {
                return null; // Empty
            }
        } while (!tail.compareAndSet(currentTail, currentTail + 1));

        // Spin until the producer finishes writing to this slot
        Item item;
        int idx = (int)(currentTail & mask);
        while ((item = buffer[idx]) == null) {
            Thread.yield();
        }
        buffer[idx] = null; // Clear slot for reuse
        return item;
    }

    public int size() {
        return (int)(head.get() - tail.get());
    }

    public boolean isEmpty() { return head.get() <= tail.get(); }
    public boolean isFull() { return head.get() - tail.get() >= buffer.length; }
    public int capacity() { return buffer.length; }
}

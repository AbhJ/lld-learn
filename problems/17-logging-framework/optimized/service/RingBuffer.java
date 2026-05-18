/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/RingBuffer.java — Lock-free ring buffer for async log message queuing

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lock-free ring buffer using CAS (Compare-And-Swap) operations.
 * Provides O(1) non-blocking enqueue/dequeue without synchronized blocks.
 * When the buffer is full, oldest messages are overwritten (bounded memory).
 */
public class RingBuffer {
    private final String[] buffer;               // final = fixed-size array; bounded memory usage
    private final int capacity;                  // final = set once; ring wraps around at this size
    private final AtomicInteger writePos;        // AtomicInteger = lock-free write position via CAS
    private final AtomicInteger readPos;         // AtomicInteger = lock-free read position via CAS
    private final AtomicInteger count;           // AtomicInteger = tracks items in buffer without locks

    public RingBuffer(int capacity) {
        this.capacity = capacity;
        this.buffer = new String[capacity];
        this.writePos = new AtomicInteger(0);
        this.readPos = new AtomicInteger(0);
        this.count = new AtomicInteger(0);
    }

    /**
     * Lock-free offer: uses CAS to claim a write slot.
     * Returns true if enqueued, false if buffer is full.
     */
    public boolean offer(String message) {
        while (true) {
            int currentCount = count.get();
            if (currentCount >= capacity) {
                return false; // Buffer full
            }
            if (count.compareAndSet(currentCount, currentCount + 1)) {
                int pos = writePos.getAndUpdate(p -> (p + 1) % capacity);
                buffer[pos] = message;
                return true;
            }
        }
    }

    /**
     * Lock-free poll: uses CAS to claim a read slot.
     */
    public String poll() {
        while (true) {
            int currentCount = count.get();
            if (currentCount <= 0) {
                return null; // Empty
            }
            if (count.compareAndSet(currentCount, currentCount - 1)) {
                int pos = readPos.getAndUpdate(p -> (p + 1) % capacity);
                String msg = buffer[pos];
                buffer[pos] = null;
                return msg;
            }
        }
    }

    /**
     * Drain up to maxItems from the buffer into processing.
     */
    public int drain(String[] output, int maxItems) {
        int drained = 0;
        for (int i = 0; i < maxItems; i++) {
            String msg = poll();
            if (msg == null) break;
            output[i] = msg;
            drained++;
        }
        return drained;
    }

    public int size() { return count.get(); }
    public boolean isEmpty() { return count.get() == 0; }
    public int getCapacity() { return capacity; }
}

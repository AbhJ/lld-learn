/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/BoundedBuffer.java — Using ReentrantLock + Condition (await/signal)

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe bounded buffer using explicit lock + conditions.
 *
 * - Producers block when buffer is FULL (notFull condition)
 * - Consumers block when buffer is EMPTY (notEmpty condition)
 * - No busy-waiting — threads are parked and signaled efficiently
 */
class BoundedBuffer {
    private final Queue<Item> buffer;
    private final int capacity;
    private final ReentrantLock lock = new ReentrantLock();       // ReentrantLock = explicit lock; more flexible than synchronized
    private final Condition notFull = lock.newCondition();        // Condition = producers wait here when buffer is full
    private final Condition notEmpty = lock.newCondition();       // Condition = consumers wait here when buffer is empty
    private final AtomicInteger produced = new AtomicInteger(0); // AtomicInteger = lock-free counter for stats
    private final AtomicInteger consumed = new AtomicInteger(0);
    private volatile boolean shutdown = false;                    // volatile = shutdown visible to all blocked threads

    public BoundedBuffer(int capacity) {
        this.capacity = capacity;
        this.buffer = new LinkedList<>();
    }

    /**
     * Put an item into the buffer. Blocks if buffer is full.
     */
    public boolean put(Item item) throws InterruptedException {
        lock.lock();
        try {
            while (buffer.size() == capacity && !shutdown) {
                notFull.await(); // Wait until space available
            }
            if (shutdown) return false;
            buffer.offer(item);
            produced.incrementAndGet();
            notEmpty.signal(); // Wake one waiting consumer
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Take an item from the buffer. Blocks if buffer is empty.
     */
    public Item take() throws InterruptedException {
        lock.lock();
        try {
            while (buffer.isEmpty() && !shutdown) {
                notEmpty.await(); // Wait until item available
            }
            if (buffer.isEmpty()) return null; // Shutdown with empty buffer
            Item item = buffer.poll();
            consumed.incrementAndGet();
            notFull.signal(); // Wake one waiting producer
            return item;
        } finally {
            lock.unlock();
        }
    }

    public void shutdown() {
        lock.lock();
        try {
            shutdown = true;
            notFull.signalAll();
            notEmpty.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            return buffer.size();
        } finally {
            lock.unlock();
        }
    }

    public int getProducedCount() { return produced.get(); }
    public int getConsumedCount() { return consumed.get(); }
    public boolean isShutdown() { return shutdown; }
    public int getCapacity() { return capacity; }
}

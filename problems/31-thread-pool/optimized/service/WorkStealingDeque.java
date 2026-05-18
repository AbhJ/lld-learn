/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/WorkStealingDeque.java — Lock-free per-worker deque supporting push/pop (owner) and steal (thieves)
import java.util.concurrent.atomic.AtomicInteger;

public class WorkStealingDeque {
    // WHY: Per-worker deques eliminate contention on a single shared queue.
    // The owner pushes/pops from the bottom (no lock needed for LIFO locality),
    // while thieves steal from the top using CAS to avoid locks.
    private final Task[] buffer;                                // fixed-size array = no allocation during runtime
    private final AtomicInteger top = new AtomicInteger(0);    // AtomicInteger = thieves CAS here to steal safely
    private volatile int bottom = 0;                            // volatile = owner writes, stealers read; no CAS needed

    public WorkStealingDeque(int capacity) {
        this.buffer = new Task[capacity];
    }

    // Called only by the owning worker thread — no synchronization needed for bottom
    public void push(Task task) {
        int b = bottom;
        buffer[b % buffer.length] = task;
        bottom = b + 1;
    }

    // Called only by the owning worker — pops from bottom (LIFO for cache locality)
    public Task pop() {
        int b = bottom - 1;
        bottom = b;
        int t = top.get();

        if (t <= b) {
            Task task = buffer[b % buffer.length];
            if (t == b) {
                // Last element — compete with stealers via CAS
                if (!top.compareAndSet(t, t + 1)) {
                    // Lost race to stealer
                    bottom = t + 1;
                    return null;
                }
                bottom = t + 1;
            }
            return task;
        } else {
            // Deque is empty
            bottom = t;
            return null;
        }
    }

    // Called by other workers — steals from top (FIFO to preserve fairness)
    public Task steal() {
        int t = top.get();
        int b = bottom;
        if (t < b) {
            Task task = buffer[t % buffer.length];
            // WHY CAS: Multiple thieves may race; only one wins without locks
            if (top.compareAndSet(t, t + 1)) {
                return task;
            }
        }
        return null;
    }

    public int size() {
        return Math.max(0, bottom - top.get());
    }

    public boolean isEmpty() {
        return top.get() >= bottom;
    }
}

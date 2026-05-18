/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/ObjectPool.java — Semaphore for capacity + ConcurrentLinkedDeque for idle objects

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ObjectPool {
    private final ConcurrentLinkedDeque<PooledObject> idle; // ConcurrentLinkedDeque = lock-free; threads poll/offer without blocking each other
    private final Semaphore semaphore;                      // Semaphore = limits concurrent borrows to capacity; threads block when exhausted
    private final int capacity;                             // final = pool size fixed at creation
    private final AtomicInteger borrowCount = new AtomicInteger(0); // AtomicInteger = lock-free stats counter
    private final AtomicInteger returnCount = new AtomicInteger(0);

    public ObjectPool(int capacity) {
        this.capacity = capacity;
        this.semaphore = new Semaphore(capacity);
        this.idle = new ConcurrentLinkedDeque<>();

        for (int i = 0; i < capacity; i++) {
            idle.offer(new PooledObject(i));
        }
    }

    /**
     * Borrow an object from the pool. Blocks if pool is exhausted.
     * Semaphore ensures no more than 'capacity' objects are borrowed simultaneously.
     */
    public PooledObject borrow(long timeoutMs) throws InterruptedException {
        if (!semaphore.tryAcquire(timeoutMs, TimeUnit.MILLISECONDS)) {
            return null; // timed out
        }
        PooledObject obj = idle.poll();
        if (obj != null) {
            obj.acquire();
            borrowCount.incrementAndGet();
            return obj;
        }
        // Should not happen if semaphore is correctly managed
        semaphore.release();
        return null;
    }

    /**
     * Return an object to the pool.
     */
    public void returnObject(PooledObject obj) {
        if (obj == null) return;
        obj.release();
        idle.offer(obj);
        returnCount.incrementAndGet();
        semaphore.release();
    }

    public int getAvailable() { return semaphore.availablePermits(); }
    public int getCapacity() { return capacity; }
    public int getBorrowCount() { return borrowCount.get(); }
    public int getReturnCount() { return returnCount.get(); }
}

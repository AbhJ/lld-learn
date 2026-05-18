/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/ObjectPool.java — ConcurrentLinkedDeque (LIFO) + Semaphore bounding + background evictor
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class ObjectPool<T> {
    // WHY ConcurrentLinkedDeque: Lock-free LIFO access. Most-recently-returned objects
    // are borrowed first, keeping them cache-warm and reducing stale connections.
    private final ConcurrentLinkedDeque<PooledObject<T>> idle = new ConcurrentLinkedDeque<>(); // ConcurrentLinkedDeque = lock-free LIFO; recently-returned objects reused first

    // WHY Semaphore: Bounds total objects without synchronized. Threads block on acquire()
    // when pool is exhausted — no busy-spinning or exception throwing needed.
    private final Semaphore permits; // Semaphore = bounds total objects; threads block when exhausted

    private final ObjectFactory<T> factory;
    private final PoolConfig config;
    private final AtomicInteger totalCreated = new AtomicInteger(0); // AtomicInteger = lock-free counter for stats
    private final AtomicInteger activeCount = new AtomicInteger(0);
    private volatile boolean running = true;       // volatile = evictor thread sees shutdown immediately
    private final Thread evictorThread;            // background daemon for periodic idle eviction

    public ObjectPool(ObjectFactory<T> factory, PoolConfig config) {
        this.factory = factory;
        this.config = config;
        this.permits = new Semaphore(config.getMaxSize());

        // Pre-create minimum objects
        for (int i = 0; i < config.getMinSize(); i++) {
            idle.push(new PooledObject<>(factory.create()));
            totalCreated.incrementAndGet();
            permits.acquireUninterruptibly();
        }

        // WHY background evictor: Periodically removes idle objects exceeding maxIdleTime,
        // keeping resource usage bounded without manual intervention.
        evictorThread = new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(config.getEvictionIntervalMs());
                    evict();
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "pool-evictor");
        evictorThread.setDaemon(true);
        evictorThread.start();
    }

    public T borrow() throws InterruptedException {
        // WHY: Semaphore blocks if all permits taken — natural backpressure
        permits.acquire();
        activeCount.incrementAndGet();

        // Try to get from idle stack (LIFO for cache locality)
        PooledObject<T> po;
        while ((po = idle.pollFirst()) != null) {
            if (factory.validate(po.getObject())) {
                return po.getObject();
            }
            // Invalid — discard and create new
            factory.destroy(po.getObject());
        }

        // No idle object available, create new one
        T obj = factory.create();
        totalCreated.incrementAndGet();
        return obj;
    }

    public void returnObject(T obj) {
        if (factory.validate(obj)) {
            PooledObject<T> po = new PooledObject<>(obj);
            // WHY pushFirst: LIFO ensures recently-used objects are borrowed next
            idle.push(po);
        } else {
            factory.destroy(obj);
        }
        activeCount.decrementAndGet();
        permits.release();
    }

    private void evict() {
        int currentIdle = idle.size();
        int toEvict = currentIdle - config.getMinSize();
        if (toEvict <= 0) return;

        int evicted = 0;
        // WHY pollLast: Evict from the tail (oldest/least-recently-returned)
        while (evicted < toEvict) {
            PooledObject<T> po = idle.pollLast();
            if (po == null) break;
            if (po.getIdleTimeMs() > config.getMaxIdleTimeMs()) {
                factory.destroy(po.getObject());
                permits.release(); // Return the permit for the destroyed object
                evicted++;
            } else {
                idle.addLast(po); // Not yet expired, put back
                break;
            }
        }
        if (evicted > 0) {
            System.out.println("    [Evictor] Evicted " + evicted + " idle objects");
        }
    }

    public void shutdown() {
        running = false;
        evictorThread.interrupt();
        PooledObject<T> po;
        while ((po = idle.poll()) != null) factory.destroy(po.getObject());
    }

    public int getIdleCount() { return idle.size(); }
    public int getActiveCount() { return activeCount.get(); }
    public int getTotalCreated() { return totalCreated.get(); }
}

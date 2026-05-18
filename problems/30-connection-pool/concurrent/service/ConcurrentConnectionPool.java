/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/ConcurrentConnectionPool.java — Semaphore for bounding, BlockingQueue for idle connections

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe connection pool using:
 * - Semaphore: bounds the total number of connections that can be acquired
 * - BlockingQueue: efficiently hands out idle connections (threads block if none available)
 * - AtomicInteger: tracks pool statistics without locks
 */
class ConcurrentConnectionPool {
    private final int maxSize;                  // final = immutable config; safe for all threads to read
    private final long timeoutMillis;           // final = immutable; no synchronization needed
    private final Semaphore semaphore;          // Semaphore = bounds concurrent access; fair=true for FIFO ordering
    private final BlockingQueue<Connection> idleConnections; // BlockingQueue = thread-safe; threads block when empty
    private final AtomicInteger totalCreated = new AtomicInteger(0); // AtomicInteger = lock-free counter
    private final AtomicInteger activeCount = new AtomicInteger(0);  // AtomicInteger = thread-safe active tracking
    private final AtomicInteger waitCount = new AtomicInteger(0);
    private final AtomicInteger timeoutCount = new AtomicInteger(0);
    private volatile boolean shutdown = false;  // volatile = shutdown flag visible to all threads immediately

    public ConcurrentConnectionPool(int maxSize, long timeoutMillis) {
        this.maxSize = maxSize;
        this.timeoutMillis = timeoutMillis;
        this.semaphore = new Semaphore(maxSize, true); // fair=true ensures FIFO; longest-waiting thread acquires first
        this.idleConnections = new LinkedBlockingQueue<>();

        // Pre-create all connections
        for (int i = 0; i < maxSize; i++) {
            Connection conn = new Connection();
            idleConnections.offer(conn);
            totalCreated.incrementAndGet();
        }
    }

    /**
     * Acquire a connection from the pool.
     * Blocks up to timeoutMillis if all connections are in use.
     * Returns null if timeout expires (pool exhaustion).
     */
    public Connection acquire() throws InterruptedException {
        if (shutdown) throw new IllegalStateException("Pool is shut down");

        waitCount.incrementAndGet();
        try {
            if (!semaphore.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS)) { // tryAcquire = blocks up to timeout; no busy-wait
                timeoutCount.incrementAndGet();
                return null; // Pool exhausted — timeout
            }
        } finally {
            waitCount.decrementAndGet();
        }

        Connection conn = idleConnections.poll();
        if (conn == null || conn.isClosed()) {
            // Create a new connection if needed
            conn = new Connection();
            totalCreated.incrementAndGet();
        }
        conn.acquire();
        activeCount.incrementAndGet();
        return conn;
    }

    /**
     * Return a connection to the pool.
     */
    public void release(Connection conn) {
        if (conn == null) return;
        conn.release();
        activeCount.decrementAndGet();
        if (!shutdown) {
            idleConnections.offer(conn);
        } else {
            conn.close();
        }
        semaphore.release();
    }

    public void shutdown() {
        shutdown = true;
        Connection conn;
        while ((conn = idleConnections.poll()) != null) {
            conn.close();
        }
    }

    public int getMaxSize() { return maxSize; }
    public int getActiveCount() { return activeCount.get(); }
    public int getIdleCount() { return idleConnections.size(); }
    public int getWaitCount() { return waitCount.get(); }
    public int getTimeoutCount() { return timeoutCount.get(); }
    public int getTotalCreated() { return totalCreated.get(); }
}

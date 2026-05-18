/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/RaceConditionDemo.java — Shows connection leak bug when thread dies without returning connection

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * RACE CONDITION: Pool exhaustion due to connection leaks.
 *
 * BUG: If a thread acquires a connection but crashes/throws before returning it,
 * the connection is permanently lost from the pool. Eventually the pool is exhausted.
 *
 * FIX: Use try-finally to guarantee connection return, or implement
 * a reaper thread that reclaims connections held too long.
 */
public class RaceConditionDemo {

    // ===== BUGGY VERSION: No try-finally — connection leak on exception =====
    static class BuggyWorker implements Runnable { // implements Runnable = can be executed by a Thread
        private final ConcurrentConnectionPool pool; // final = safe publication; reference never changes
        private final int id;
        private final AtomicInteger leaks;           // AtomicInteger = thread-safe leak counter

        BuggyWorker(ConcurrentConnectionPool pool, int id, AtomicInteger leaks) {
            this.pool = pool;
            this.id = id;
            this.leaks = leaks;
        }

        @Override
        public void run() {
            try {
                Connection conn = pool.acquire();
                if (conn == null) return;

                // BUG: If this throws, connection is never returned!
                if (id % 3 == 0) {
                    throw new RuntimeException("Simulated crash in thread " + id);
                }

                // Only reached if no exception
                pool.release(conn);
            } catch (RuntimeException e) {
                leaks.incrementAndGet();
                // Connection leaked! Never returned to pool.
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // ===== FIXED VERSION: try-finally guarantees connection return =====
    static class FixedWorker implements Runnable { // implements Runnable = can be executed by a Thread
        private final ConcurrentConnectionPool pool; // final = safe publication; reference never changes
        private final int id;

        FixedWorker(ConcurrentConnectionPool pool, int id) {
            this.pool = pool;
            this.id = id;
        }

        @Override
        public void run() {
            Connection conn = null;
            try {
                conn = pool.acquire();
                if (conn == null) return;

                // Even if this throws, finally block returns the connection
                if (id % 3 == 0) {
                    throw new RuntimeException("Simulated crash in thread " + id);
                }

                conn.execute("SELECT 1");
            } catch (RuntimeException e) {
                // Exception handled, but connection still safe
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                // FIX: Always return connection, even on exception
                if (conn != null) {
                    pool.release(conn);
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Race Condition Demo: Connection Pool Leak ===\n");
        demonstrateBug();
        System.out.println();
        demonstrateFix();
    }

    static void demonstrateBug() throws InterruptedException {
        System.out.println("--- BUGGY VERSION (no try-finally) ---");
        System.out.println("Threads crash without returning connections.\n");

        ConcurrentConnectionPool pool = new ConcurrentConnectionPool(5, 200);
        AtomicInteger leaks = new AtomicInteger(0);
        int threadCount = 15;

        CountDownLatch done = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            final int id = i;
            new Thread(() -> {
                new BuggyWorker(pool, id, leaks).run();
                done.countDown();
            }).start();
        }
        done.await();

        System.out.println("Pool size: 5");
        System.out.println("Threads that crashed: " + leaks.get());
        System.out.println("Connections leaked (never returned): " + leaks.get());
        System.out.println("Pool active connections: " + pool.getActiveCount());
        System.out.println("Pool idle connections: " + pool.getIdleCount());
        System.out.println("BUG: " + leaks.get() + " connections permanently lost from pool!");
        System.out.println("Eventually, all connections leak and pool becomes unusable.");
        pool.shutdown();
    }

    static void demonstrateFix() throws InterruptedException {
        System.out.println("--- FIXED VERSION (try-finally guarantees return) ---");
        System.out.println("Threads crash but connections are always returned.\n");

        ConcurrentConnectionPool pool = new ConcurrentConnectionPool(5, 200);
        int threadCount = 15;

        CountDownLatch done = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            final int id = i;
            new Thread(() -> {
                new FixedWorker(pool, id).run();
                done.countDown();
            }).start();
        }
        done.await();

        System.out.println("Pool size: 5");
        System.out.println("Pool active connections: " + pool.getActiveCount());
        System.out.println("Pool idle connections: " + pool.getIdleCount());
        boolean healthy = pool.getActiveCount() == 0 && pool.getIdleCount() == 5;
        System.out.println("FIX VERIFIED: All connections returned despite crashes. Pool healthy: " + healthy);
        pool.shutdown();
    }
}

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Demonstrates object pool with ConcurrentLinkedDeque, Semaphore, and background evictor
public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("=== Object Pool (Optimized: Lock-Free + Semaphore) Demo ===\n");
        DatabaseConnection.resetCounter();

        PoolConfig config = new PoolConfig(2, 5, 500, 300);
        ObjectPool<DatabaseConnection> pool = new ObjectPool<>(new DatabaseConnectionFactory(), config);

        // --- Test 1: Borrow and Return ---
        System.out.println("--- Test 1: Borrow and Return ---");
        DatabaseConnection c1 = pool.borrow();
        DatabaseConnection c2 = pool.borrow();
        System.out.println("  Borrowed: " + c1 + ", " + c2);
        System.out.println("  " + c1.executeQuery("SELECT 1"));
        pool.returnObject(c1);
        pool.returnObject(c2);
        System.out.println("  Idle: " + pool.getIdleCount());

        // --- Test 2: LIFO Reuse (cache locality) ---
        System.out.println("\n--- Test 2: LIFO Reuse ---");
        DatabaseConnection reused = pool.borrow();
        System.out.println("  Got: " + reused.getId() + " (most recently returned)");
        pool.returnObject(reused);

        // --- Test 3: Semaphore Bounding ---
        System.out.println("\n--- Test 3: Semaphore Bounding (max=5) ---");
        DatabaseConnection[] all = new DatabaseConnection[5];
        for (int i = 0; i < 5; i++) all[i] = pool.borrow();
        System.out.println("  All 5 borrowed. Active: " + pool.getActiveCount());

        // Try borrow in separate thread — will block until return
        Thread blocked = new Thread(() -> {
            try {
                System.out.println("  [Blocked thread] Waiting for permit...");
                DatabaseConnection c = pool.borrow();
                System.out.println("  [Blocked thread] Got: " + c.getId());
                pool.returnObject(c);
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        blocked.start();
        Thread.sleep(100);
        pool.returnObject(all[0]); // Releases permit, unblocks waiter
        blocked.join(2000);
        for (int i = 1; i < 5; i++) pool.returnObject(all[i]);

        // --- Test 4: Background Eviction ---
        System.out.println("\n--- Test 4: Background Eviction ---");
        System.out.println("  Idle before wait: " + pool.getIdleCount());
        Thread.sleep(900); // Evictor runs every 300ms, idle timeout 500ms
        System.out.println("  Idle after eviction: " + pool.getIdleCount() + " (min=2 maintained)");

        // --- Test 5: Concurrent Access ---
        System.out.println("\n--- Test 5: Concurrent Access ---");
        Thread[] threads = new Thread[8];
        for (int i = 0; i < 8; i++) {
            final int id = i;
            threads[i] = new Thread(() -> {
                try {
                    DatabaseConnection c = pool.borrow();
                    c.executeQuery("SELECT " + id);
                    Thread.sleep(50);
                    pool.returnObject(c);
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            });
        }
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
        System.out.println("  All 8 threads served. Total created: " + pool.getTotalCreated());

        pool.shutdown();
        System.out.println("\n=== Object Pool (Optimized) Demo Complete ===");
    }
}

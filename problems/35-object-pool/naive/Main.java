/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Demonstrates object pool with synchronized ArrayList
public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Object Pool (Naive) Demo ===\n");
        DatabaseConnection.resetCounter();

        PoolConfig config = new PoolConfig(2, 5, 500);
        ObjectPool<DatabaseConnection> pool = new ObjectPool<>(
                new DatabaseConnectionFactory(), new ConnectionValidator(), config);

        // --- Test 1: Borrow and Return ---
        System.out.println("--- Test 1: Borrow and Return ---");
        DatabaseConnection c1 = pool.borrow();
        DatabaseConnection c2 = pool.borrow();
        System.out.println("  Borrowed: " + c1 + ", " + c2);
        System.out.println("  Query: " + c1.executeQuery("SELECT 1"));
        pool.returnObject(c1);
        pool.returnObject(c2);
        System.out.println("  Idle: " + pool.getIdleCount());

        // --- Test 2: Reuse ---
        System.out.println("\n--- Test 2: Object Reuse ---");
        DatabaseConnection reused = pool.borrow();
        System.out.println("  Reused: " + reused.getId());
        pool.returnObject(reused);

        // --- Test 3: Pool Exhaustion ---
        System.out.println("\n--- Test 3: Pool Exhaustion ---");
        DatabaseConnection[] all = new DatabaseConnection[5];
        for (int i = 0; i < 5; i++) all[i] = pool.borrow();
        try { pool.borrow(); } catch (RuntimeException e) { System.out.println("  " + e.getMessage()); }
        for (DatabaseConnection c : all) pool.returnObject(c);

        // --- Test 4: Eviction ---
        System.out.println("\n--- Test 4: Eviction ---");
        System.out.println("  Idle before: " + pool.getIdleCount());
        Thread.sleep(600);
        int evicted = pool.evict();
        System.out.println("  Evicted: " + evicted + ", Idle after: " + pool.getIdleCount());

        System.out.println("\n=== Object Pool (Naive) Demo Complete ===");
    }
}

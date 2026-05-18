/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Demonstrates thread pool with basic synchronized queue approach
public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Thread Pool (Naive) Demo ===\n");

        // --- Test 1: Basic Task Execution ---
        System.out.println("--- Test 1: Basic Task Execution ---");
        PoolConfig config = new PoolConfig(3, 5, 10, 5000);
        ThreadPool pool = new ThreadPool(config);

        for (int i = 1; i <= 5; i++) {
            final int taskId = i;
            pool.submit(new Task() {
                @Override public String getName() { return "Task-" + taskId; }
                @Override public void execute() {
                    System.out.println("  " + getName() + " executed by " + Thread.currentThread().getName());
                    try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                }
            });
        }
        Thread.sleep(800);
        pool.shutdownNow();

        // --- Test 2: Rejection Policies ---
        System.out.println("\n--- Test 2: Rejection Policies ---");
        Task slowTask = new Task() {
            @Override public String getName() { return "SlowTask"; }
            @Override public void execute() {
                try { Thread.sleep(5000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        };

        ThreadPool abortPool = new ThreadPool(new PoolConfig(1, 1, 2, 1000));
        abortPool.setRejectionPolicy(new AbortPolicy());
        abortPool.submit(slowTask);
        Thread.sleep(50);
        abortPool.submit(slowTask);
        abortPool.submit(slowTask);
        try {
            abortPool.submit(slowTask);
        } catch (RuntimeException e) {
            System.out.println("  " + e.getMessage());
        }
        abortPool.shutdownNow();

        // --- Test 3: Pool Statistics ---
        System.out.println("\n--- Test 3: Pool Statistics ---");
        ThreadPool statsPool = new ThreadPool(new PoolConfig(3, 5, 20, 5000));
        for (int i = 1; i <= 10; i++) {
            final int id = i;
            statsPool.submit(new Task() {
                @Override public String getName() { return "StatsTask-" + id; }
                @Override public void execute() {
                    try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                }
            });
        }
        Thread.sleep(100);
        System.out.println("  Mid-execution: " + statsPool.getStats());
        Thread.sleep(600);
        System.out.println("  After completion: " + statsPool.getStats());
        statsPool.shutdownNow();

        System.out.println("\n=== Thread Pool (Naive) Demo Complete ===");
    }
}

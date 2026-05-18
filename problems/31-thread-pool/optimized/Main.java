/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Demonstrates work-stealing pool with per-worker deques and lock-free task submission
public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Thread Pool (Optimized: Work-Stealing) Demo ===\n");

        // --- Test 1: Basic Execution with Work Stealing ---
        System.out.println("--- Test 1: Basic Execution ---");
        PoolConfig config = new PoolConfig(4, 256);
        WorkStealingPool pool = new WorkStealingPool(config);

        for (int i = 1; i <= 10; i++) {
            final int taskId = i;
            pool.submit(new Task() {
                @Override public String getName() { return "Task-" + taskId; }
                @Override public void execute() {
                    System.out.println("  " + getName() + " on " + Thread.currentThread().getName());
                    try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                }
            });
        }
        Thread.sleep(400);
        System.out.println("  Stats: " + pool.getStats());

        // --- Test 2: Uneven Load Triggers Stealing ---
        System.out.println("\n--- Test 2: Uneven Load (Stealing) ---");
        // Submit all tasks to worker-0 by rapid submission; other workers will steal
        for (int i = 1; i <= 20; i++) {
            final int taskId = i;
            pool.submit(new Task() {
                @Override public String getName() { return "Heavy-" + taskId; }
                @Override public void execute() {
                    try { Thread.sleep(30); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                }
            });
        }
        Thread.sleep(500);
        System.out.println("  Stats after uneven load: " + pool.getStats());
        System.out.println("  Steals indicate load balancing: " + pool.getStats().getStolen() + " tasks stolen");

        // --- Test 3: High Throughput ---
        System.out.println("\n--- Test 3: High Throughput ---");
        long start = System.currentTimeMillis();
        int totalTasks = 1000;
        for (int i = 0; i < totalTasks; i++) {
            pool.submit(new Task() {
                @Override public String getName() { return "micro"; }
                @Override public void execute() { /* no-op task for throughput measurement */ }
            });
        }
        Thread.sleep(200);
        long elapsed = System.currentTimeMillis() - start;
        System.out.println("  Submitted " + totalTasks + " tasks");
        System.out.println("  Completed: " + pool.getStats().getCompleted());
        System.out.println("  Elapsed: " + elapsed + "ms");

        // --- Test 4: Graceful Shutdown ---
        System.out.println("\n--- Test 4: Graceful Shutdown ---");
        pool.shutdown();
        System.out.println("  Final: " + pool.getStats());

        System.out.println("\n=== Thread Pool (Optimized) Demo Complete ===");
    }
}

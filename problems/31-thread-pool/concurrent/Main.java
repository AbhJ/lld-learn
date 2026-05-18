/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — Submits tasks, triggers shutdown, shows some are rejected properly

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Thread Pool Demo ===\n");

        int poolSize = 4;
        int queueCapacity = 10;
        ConcurrentThreadPool pool = new ConcurrentThreadPool(poolSize, queueCapacity, new DiscardPolicy());

        List<Task> allTasks = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger rejectedAfterShutdown = new AtomicInteger(0);

        System.out.println("Pool: " + poolSize + " workers, queue capacity: " + queueCapacity);
        System.out.println("Scenario: Submit 20 tasks, then shutdown, then try 10 more.\n");

        // Phase 1: Submit 20 tasks (some will execute, some will queue)
        System.out.println("--- Phase 1: Submitting 20 tasks before shutdown ---");
        for (int i = 0; i < 20; i++) {
            Task task = new Task("Work-" + i, 50); // Each task takes 50ms
            allTasks.add(task);
            pool.submit(task);
        }
        System.out.println("Submitted 20 tasks.");
        System.out.println("Queue size after submission: " + pool.getQueueSize());

        // Phase 2: Initiate shutdown
        Thread.sleep(100); // Let some tasks start executing
        System.out.println("\n--- Phase 2: Initiating shutdown ---");
        pool.shutdown();
        System.out.println("Shutdown initiated. Queue size: " + pool.getQueueSize());

        // Phase 3: Try to submit more tasks (should be rejected)
        System.out.println("\n--- Phase 3: Submitting 10 tasks after shutdown ---");
        for (int i = 20; i < 30; i++) {
            Task task = new Task("Late-" + i, 50);
            allTasks.add(task);
            try {
                pool.submit(task);
            } catch (RejectedTaskException e) {
                // Expected with AbortPolicy
            }
            if (task.isRejected()) {
                rejectedAfterShutdown.incrementAndGet();
            }
        }
        System.out.println("Rejected after shutdown: " + rejectedAfterShutdown.get());

        // Wait for graceful termination
        boolean terminated = pool.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("\n--- Phase 4: Awaiting termination ---");
        System.out.println("Graceful termination: " + (terminated ? "SUCCESS" : "TIMED OUT"));

        // Summary
        long executed = allTasks.stream().filter(Task::isExecuted).count();
        long rejected = allTasks.stream().filter(Task::isRejected).count();
        long pending = allTasks.stream().filter(t -> !t.isExecuted() && !t.isRejected()).count();

        System.out.println("\n--- Summary ---");
        System.out.println("Total tasks created: " + allTasks.size());
        System.out.println("Executed: " + executed);
        System.out.println("Rejected: " + rejected);
        System.out.println("Pending (never ran): " + pending);
        System.out.println("Pool stats — submitted: " + pool.getSubmittedCount() +
                ", executed: " + pool.getExecutedCount() + ", rejected: " + pool.getRejectedCount());

        // Correctness: no task both executed AND rejected
        boolean noConflicts = allTasks.stream().noneMatch(t -> t.isExecuted() && t.isRejected());
        boolean shutdownTasksRejected = rejectedAfterShutdown.get() == 10;
        System.out.println("\nNo execute+reject conflicts: " + (noConflicts ? "PASSED" : "FAILED"));
        System.out.println("All post-shutdown tasks rejected: " + (shutdownTasksRejected ? "PASSED" : "FAILED"));
    }
}

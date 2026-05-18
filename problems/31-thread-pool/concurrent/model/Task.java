/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Task.java — A unit of work submitted to the thread pool

import java.util.concurrent.atomic.AtomicInteger;

class Task implements Runnable { // implements Runnable = can be passed to a Thread for execution
    private static final AtomicInteger counter = new AtomicInteger(0); // static final = shared ID generator across all Task instances
    private final String taskId;           // final = assigned once; safe to read from any thread
    private final String name;             // final = safe publication; no synchronization needed to read
    private final long executionTimeMs;
    private volatile boolean executed = false;  // volatile = worker thread sets, main thread reads immediately
    private volatile boolean rejected = false;  // volatile = rejection visible across threads without lock
    private volatile String executedBy = null;  // volatile = ensures happens-before when read by another thread

    public Task(String name, long executionTimeMs) {
        this.taskId = "TASK-" + counter.incrementAndGet();
        this.name = name;
        this.executionTimeMs = executionTimeMs;
    }

    @Override
    public void run() {
        executedBy = Thread.currentThread().getName();
        try {
            Thread.sleep(executionTimeMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        executed = true;
    }

    public void markRejected() { this.rejected = true; }

    public String getTaskId() { return taskId; }
    public String getName() { return name; }
    public boolean isExecuted() { return executed; }
    public boolean isRejected() { return rejected; }
    public String getExecutedBy() { return executedBy; }

    @Override
    public String toString() {
        if (rejected) return taskId + " (" + name + ") [REJECTED]";
        if (executed) return taskId + " (" + name + ") [DONE by " + executedBy + "]";
        return taskId + " (" + name + ") [PENDING]";
    }

    public static void resetCounter() { counter.set(0); }
}

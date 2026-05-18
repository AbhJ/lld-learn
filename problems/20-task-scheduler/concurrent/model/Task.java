/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Task.java — Schedulable task with atomic state and dependency tracking

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

enum TaskState {
    WAITING,    // Has unmet dependencies
    READY,      // All dependencies met, can be picked up
    RUNNING,    // Currently being executed by a worker
    COMPLETED,  // Done
    FAILED      // Execution failed
}

class Task {
    private static final AtomicInteger counter = new AtomicInteger(0); // AtomicInteger = thread-safe ID generator
    private final String taskId;                 // final = immutable; safe for threads to read
    private final String name;                   // final = set once; no synchronization needed
    private final long executionTimeMs;          // final = immutable execution duration
    private final List<String> dependencies;     // final = reference won't change; safe publication
    private final AtomicReference<TaskState> state = new AtomicReference<>(TaskState.WAITING); // AtomicReference = CAS-based state transitions; prevents double-pickup
    private volatile String executedBy = null;   // volatile = worker name visible to all threads
    private volatile long startTime;             // volatile = timing visible across threads
    private volatile long endTime;               // volatile = completion time visible to observers

    public Task(String name, long executionTimeMs, List<String> dependencies) {
        this.taskId = "TSK-" + counter.incrementAndGet();
        this.name = name;
        this.executionTimeMs = executionTimeMs;
        this.dependencies = dependencies;
    }

    /**
     * CAS-based state transition — prevents double-pickup.
     * Only one worker can transition READY -> RUNNING.
     */
    public boolean tryPickUp(String workerName) {
        if (state.compareAndSet(TaskState.READY, TaskState.RUNNING)) { // CAS = only one thread wins; others get false
            executedBy = workerName;
            startTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public void markReady() {
        state.compareAndSet(TaskState.WAITING, TaskState.READY);
    }

    public void markCompleted() {
        state.set(TaskState.COMPLETED);
        endTime = System.currentTimeMillis();
    }

    public void markFailed() {
        state.set(TaskState.FAILED);
        endTime = System.currentTimeMillis();
    }

    public String getTaskId() { return taskId; }
    public String getName() { return name; }
    public long getExecutionTimeMs() { return executionTimeMs; }
    public List<String> getDependencies() { return dependencies; }
    public TaskState getState() { return state.get(); }
    public String getExecutedBy() { return executedBy; }

    public boolean isCompleted() { return state.get() == TaskState.COMPLETED; }
    public boolean isReady() { return state.get() == TaskState.READY; }

    @Override
    public String toString() {
        return taskId + " (" + name + ") [" + state.get() +
                (executedBy != null ? " by " + executedBy : "") + "]";
    }

    public static void resetCounter() { counter.set(0); }
}

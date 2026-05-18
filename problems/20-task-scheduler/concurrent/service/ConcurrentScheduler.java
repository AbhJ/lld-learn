/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/ConcurrentScheduler.java — AtomicReference for task state, prevents double-pickup

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Concurrent task scheduler that:
 * 1. Manages a pool of workers competing for tasks
 * 2. Resolves dependencies before making tasks available
 * 3. Uses CAS to prevent two workers from picking up the same task
 */
class ConcurrentScheduler {
    private final List<Task> allTasks = new CopyOnWriteArrayList<>(); // CopyOnWriteArrayList = safe iteration while tasks are added
    private final DependencyResolver resolver = new DependencyResolver(); // final = safe publication to worker threads
    private final AtomicInteger completedCount = new AtomicInteger(0); // AtomicInteger = thread-safe completion counter
    private final AtomicInteger pickupConflicts = new AtomicInteger(0); // AtomicInteger = tracks CAS contention

    public void addTask(Task task) {
        allTasks.add(task);
        resolver.registerTask(task.getTaskId());
        // If task has no dependencies, mark it as ready immediately
        if (task.getDependencies().isEmpty()) {
            task.markReady();
        }
    }

    /**
     * Execute all tasks using the given number of workers.
     * Workers compete for ready tasks — CAS prevents double-pickup.
     */
    public void executeAll(int workerCount) throws InterruptedException {
        CountDownLatch allDone = new CountDownLatch(workerCount);

        for (int w = 0; w < workerCount; w++) {
            final String workerName = "Worker-" + w;
            new Thread(() -> {
                try {
                    while (completedCount.get() < allTasks.size()) {
                        Task task = findAndPickUpTask(workerName);
                        if (task != null) {
                            executeTask(task);
                        } else {
                            Thread.sleep(5); // Brief wait before retrying
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    allDone.countDown();
                }
            }, workerName).start();
        }

        allDone.await(10, TimeUnit.SECONDS);
    }

    /**
     * Find a ready task and try to pick it up atomically.
     * If CAS fails (another worker got it), returns null.
     */
    private Task findAndPickUpTask(String workerName) {
        for (Task task : allTasks) {
            if (task.getState() == TaskState.WAITING) {
                // Check if all dependencies are satisfied
                boolean allSatisfied = true;
                for (String dep : task.getDependencies()) {
                    if (!resolver.isDependencySatisfied(dep)) {
                        allSatisfied = false;
                        break;
                    }
                }
                if (allSatisfied) {
                    task.markReady();
                }
            }

            if (task.isReady()) {
                if (task.tryPickUp(workerName)) {
                    return task; // Successfully picked up
                } else {
                    pickupConflicts.incrementAndGet(); // Another worker got it
                }
            }
        }
        return null;
    }

    private void executeTask(Task task) throws InterruptedException {
        // Wait for all dependencies (should already be done, but be safe)
        for (String dep : task.getDependencies()) {
            resolver.awaitDependency(dep);
        }

        // Execute the task
        Thread.sleep(task.getExecutionTimeMs());
        task.markCompleted();
        resolver.markCompleted(task.getTaskId());
        completedCount.incrementAndGet();
    }

    public int getCompletedCount() { return completedCount.get(); }
    public int getPickupConflicts() { return pickupConflicts.get(); }
    public List<Task> getAllTasks() { return Collections.unmodifiableList(allTasks); }
}

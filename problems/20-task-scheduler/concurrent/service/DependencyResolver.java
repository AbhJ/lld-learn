/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/DependencyResolver.java — CountDownLatch per dependency for thread-safe resolution

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Manages task dependencies using CountDownLatch.
 * When a task completes, all tasks waiting on it are notified.
 * CountDownLatch is inherently thread-safe — multiple threads can await,
 * and countDown is atomic.
 */
class DependencyResolver {
    // Maps task ID -> latch that's counted down when that task completes
    private final ConcurrentHashMap<String, CountDownLatch> completionLatches = new ConcurrentHashMap<>(); // ConcurrentHashMap = thread-safe latch registry; CountDownLatch = threads block until dependency completes

    /**
     * Register a task so others can wait on its completion.
     */
    public void registerTask(String taskId) {
        completionLatches.putIfAbsent(taskId, new CountDownLatch(1));
    }

    /**
     * Mark a task as completed — wakes up all threads waiting on it.
     */
    public void markCompleted(String taskId) {
        CountDownLatch latch = completionLatches.get(taskId);
        if (latch != null) {
            latch.countDown();
        }
    }

    /**
     * Wait for a dependency to complete.
     * Blocks the calling thread until the dependency task calls markCompleted.
     */
    public void awaitDependency(String taskId) throws InterruptedException {
        CountDownLatch latch = completionLatches.get(taskId);
        if (latch != null) {
            latch.await();
        }
        // If latch is null, task was never registered — treat as already complete
    }

    /**
     * Check if a dependency is already satisfied (non-blocking).
     */
    public boolean isDependencySatisfied(String taskId) {
        CountDownLatch latch = completionLatches.get(taskId);
        return latch == null || latch.getCount() == 0;
    }
}

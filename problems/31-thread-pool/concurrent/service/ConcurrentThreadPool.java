/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/ConcurrentThreadPool.java — volatile shutdown flag, CountDownLatch for graceful termination

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Custom thread pool demonstrating:
 * - volatile shutdown flag for visibility across threads
 * - CountDownLatch for graceful termination (wait for workers to finish)
 * - Rejection policies for tasks submitted during/after shutdown
 */
class ConcurrentThreadPool {
    private final int poolSize;
    private final BlockingQueue<Task> taskQueue;       // final = reference never changes; BlockingQueue = thread-safe with blocking
    private final Thread[] workers;                    // final = array ref fixed at construction
    private volatile boolean shutdown = false;          // volatile = all worker threads see this flag immediately
    private final CountDownLatch terminationLatch;     // CountDownLatch = workers count down; awaitTermination blocks until zero
    private final RejectionPolicy rejectionPolicy;
    private final AtomicInteger submittedCount = new AtomicInteger(0); // AtomicInteger = lock-free thread-safe counter
    private final AtomicInteger executedCount = new AtomicInteger(0);
    private final AtomicInteger rejectedCount = new AtomicInteger(0);

    public ConcurrentThreadPool(int poolSize, int queueCapacity, RejectionPolicy policy) {
        this.poolSize = poolSize;
        this.taskQueue = new LinkedBlockingQueue<>(queueCapacity);
        this.workers = new Thread[poolSize];
        this.terminationLatch = new CountDownLatch(poolSize);
        this.rejectionPolicy = policy;

        // Start worker threads
        for (int i = 0; i < poolSize; i++) {
            workers[i] = new Thread(() -> {
                try {
                    while (!shutdown || !taskQueue.isEmpty()) {
                        Task task = taskQueue.poll(100, TimeUnit.MILLISECONDS);
                        if (task != null) {
                            task.run();
                            executedCount.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    terminationLatch.countDown();
                }
            }, "PoolWorker-" + i);
            workers[i].setDaemon(true);
            workers[i].start();
        }
    }

    /**
     * Submit a task. If pool is shutting down, apply rejection policy.
     */
    public void submit(Task task) {
        if (shutdown) {
            rejectedCount.incrementAndGet();
            rejectionPolicy.reject(task, this);
            return;
        }

        submittedCount.incrementAndGet();
        if (!taskQueue.offer(task)) {
            // Queue full — reject
            rejectedCount.incrementAndGet();
            rejectionPolicy.reject(task, this);
        }
    }

    /**
     * Initiate graceful shutdown.
     * No new tasks accepted, existing tasks in queue will complete.
     */
    public void shutdown() {
        shutdown = true;
    }

    /**
     * Wait for all workers to finish processing remaining tasks.
     */
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return terminationLatch.await(timeout, unit);
    }

    public boolean isShutdown() { return shutdown; }
    public int getSubmittedCount() { return submittedCount.get(); }
    public int getExecutedCount() { return executedCount.get(); }
    public int getRejectedCount() { return rejectedCount.get(); }
    public int getQueueSize() { return taskQueue.size(); }
    public int getPoolSize() { return poolSize; }
}

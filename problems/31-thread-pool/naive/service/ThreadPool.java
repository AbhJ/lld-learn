/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/ThreadPool.java — Manages worker threads and dispatches tasks from a single shared queue
import java.util.ArrayList;
import java.util.List;

public class ThreadPool {
    private final PoolConfig config;                   // private final = only this class uses; never reassigned
    private final TaskQueue taskQueue;                 // final = reference fixed; contents change via synchronized
    private final List<Worker> workers;                // private = internal detail hidden from outside
    private final ThreadPoolStats stats;
    private RejectionPolicy rejectionPolicy;           // private = can only be changed via setter method
    private CompletionCallback completionCallback;
    private volatile boolean shutdown = false;          // volatile = all threads see updated value immediately

    public ThreadPool(PoolConfig config) {
        this.config = config;
        this.taskQueue = new TaskQueue(config.getQueueCapacity());
        this.workers = new ArrayList<>();
        this.stats = new ThreadPoolStats();
        this.rejectionPolicy = new AbortPolicy();
        System.out.println("[Pool] Created with core=" + config.getCoreSize() +
                ", max=" + config.getMaxSize() + ", queueCapacity=" + config.getQueueCapacity());
        initCoreWorkers();
    }

    private void initCoreWorkers() {
        for (int i = 0; i < config.getCoreSize(); i++) {
            Worker worker = new Worker("pool-worker-" + (i + 1), taskQueue, stats, completionCallback);
            workers.add(worker);
            worker.start();
        }
    }

    public void setRejectionPolicy(RejectionPolicy policy) {
        this.rejectionPolicy = policy;
    }

    public void setCompletionCallback(CompletionCallback callback) {
        this.completionCallback = callback;
    }

    public void submit(Task task) {
        if (shutdown) {
            throw new IllegalStateException("Pool is shut down, cannot accept tasks");
        }
        boolean queued = taskQueue.offer(task);
        if (queued) {
            stats.incrementQueued();
        } else {
            synchronized (workers) {
                if (workers.size() < config.getMaxSize()) {
                    Worker worker = new Worker("pool-worker-" + (workers.size() + 1),
                            taskQueue, stats, completionCallback);
                    workers.add(worker);
                    worker.start();
                    queued = taskQueue.offer(task);
                    if (queued) {
                        stats.incrementQueued();
                        return;
                    }
                }
            }
            stats.incrementRejected();
            rejectionPolicy.reject(task, this);
        }
    }

    public void shutdown() {
        shutdown = true;
        System.out.println("Pool shutting down gracefully...");
        while (!taskQueue.isEmpty()) {
            try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
        }
        try { Thread.sleep(200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        for (Worker worker : workers) { worker.shutdown(); }
        for (Worker worker : workers) {
            try { worker.join(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        System.out.println("All tasks completed. Pool terminated.");
    }

    public void shutdownNow() {
        shutdown = true;
        taskQueue.clear();
        for (Worker worker : workers) { worker.shutdown(); }
    }

    public boolean isShutdown() { return shutdown; }
    public ThreadPoolStats getStats() { return stats; }
    public PoolConfig getConfig() { return config; }
}

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/WorkStealingPool.java — Pool with per-worker deques and work-stealing for load balancing
import java.util.concurrent.atomic.AtomicInteger;

public class WorkStealingPool {
    private final StealingWorker[] workers;                     // array = O(1) access by index for stealing
    private final PoolStats stats;
    private final AtomicInteger nextWorker = new AtomicInteger(0); // AtomicInteger = lock-free round-robin counter
    private volatile boolean shutdown = false;                  // volatile = all workers see shutdown immediately

    // WHY: Each worker has its own deque, eliminating the single-queue bottleneck.
    // Submissions are round-robin distributed; idle workers steal from busy ones.
    public WorkStealingPool(PoolConfig config) {
        this.stats = new PoolStats();
        this.workers = new StealingWorker[config.getWorkerCount()];
        for (int i = 0; i < config.getWorkerCount(); i++) {
            WorkStealingDeque deque = new WorkStealingDeque(config.getLocalQueueCapacity());
            workers[i] = new StealingWorker(i, deque, this, stats);
            workers[i].start();
        }
        System.out.println("[WorkStealingPool] Started " + config.getWorkerCount() + " workers");
    }

    // WHY: Round-robin submission distributes tasks evenly across workers without locking
    public void submit(Task task) {
        if (shutdown) {
            throw new IllegalStateException("Pool is shut down");
        }
        int idx = Math.abs(nextWorker.getAndIncrement() % workers.length);
        workers[idx].getDeque().push(task);
        stats.recordSubmit();
    }

    // Called by idle workers to steal from a random peer
    Task stealFrom(int callerIndex) {
        // WHY: Random victim selection avoids thundering-herd on one queue
        int start = (callerIndex + 1) % workers.length;
        for (int i = 0; i < workers.length - 1; i++) {
            int victim = (start + i) % workers.length;
            Task stolen = workers[victim].getDeque().steal();
            if (stolen != null) {
                return stolen;
            }
        }
        return null;
    }

    public void shutdown() {
        shutdown = true;
        // Wait for queues to drain
        boolean allEmpty;
        do {
            allEmpty = true;
            for (StealingWorker w : workers) {
                if (!w.getDeque().isEmpty()) {
                    allEmpty = false;
                    break;
                }
            }
            if (!allEmpty) {
                try { Thread.sleep(10); } catch (InterruptedException e) { break; }
            }
        } while (!allEmpty);

        try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        for (StealingWorker w : workers) { w.shutdown(); }
        System.out.println("[WorkStealingPool] Shut down. " + stats);
    }

    public void shutdownNow() {
        shutdown = true;
        for (StealingWorker w : workers) { w.shutdown(); }
    }

    public PoolStats getStats() { return stats; }
    public boolean isShutdown() { return shutdown; }
}

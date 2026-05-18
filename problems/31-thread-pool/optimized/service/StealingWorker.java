/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/StealingWorker.java — Worker with its own deque; steals from peers when idle
public class StealingWorker extends Thread { // extends Thread = each worker IS its own thread
    private final int index;                   // identifies this worker for round-robin and steal-avoidance
    private final WorkStealingDeque localDeque; // per-worker deque eliminates shared-queue contention
    private final WorkStealingPool pool;        // reference back to pool for cross-worker stealing
    private final PoolStats stats;
    private volatile boolean running = true;    // volatile = shutdown signal visible across threads instantly

    public StealingWorker(int index, WorkStealingDeque localDeque, WorkStealingPool pool, PoolStats stats) {
        super("steal-worker-" + index);
        this.index = index;
        this.localDeque = localDeque;
        this.pool = pool;
        this.stats = stats;
        setDaemon(true);
    }

    @Override
    public void run() {
        while (running) {
            Task task = localDeque.pop();

            // WHY: If own queue is empty, steal from a random peer.
            // This balances load without centralized scheduling.
            if (task == null) {
                task = pool.stealFrom(index);
                if (task != null) {
                    stats.recordSteal();
                }
            }

            if (task != null) {
                try {
                    task.execute();
                    stats.recordComplete();
                } catch (Exception e) {
                    stats.recordFailure();
                }
            } else {
                // WHY: Brief park avoids busy-spin when all queues are empty
                try { Thread.sleep(1); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    public WorkStealingDeque getDeque() { return localDeque; }
    public int getIndex() { return index; }

    public void shutdown() {
        running = false;
        interrupt();
    }
}

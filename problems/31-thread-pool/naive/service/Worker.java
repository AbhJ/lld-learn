/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/Worker.java — Long-lived thread that pulls and executes tasks from a shared queue
public class Worker extends Thread { // extends Thread = this IS a thread; can be started directly
    private final TaskQueue taskQueue;       // private final = only this worker uses; never reassigned
    private final ThreadPoolStats stats;
    private final CompletionCallback callback;
    private volatile boolean running = true; // volatile = shutdown() in another thread is seen immediately

    public Worker(String name, TaskQueue taskQueue, ThreadPoolStats stats, CompletionCallback callback) {
        super(name);
        this.taskQueue = taskQueue;
        this.stats = stats;
        this.callback = callback;
        setDaemon(true);
    }

    @Override // @Override = compiler checks this actually overrides Thread.run()
    public void run() {
        while (running) {
            try {
                Task task = taskQueue.poll(500);
                if (task == null) {
                    continue;
                }
                stats.decrementQueued();
                stats.incrementActive();
                try {
                    task.execute();
                    stats.incrementCompleted();
                    if (callback != null) {
                        callback.onComplete(task);
                    }
                } catch (Exception e) {
                    stats.incrementFailed();
                    if (callback != null) {
                        callback.onError(task, e);
                    }
                } finally {
                    stats.decrementActive();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void shutdown() {
        running = false;
        interrupt();
    }

    public boolean isRunning() {
        return running;
    }
}

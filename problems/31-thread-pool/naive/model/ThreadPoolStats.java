/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/ThreadPoolStats.java — Runtime statistics for pool health and throughput
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ThreadPoolStats {
    private final AtomicInteger activeThreads = new AtomicInteger(0);  // private = encapsulated; final = reference never reassigned
    private final AtomicInteger queuedTasks = new AtomicInteger(0);   // AtomicInteger = thread-safe counter
    private final AtomicLong completedTasks = new AtomicLong(0);      // AtomicLong = thread-safe for larger counts
    private final AtomicLong failedTasks = new AtomicLong(0);
    private final AtomicLong rejectedTasks = new AtomicLong(0);

    public void incrementActive() { activeThreads.incrementAndGet(); }
    public void decrementActive() { activeThreads.decrementAndGet(); }
    public void incrementQueued() { queuedTasks.incrementAndGet(); }
    public void decrementQueued() { queuedTasks.decrementAndGet(); }
    public void incrementCompleted() { completedTasks.incrementAndGet(); }
    public void incrementFailed() { failedTasks.incrementAndGet(); }
    public void incrementRejected() { rejectedTasks.incrementAndGet(); }

    public int getActiveThreads() { return activeThreads.get(); }
    public int getQueuedTasks() { return queuedTasks.get(); }
    public long getCompletedTasks() { return completedTasks.get(); }
    public long getFailedTasks() { return failedTasks.get(); }
    public long getRejectedTasks() { return rejectedTasks.get(); }

    @Override
    public String toString() {
        return String.format("ThreadPoolStats[active=%d, queued=%d, completed=%d, failed=%d, rejected=%d]",
                activeThreads.get(), queuedTasks.get(), completedTasks.get(),
                failedTasks.get(), rejectedTasks.get());
    }
}

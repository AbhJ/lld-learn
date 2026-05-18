/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/DelayedTask.java — Task with scheduled execution time deferring until delay elapses

public class DelayedTask extends Task {          // extends = inherits Task; adds delay-based scheduling
    private String description;
    private long delayMs;
    private long scheduledTime;

    public DelayedTask(String name, TaskPriority priority, String description, long delayMs) {
        super(name, priority);
        this.description = description;
        this.delayMs = delayMs;
        this.scheduledTime = System.currentTimeMillis() + delayMs;
    }

    public boolean isReadyToExecute() {
        return System.currentTimeMillis() >= scheduledTime;
    }

    @Override
    public boolean isReady() {
        return super.isReady() && isReadyToExecute();
    }

    @Override
    public TaskResult execute() {
        long start = System.currentTimeMillis();
        System.out.printf("    Executing delayed task: %s (was delayed by %dms)%n", getName(), delayMs);
        long elapsed = System.currentTimeMillis() - start;
        return TaskResult.success(getTaskId(), description + " completed after delay", elapsed);
    }

    public long getDelayMs() { return delayMs; }
    public long getScheduledTime() { return scheduledTime; }
}

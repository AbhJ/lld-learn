/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/RecurringTask.java — Task with recurring execution at fixed intervals

public class RecurringTask extends Task {        // extends = inherits Task; adds recurring execution logic
    private String description;
    private int maxRuns;
    private int currentRun;
    private long intervalMs;

    public RecurringTask(String name, TaskPriority priority, String description,
                         int maxRuns, long intervalMs) {
        super(name, priority);
        this.description = description;
        this.maxRuns = maxRuns;
        this.currentRun = 0;
        this.intervalMs = intervalMs;
    }

    @Override
    public TaskResult execute() {
        long start = System.currentTimeMillis();
        currentRun++;
        System.out.printf("    Executing recurring: %s (run %d/%d)%n", getName(), currentRun, maxRuns);
        long elapsed = System.currentTimeMillis() - start;
        return TaskResult.success(getTaskId(),
                String.format("Run %d/%d completed", currentRun, maxRuns), elapsed);
    }

    public boolean hasMoreRuns() { return currentRun < maxRuns; }
    public int getCurrentRun() { return currentRun; }
    public int getMaxRuns() { return maxRuns; }
    public long getIntervalMs() { return intervalMs; }

    public void resetForNextRun() {
        setState(State.PENDING);
    }
}

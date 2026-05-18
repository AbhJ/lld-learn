/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Task.java — Schedulable unit of work with priority, dependencies, and delayed execution

import java.util.*;

public abstract class Task implements Comparable<Task> { // abstract = can't instantiate; subclass provides execute()
    public enum State { PENDING, READY, RUNNING, COMPLETED, FAILED, CANCELLED } // enum = fixed lifecycle states

    private String taskId;
    private String name;
    private TaskPriority priority;
    private State state;
    private TaskResult result;
    private long createdAt;
    private long scheduledTimeMs; // When this task becomes eligible
    private Set<String> dependencies;            // HashSet = O(1) add/remove/check for dependencies
    private static int counter = 0;              // static = shared ID counter across all Task subclasses

    public Task(String name, TaskPriority priority) {
        this(name, priority, 0);
    }

    public Task(String name, TaskPriority priority, long delayMs) {
        this.taskId = "TASK-" + (++counter);
        this.name = name;
        this.priority = priority;
        this.state = State.PENDING;
        this.createdAt = System.currentTimeMillis();
        this.scheduledTimeMs = System.currentTimeMillis() + delayMs;
        this.dependencies = new HashSet<>();
    }

    public abstract TaskResult execute();         // abstract = each subclass defines its own execution logic

    public void addDependency(String taskId) { dependencies.add(taskId); }
    public boolean hasDependencies() { return !dependencies.isEmpty(); }
    public void removeDependency(String taskId) { dependencies.remove(taskId); }

    public boolean isReady() {
        return dependencies.isEmpty() && state == State.PENDING
                && System.currentTimeMillis() >= scheduledTimeMs;
    }

    public long getRemainingDelayMs() {
        return scheduledTimeMs - System.currentTimeMillis();
    }

    @Override
    public int compareTo(Task other) {
        // Higher priority first, then earlier scheduled time
        int prioDiff = other.priority.getLevel() - this.priority.getLevel();
        if (prioDiff != 0) return prioDiff;
        return Long.compare(this.scheduledTimeMs, other.scheduledTimeMs);
    }

    public String getTaskId() { return taskId; }
    public String getName() { return name; }
    public TaskPriority getPriority() { return priority; }
    public State getState() { return state; }
    public void setState(State state) { this.state = state; }
    public TaskResult getResult() { return result; }
    public void setResult(TaskResult result) { this.result = result; }
    public Set<String> getDependencies() { return Collections.unmodifiableSet(dependencies); }
    public long getScheduledTimeMs() { return scheduledTimeMs; }

    @Override
    public String toString() {
        return String.format("Task[%s] \"%s\" [%s] State=%s", taskId, name, priority, state);
    }
}

class SimpleTask extends Task {                  // extends = inherits from abstract Task
    private String description;

    public SimpleTask(String name, TaskPriority priority, String description) {
        super(name, priority);
        this.description = description;
    }

    public SimpleTask(String name, TaskPriority priority, String description, long delayMs) {
        super(name, priority, delayMs);
        this.description = description;
    }

    @Override
    public TaskResult execute() {
        long start = System.currentTimeMillis();
        System.out.printf("    Executing: %s (%s)%n", getName(), description);
        long elapsed = System.currentTimeMillis() - start;
        return TaskResult.success(getTaskId(), description + " completed", elapsed);
    }
}

class RecurringTask extends Task {               // extends = inherits Task; adds recurring run logic
    private String description;
    private int maxRuns;
    private int currentRun;
    private long intervalMs;

    public RecurringTask(String name, TaskPriority priority, String description, int maxRuns, long intervalMs) {
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
        return TaskResult.success(getTaskId(), String.format("Run %d/%d completed", currentRun, maxRuns), elapsed);
    }

    public boolean hasMoreRuns() { return currentRun < maxRuns; }
    public void resetForNextRun() { setState(State.PENDING); }
    public int getCurrentRun() { return currentRun; }
    public int getMaxRuns() { return maxRuns; }
}

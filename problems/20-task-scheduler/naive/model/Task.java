/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Task.java — Schedulable unit of work with priority, dependencies, and state

import java.util.*;

public abstract class Task implements Comparable<Task> { // abstract = can't instantiate directly; must be subclassed
    public enum State { PENDING, READY, RUNNING, COMPLETED, FAILED, CANCELLED } // enum = fixed set of task states

    private String taskId;                       // private = only this class manages task identity
    private String name;
    private TaskPriority priority;
    private State state;
    private TaskResult result;
    private long createdAt;
    private Set<String> dependencies; // taskIds this task depends on
    private static int counter = 0;              // static = shared ID counter across all Task instances

    public Task(String name, TaskPriority priority) {
        this.taskId = "TASK-" + (++counter);
        this.name = name;
        this.priority = priority;
        this.state = State.PENDING;
        this.createdAt = System.currentTimeMillis();
        this.dependencies = new HashSet<>();
    }

    public abstract TaskResult execute();         // abstract = subclass MUST provide its own execution logic

    public void addDependency(String taskId) {
        dependencies.add(taskId);
    }

    public boolean hasDependencies() { return !dependencies.isEmpty(); }

    public void removeDependency(String taskId) {
        dependencies.remove(taskId);
    }

    public boolean isReady() {
        return dependencies.isEmpty() && state == State.PENDING;
    }

    public String getTaskId() { return taskId; }
    public String getName() { return name; }
    public TaskPriority getPriority() { return priority; }
    public State getState() { return state; }
    public void setState(State state) { this.state = state; }
    public TaskResult getResult() { return result; }
    public void setResult(TaskResult result) { this.result = result; }
    public long getCreatedAt() { return createdAt; }
    public Set<String> getDependencies() { return Collections.unmodifiableSet(dependencies); }

    @Override
    public int compareTo(Task other) {
        // Higher priority first
        int prioDiff = other.priority.getLevel() - this.priority.getLevel();
        if (prioDiff != 0) return prioDiff;
        // Older tasks first (FIFO for same priority)
        return Long.compare(this.createdAt, other.createdAt);
    }

    @Override
    public String toString() {
        return String.format("Task[%s] \"%s\" [%s] State=%s", taskId, name, priority, state);
    }
}

class SimpleTask extends Task {                  // extends = inherits from abstract Task; provides execute()
    private Runnable action;
    private String actionDescription;

    public SimpleTask(String name, TaskPriority priority, String description) {
        super(name, priority);
        this.actionDescription = description;
    }

    @Override
    public TaskResult execute() {
        long start = System.currentTimeMillis();
        try {
            // Simulate task execution
            System.out.printf("    Executing: %s (%s)%n", getName(), actionDescription);
            long elapsed = System.currentTimeMillis() - start;
            return TaskResult.success(getTaskId(), actionDescription + " completed", elapsed);
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            return TaskResult.failure(getTaskId(), e.getMessage(), elapsed);
        }
    }
}

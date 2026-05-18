/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/Scheduler.java — Orchestrates task scheduling, dependency resolution, and workers

import java.util.*;

public class Scheduler {
    // Strategy: pluggable policy that chooses which ready task to run next.
    private SchedulingStrategy schedulingStrategy;
    private List<Task> readyQueue;               // ready tasks; ordering decided by SchedulingStrategy
    private List<Task> pendingTasks; // Tasks waiting for dependencies or delay
    private List<Task> allTasks;
    private List<Worker> workers;                // private = worker pool managed internally
    private TaskDependency dependencyGraph;       // private = encapsulates dependency resolution
    private Map<String, Task> taskMap;
    private List<TaskResult> results;

    // Observer: fan-out for task lifecycle events (completion / failure).
    private final List<TaskCompletionListener> listeners = new ArrayList<>();

    /** Default ctor uses priority-based scheduling (matches the original PriorityQueue behaviour). */
    public Scheduler(int workerCount) {
        this(workerCount, new PrioritySchedulingStrategy());
    }

    /** Inject a different scheduling strategy (e.g. FIFO for fairness demos). */
    public Scheduler(int workerCount, SchedulingStrategy schedulingStrategy) {
        this.schedulingStrategy = schedulingStrategy;
        this.readyQueue = new ArrayList<>();
        this.pendingTasks = new ArrayList<>();
        this.allTasks = new ArrayList<>();
        this.workers = new ArrayList<>();
        this.dependencyGraph = new TaskDependency();
        this.taskMap = new HashMap<>();
        this.results = new ArrayList<>();

        for (int i = 0; i < workerCount; i++) {
            workers.add(new Worker("W-" + (i + 1), "Worker-" + (i + 1)));
        }
    }

    /** Register a listener that wants to hear about task completions/failures. */
    public void addListener(TaskCompletionListener listener) {
        listeners.add(listener);
    }

    public Task submit(Task task) {
        allTasks.add(task);
        taskMap.put(task.getTaskId(), task);

        if (task.isReady()) {
            readyQueue.add(task);
            task.setState(Task.State.READY);
        } else {
            pendingTasks.add(task);
        }

        System.out.printf("  Submitted: %s [%s]%s%n", task.getName(), task.getPriority(),
                task.hasDependencies() ? " (has dependencies)" : "");
        return task;
    }

    public boolean addDependency(Task task, Task dependsOn) {
        boolean success = dependencyGraph.addDependency(task.getTaskId(), dependsOn.getTaskId());
        if (success) {
            task.addDependency(dependsOn.getTaskId());
            // Move from ready to pending if it was ready
            if (task.getState() == Task.State.READY) {
                readyQueue.remove(task);
                task.setState(Task.State.PENDING);
                pendingTasks.add(task);
            }
            System.out.printf("  Dependency: \"%s\" depends on \"%s\"%n", task.getName(), dependsOn.getName());
        }
        return success;
    }

    public void runNext() {
        // Check pending tasks that may now be ready
        checkPendingTasks();

        if (readyQueue.isEmpty()) {
            System.out.println("  No tasks ready to execute.");
            return;
        }

        Task task = schedulingStrategy.selectNext(readyQueue);
        if (task == null) {
            System.out.println("  No tasks ready to execute.");
            return;
        }
        readyQueue.remove(task);

        Worker worker = getAvailableWorker();
        if (worker == null) {
            readyQueue.add(task); // Put it back
            System.out.println("  No workers available.");
            return;
        }

        TaskResult result = worker.execute(task);
        results.add(result);

        // Handle completion / failure and fan out to listeners
        if (result.isSuccess()) {
            onTaskCompleted(task);
            fireCompleted(task);
        } else {
            fireFailed(task, new RuntimeException(result.getOutput() == null ? "task failed" : result.getOutput()));
        }
    }

    public void runAll() {
        int maxIterations = allTasks.size() * 10; // Prevent infinite loops for recurring
        int iteration = 0;
        while (iteration < maxIterations) {
            checkPendingTasks();
            if (readyQueue.isEmpty()) break;
            runNext();
            iteration++;
        }
    }

    private void onTaskCompleted(Task task) {
        // Notify dependency graph
        dependencyGraph.markCompleted(task.getTaskId());

        // Handle recurring tasks
        if (task instanceof RecurringTask) {
            RecurringTask recurring = (RecurringTask) task;
            if (recurring.hasMoreRuns()) {
                recurring.resetForNextRun();
                readyQueue.add(recurring);
            }
        }

        // Check pending tasks
        checkPendingTasks();
    }

    private void fireCompleted(Task task) {
        for (TaskCompletionListener l : listeners) l.onTaskCompleted(task);
    }

    private void fireFailed(Task task, Throwable error) {
        for (TaskCompletionListener l : listeners) l.onTaskFailed(task, error);
    }

    private void checkPendingTasks() {
        Iterator<Task> iter = pendingTasks.iterator();
        while (iter.hasNext()) {
            Task task = iter.next();
            if (task.getState() == Task.State.CANCELLED) {
                iter.remove();
                continue;
            }
            // Check dependencies
            boolean depsReady = dependencyGraph.isReady(task.getTaskId());
            // Check delay for delayed tasks
            boolean delayReady = true;
            if (task instanceof DelayedTask) {
                delayReady = ((DelayedTask) task).isReadyToExecute();
            }

            if (depsReady && delayReady) {
                iter.remove();
                task.setState(Task.State.READY);
                readyQueue.add(task);
            }
        }
    }

    public boolean cancel(Task task) {
        if (task.getState() == Task.State.RUNNING || task.getState() == Task.State.COMPLETED) {
            return false;
        }
        task.setState(Task.State.CANCELLED);
        task.setResult(TaskResult.cancelled(task.getTaskId()));
        readyQueue.remove(task);
        System.out.printf("  Cancelled: %s%n", task.getName());
        return true;
    }

    private Worker getAvailableWorker() {
        for (Worker w : workers) {
            if (!w.isBusy()) return w;
        }
        return null;
    }

    public SchedulingStrategy getSchedulingStrategy() { return schedulingStrategy; }
    public List<TaskResult> getResults() { return Collections.unmodifiableList(results); }
    public List<Worker> getWorkers() { return Collections.unmodifiableList(workers); }
    public int getPendingCount() { return pendingTasks.size(); }
    public int getReadyCount() { return readyQueue.size(); }
    public int getTotalTasks() { return allTasks.size(); }
}

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/Scheduler.java — Optimized scheduler with DelayQueue and DAG-based topological sort

import java.util.*;
import java.util.concurrent.*;

/**
 * Optimized: Uses an ordered ready collection (driven by a pluggable
 * SchedulingStrategy) for event-driven scheduling instead of poll-based
 * checking. Tasks are only added to the ready queue when their dependencies
 * are satisfied (signaled by DAG.markCompleted).
 */
public class Scheduler {
    // Strategy: pluggable policy that chooses which ready task to run next.
    private SchedulingStrategy schedulingStrategy;
    private List<Task> readyQueue;               // ready tasks; ordering decided by SchedulingStrategy
    private List<Task> allTasks;
    private Map<String, Task> taskMap;           // HashMap = O(1) task lookup by ID
    private DAG dag;                             // DAG = dependency graph with topological sort
    private List<TaskResult> results;
    private ExecutorService workerPool;          // ExecutorService = managed thread pool for parallel execution
    private int workerCount;

    // Observer: fan-out for task lifecycle events (completion / failure).
    private final List<TaskCompletionListener> listeners = new ArrayList<>();

    /** Default ctor uses priority-based scheduling (matches the original PriorityBlockingQueue behaviour). */
    public Scheduler(int workerCount) {
        this(workerCount, new PrioritySchedulingStrategy());
    }

    /** Inject a different scheduling strategy (e.g. FIFO for fairness demos). */
    public Scheduler(int workerCount, SchedulingStrategy schedulingStrategy) {
        this.schedulingStrategy = schedulingStrategy;
        this.readyQueue = new ArrayList<>();
        this.allTasks = new ArrayList<>();
        this.taskMap = new HashMap<>();
        this.dag = new DAG();
        this.results = new ArrayList<>();
        this.workerCount = workerCount;
        this.workerPool = Executors.newFixedThreadPool(workerCount, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("Worker-" + t.getId());
            return t;
        });
    }

    /** Register a listener that wants to hear about task completions/failures. */
    public void addListener(TaskCompletionListener listener) {
        listeners.add(listener);
    }

    public Task submit(Task task) {
        allTasks.add(task);
        taskMap.put(task.getTaskId(), task);
        dag.addNode(task.getTaskId());

        if (task.isReady()) {
            task.setState(Task.State.READY);
            readyQueue.add(task);
        }

        System.out.printf("  Submitted: %s [%s]%n", task.getName(), task.getPriority());
        return task;
    }

    public boolean addDependency(Task task, Task dependsOn) {
        boolean success = dag.addDependency(task.getTaskId(), dependsOn.getTaskId());
        if (success) {
            task.addDependency(dependsOn.getTaskId());
            if (task.getState() == Task.State.READY) {
                readyQueue.remove(task);
                task.setState(Task.State.PENDING);
            }
            System.out.printf("  Dependency: \"%s\" depends on \"%s\"%n", task.getName(), dependsOn.getName());
        }
        return success;
    }

    /**
     * Event-driven execution: tasks enter readyQueue only when all dependencies are met.
     * No polling required - DAG.markCompleted signals which tasks become unblocked.
     */
    public void runAll() {
        int maxIterations = allTasks.size() * 10;
        int iteration = 0;
        while (iteration < maxIterations) {
            if (readyQueue.isEmpty()) break;

            Task task = schedulingStrategy.selectNext(readyQueue);
            if (task == null) break;
            readyQueue.remove(task);

            if (task.getState() == Task.State.CANCELLED) continue;

            // Check if delayed task is ready
            if (!task.isReady() && task.getState() == Task.State.READY) {
                readyQueue.add(task);
                break; // Wait for delay
            }

            executeTask(task);
            iteration++;
        }
    }

    private void executeTask(Task task) {
        task.setState(Task.State.RUNNING);
        System.out.printf("  [Worker] Running: %s [%s]%n", task.getName(), task.getPriority());

        TaskResult result;
        try {
            result = task.execute();
        } catch (Throwable t) {
            task.setState(Task.State.FAILED);
            fireFailed(task, t);
            return;
        }
        task.setResult(result);
        results.add(result);

        if (result.isSuccess()) {
            task.setState(Task.State.COMPLETED);
            onTaskCompleted(task);
            fireCompleted(task);
        } else {
            task.setState(Task.State.FAILED);
            fireFailed(task, new RuntimeException(result.getOutput() == null ? "task failed" : result.getOutput()));
        }
    }

    private void onTaskCompleted(Task task) {
        // DAG signals which tasks are now unblocked
        List<String> unblocked = dag.markCompleted(task.getTaskId());
        for (String unblockedId : unblocked) {
            Task unblockedTask = taskMap.get(unblockedId);
            if (unblockedTask != null && unblockedTask.getState() == Task.State.PENDING) {
                unblockedTask.getDependencies().forEach(unblockedTask::removeDependency);
                if (unblockedTask.isReady()) {
                    unblockedTask.setState(Task.State.READY);
                    readyQueue.add(unblockedTask);
                }
            }
        }

        // Handle recurring
        if (task instanceof RecurringTask) {
            RecurringTask recurring = (RecurringTask) task;
            if (recurring.hasMoreRuns()) {
                recurring.resetForNextRun();
                readyQueue.add(recurring);
            }
        }
    }

    private void fireCompleted(Task task) {
        for (TaskCompletionListener l : listeners) l.onTaskCompleted(task);
    }

    private void fireFailed(Task task, Throwable error) {
        for (TaskCompletionListener l : listeners) l.onTaskFailed(task, error);
    }

    public boolean cancel(Task task) {
        if (task.getState() == Task.State.RUNNING || task.getState() == Task.State.COMPLETED) return false;
        task.setState(Task.State.CANCELLED);
        task.setResult(TaskResult.cancelled(task.getTaskId()));
        readyQueue.remove(task);
        System.out.printf("  Cancelled: %s%n", task.getName());
        return true;
    }

    /**
     * Get topological execution order (for visualization).
     */
    public List<String> getExecutionOrder() {
        return dag.topologicalSort();
    }

    public SchedulingStrategy getSchedulingStrategy() { return schedulingStrategy; }
    public void shutdown() { workerPool.shutdown(); }
    public List<TaskResult> getResults() { return Collections.unmodifiableList(results); }
    public int getTotalTasks() { return allTasks.size(); }
}

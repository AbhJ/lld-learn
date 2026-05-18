/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/Worker.java — Task execution unit that pulls tasks and runs them to completion

public class Worker {
    private String workerId;                     // private = only this class manages worker identity
    private String name;
    private boolean busy;                        // private = internal state tracking if worker is occupied
    private int tasksCompleted;

    public Worker(String workerId, String name) {
        this.workerId = workerId;
        this.name = name;
        this.busy = false;
        this.tasksCompleted = 0;
    }

    public TaskResult execute(Task task) {
        busy = true;
        task.setState(Task.State.RUNNING);
        System.out.printf("  [%s] Running task: %s [%s]%n", name, task.getName(), task.getPriority());

        TaskResult result = task.execute();
        task.setResult(result);

        if (result.isSuccess()) {
            task.setState(Task.State.COMPLETED);
        } else {
            task.setState(Task.State.FAILED);
        }

        tasksCompleted++;
        busy = false;
        return result;
    }

    public String getWorkerId() { return workerId; }
    public String getName() { return name; }
    public boolean isBusy() { return busy; }
    public int getTasksCompleted() { return tasksCompleted; }

    @Override
    public String toString() {
        return String.format("Worker[%s] completed=%d, %s", name, tasksCompleted, busy ? "BUSY" : "IDLE");
    }
}

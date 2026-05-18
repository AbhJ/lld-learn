/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/TaskResult.java — Task execution outcome with success/failure and output data

public class TaskResult {
    public enum Status { SUCCESS, FAILURE, CANCELLED, PENDING } // enum = fixed set of outcomes

    private String taskId;
    private Status status;
    private String output;
    private long executionTimeMs;

    public TaskResult(String taskId, Status status, String output, long executionTimeMs) {
        this.taskId = taskId;
        this.status = status;
        this.output = output;
        this.executionTimeMs = executionTimeMs;
    }

    public static TaskResult success(String taskId, String output, long timeMs) { // static = factory method; no instance needed
        return new TaskResult(taskId, Status.SUCCESS, output, timeMs);
    }
    public static TaskResult failure(String taskId, String error, long timeMs) {
        return new TaskResult(taskId, Status.FAILURE, error, timeMs);
    }
    public static TaskResult cancelled(String taskId) {
        return new TaskResult(taskId, Status.CANCELLED, "Cancelled", 0);
    }

    public String getTaskId() { return taskId; }
    public Status getStatus() { return status; }
    public String getOutput() { return output; }
    public long getExecutionTimeMs() { return executionTimeMs; }
    public boolean isSuccess() { return status == Status.SUCCESS; }

    @Override
    public String toString() {
        return String.format("TaskResult[%s] %s - %s (%dms)", taskId, status, output, executionTimeMs);
    }
}

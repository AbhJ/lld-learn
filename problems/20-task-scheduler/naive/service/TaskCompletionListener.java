/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/TaskCompletionListener.java — Observer contract for task lifecycle events
//
// Listeners are notified after the scheduler has finished a task so that
// loggers, metrics sinks, or downstream notifiers can react without coupling
// to the scheduler's internals.

interface TaskCompletionListener {
    /** Called after a task completes successfully. */
    void onTaskCompleted(Task task);

    /** Called after a task fails or throws. */
    void onTaskFailed(Task task, Throwable error);
}

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/LoggingTaskListener.java — Console logger; demonstrates observing task events

class LoggingTaskListener implements TaskCompletionListener {
    @Override
    public void onTaskCompleted(Task task) {
        System.out.printf("  [event] completed: %s [%s]%n", task.getName(), task.getPriority());
    }

    @Override
    public void onTaskFailed(Task task, Throwable error) {
        String msg = error == null ? "unknown error" : error.getMessage();
        System.out.printf("  [event] FAILED: %s [%s] — %s%n", task.getName(), task.getPriority(), msg);
    }
}

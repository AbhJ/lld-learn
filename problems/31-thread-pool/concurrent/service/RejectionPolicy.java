/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/RejectionPolicy.java — Policies for handling rejected tasks

interface RejectionPolicy { // interface = strategy pattern; swap rejection behavior at runtime
    void reject(Task task, ConcurrentThreadPool pool);
}

/**
 * AbortPolicy: Reject the task and throw an exception.
 */
class AbortPolicy implements RejectionPolicy {
    @Override
    public void reject(Task task, ConcurrentThreadPool pool) {
        task.markRejected();
        throw new RejectedTaskException("Task " + task.getTaskId() + " rejected — pool is shutting down");
    }
}

/**
 * CallerRunsPolicy: Execute the task in the caller's thread instead.
 * This provides backpressure — the submitting thread slows down.
 */
class CallerRunsPolicy implements RejectionPolicy {
    @Override
    public void reject(Task task, ConcurrentThreadPool pool) {
        if (!pool.isShutdown()) {
            task.run(); // Run in the caller's thread
        } else {
            task.markRejected();
        }
    }
}

/**
 * DiscardPolicy: Silently discard the rejected task.
 */
class DiscardPolicy implements RejectionPolicy {
    @Override
    public void reject(Task task, ConcurrentThreadPool pool) {
        task.markRejected();
        // Silently discarded
    }
}

class RejectedTaskException extends RuntimeException { // extends = inherits from RuntimeException (unchecked)
    public RejectedTaskException(String message) {
        super(message);
    }
}

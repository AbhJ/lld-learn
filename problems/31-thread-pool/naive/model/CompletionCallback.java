/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/CompletionCallback.java — Observer notified when a task finishes execution
public interface CompletionCallback { // interface = contract any listener must implement
    void onComplete(Task task);
    void onError(Task task, Exception e);
}

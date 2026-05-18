/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/TaskQueue.java — Thread-safe synchronized queue for buffering submitted tasks
import java.util.LinkedList;
import java.util.Queue;

public class TaskQueue {
    private final Queue<Task> queue = new LinkedList<>(); // private final = only this class uses it; reference never changes
    private final int capacity;

    public TaskQueue(int capacity) {
        this.capacity = capacity;
    }

    public synchronized boolean offer(Task task) { // synchronized = only one thread can enter at a time
        if (queue.size() >= capacity) {
            return false;
        }
        queue.add(task);
        notifyAll();
        return true;
    }

    public synchronized Task poll(long timeoutMs) throws InterruptedException { // synchronized = mutual exclusion on this object
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (queue.isEmpty()) {
            long remaining = deadline - System.currentTimeMillis();
            if (remaining <= 0) {
                return null;
            }
            wait(remaining);
        }
        return queue.poll();
    }

    public synchronized Task poll() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();
        }
        return queue.poll();
    }

    public synchronized int size() {
        return queue.size();
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    public synchronized void clear() {
        queue.clear();
    }

    public synchronized void wakeAll() {
        notifyAll();
    }
}

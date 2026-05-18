/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Elevator.java — Elevator with AtomicReference to prevent double-assignment of requests

package model;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Elevator {
    private final int id;             // final = immutable identity; safe to read from any thread
    private final ConcurrentLinkedQueue<Request> assignedRequests; // ConcurrentLinkedQueue = lock-free queue for multi-thread add/poll
    private final AtomicReference<Request> currentRequest; // AtomicReference = CAS-based; prevents double-processing

    public Elevator(int id) {
        this.id = id;
        this.assignedRequests = new ConcurrentLinkedQueue<>();
        this.currentRequest = new AtomicReference<>(null);
    }

    /**
     * Attempt to claim a request using CAS. Only one elevator can claim it.
     * Returns true if this elevator successfully claimed the request.
     */
    public boolean tryAcceptRequest(Request request, AtomicReference<Elevator> assignmentLock) {
        // CAS on the shared assignment lock — only one elevator wins
        if (assignmentLock.compareAndSet(null, this)) {
            assignedRequests.add(request);
            return true;
        }
        return false;
    }

    /**
     * Process the next request in the queue. Returns the request if one was available.
     */
    public Request processNext() {
        Request next = assignedRequests.poll();
        if (next != null) {
            currentRequest.set(next);
        }
        return next;
    }

    public void completeCurrentRequest() {
        currentRequest.set(null);
    }

    public int getId() { return id; }
    public int getQueueSize() { return assignedRequests.size(); }
    public Request getCurrentRequest() { return currentRequest.get(); }

    @Override
    public String toString() {
        return "Elevator-" + id + " (queue=" + assignedRequests.size() + ")";
    }
}

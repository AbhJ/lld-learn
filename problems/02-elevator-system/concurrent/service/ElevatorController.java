/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/ElevatorController.java — Controller using ConcurrentLinkedQueue and AtomicReference for safe dispatch

package service;

import model.Elevator;
import model.Request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ElevatorController {
    private final List<Elevator> elevators;  // final = reference never changes after construction
    private final ConcurrentLinkedQueue<Request> requestQueue; // ConcurrentLinkedQueue = thread-safe submission from many callers
    private final AtomicInteger servedCount; // AtomicInteger = lock-free counter; no lost increments
    private final List<String> servedLog;    // synchronizedList = safe for concurrent appends

    public ElevatorController(int numElevators) {
        this.elevators = new ArrayList<>();
        for (int i = 0; i < numElevators; i++) {
            elevators.add(new Elevator(i));
        }
        this.requestQueue = new ConcurrentLinkedQueue<>();
        this.servedCount = new AtomicInteger(0);
        this.servedLog = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * Submit a request to the system. Thread-safe via ConcurrentLinkedQueue.
     */
    public void submitRequest(Request request) {
        requestQueue.add(request);
    }

    /**
     * Dispatch all pending requests to elevators.
     * Each request gets an AtomicReference lock — only one elevator can claim it.
     * This prevents double-assignment.
     */
    public void dispatchAll() {
        Request request;
        while ((request = requestQueue.poll()) != null) {
            AtomicReference<Elevator> assignmentLock = new AtomicReference<>(null);
            Elevator assigned = null;

            // Find the best elevator (least loaded) via CAS competition
            // In a concurrent scenario, multiple elevators might try to accept
            int minLoad = Integer.MAX_VALUE;
            Elevator candidate = null;
            for (Elevator e : elevators) {
                if (e.getQueueSize() < minLoad) {
                    minLoad = e.getQueueSize();
                    candidate = e;
                }
            }

            if (candidate != null && candidate.tryAcceptRequest(request, assignmentLock)) {
                assigned = candidate;
            }

            if (assigned != null) {
                servedCount.incrementAndGet();
                servedLog.add(request.getPassengerId() + " -> Elevator-" + assigned.getId());
            }
        }
    }

    /**
     * Simulate concurrent dispatch where multiple elevators race to accept requests.
     * This is the scenario that exposes the race condition the AtomicReference prevents.
     */
    public void dispatchConcurrently() {
        Request request;
        while ((request = requestQueue.poll()) != null) {
            final Request req = request;
            final AtomicReference<Elevator> assignmentLock = new AtomicReference<>(null);

            // All elevators race to accept
            List<Thread> racers = new ArrayList<>();
            for (Elevator e : elevators) {
                racers.add(new Thread(() -> e.tryAcceptRequest(req, assignmentLock)));
            }
            racers.forEach(Thread::start);
            for (Thread t : racers) {
                try { t.join(); } catch (InterruptedException ignored) {}
            }

            Elevator winner = assignmentLock.get();
            if (winner != null) {
                servedCount.incrementAndGet();
                servedLog.add(req.getPassengerId() + " -> Elevator-" + winner.getId());
            }
        }
    }

    public int getServedCount() { return servedCount.get(); }
    public List<String> getServedLog() { return servedLog; }
    public List<Elevator> getElevators() { return elevators; }

    public int getTotalAssigned() {
        int total = 0;
        for (Elevator e : elevators) {
            total += e.getQueueSize();
        }
        return total;
    }
}

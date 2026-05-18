/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/ConcurrentStateMachine.java — AtomicReference<State> with CAS for valid transitions

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConcurrentStateMachine {
    private final AtomicReference<State> currentState;              // AtomicReference = CAS-based thread-safe state swap
    private final CopyOnWriteArrayList<State> transitionHistory = new CopyOnWriteArrayList<>(); // CopyOnWriteArrayList = safe concurrent reads of history
    private final AtomicInteger successfulTransitions = new AtomicInteger(0); // AtomicInteger = lock-free counter
    private final AtomicInteger rejectedTransitions = new AtomicInteger(0);  // tracks CAS failures
    private final AtomicInteger invalidTransitions = new AtomicInteger(0);   // tracks illegal transitions

    public ConcurrentStateMachine(State initial) {
        this.currentState = new AtomicReference<>(initial);
        transitionHistory.add(initial);
    }

    /**
     * Attempt a state transition. Uses CAS to ensure only valid transitions
     * from the current state succeed. Two concurrent events cannot both succeed
     * if they target different next states from the same current state.
     */
    public boolean transition(State targetState) {
        while (true) {
            State current = currentState.get();

            if (!current.canTransitionTo(targetState)) {
                invalidTransitions.incrementAndGet();
                return false; // Invalid transition from current state
            }

            // CAS: only transition if state hasn't changed since we checked
            if (currentState.compareAndSet(current, targetState)) { // CAS = atomic swap; prevents two threads both transitioning
                transitionHistory.add(targetState);
                successfulTransitions.incrementAndGet();
                return true;
            }

            // CAS failed — state changed, re-evaluate
            rejectedTransitions.incrementAndGet();
            return false; // Don't loop — let caller decide retry policy
        }
    }

    public State getCurrentState() { return currentState.get(); }
    public int getSuccessfulTransitions() { return successfulTransitions.get(); }
    public int getRejectedTransitions() { return rejectedTransitions.get(); }
    public int getInvalidTransitions() { return invalidTransitions.get(); }

    /**
     * Verify transition history is valid: each consecutive pair must be a valid transition.
     */
    public boolean isHistoryValid() {
        for (int i = 0; i < transitionHistory.size() - 1; i++) {
            State from = transitionHistory.get(i);
            State to = transitionHistory.get(i + 1);
            if (!from.canTransitionTo(to)) {
                return false;
            }
        }
        return true;
    }

    public int getHistorySize() { return transitionHistory.size(); }
}

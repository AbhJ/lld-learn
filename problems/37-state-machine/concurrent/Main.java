/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — Multiple event threads, verify no invalid state ever observed

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent State Machine Demo ===\n");

        ConcurrentStateMachine sm = new ConcurrentStateMachine(State.CREATED);
        int threadCount = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicBoolean invalidStateObserved = new AtomicBoolean(false);
        AtomicInteger attempts = new AtomicInteger(0);

        System.out.println("Scenario: 10 threads try various transitions simultaneously.");
        System.out.println("  Some try CREATED->VALIDATED, others CREATED->CANCELLED, etc.");
        System.out.println("Expected: Only valid transitions succeed, no invalid state observed.\n");

        // Different threads try different transitions
        State[][] transitionAttempts = {
            {State.VALIDATED},                           // thread 0
            {State.CANCELLED},                          // thread 1
            {State.VALIDATED, State.PROCESSING},        // thread 2
            {State.CANCELLED},                          // thread 3
            {State.VALIDATED, State.PROCESSING, State.SHIPPED}, // thread 4
            {State.VALIDATED},                           // thread 5
            {State.CANCELLED},                          // thread 6
            {State.VALIDATED, State.PROCESSING, State.SHIPPED, State.DELIVERED}, // thread 7
            {State.VALIDATED, State.CANCELLED},          // thread 8
            {State.VALIDATED, State.PROCESSING},         // thread 9
        };

        for (int t = 0; t < threadCount; t++) {
            final State[] targetStates = transitionAttempts[t];
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (State target : targetStates) {
                        attempts.incrementAndGet();
                        sm.transition(target);

                        // Verify current state is always a valid State enum value
                        State observed = sm.getCurrentState();
                        if (observed == null) {
                            invalidStateObserved.set(true);
                        }
                    }
                } catch (Exception e) {
                    invalidStateObserved.set(true);
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            }, "EventThread-" + t).start();
        }

        startLatch.countDown();
        doneLatch.await();

        boolean historyValid = sm.isHistoryValid();

        System.out.println("--- Results ---");
        System.out.println("Final state: " + sm.getCurrentState());
        System.out.println("Transition attempts: " + attempts.get());
        System.out.println("Successful transitions: " + sm.getSuccessfulTransitions());
        System.out.println("Rejected (CAS conflict): " + sm.getRejectedTransitions());
        System.out.println("Invalid (wrong source state): " + sm.getInvalidTransitions());
        System.out.println("Transition history valid: " + historyValid);
        System.out.println("History length: " + sm.getHistorySize());
        System.out.println("Invalid state observed: " + invalidStateObserved.get());

        boolean passed = historyValid && !invalidStateObserved.get();
        System.out.println("\nCorrectness check: " + (passed ? "PASSED" : "FAILED"));
    }
}

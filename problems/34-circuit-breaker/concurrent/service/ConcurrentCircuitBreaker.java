/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/ConcurrentCircuitBreaker.java — AtomicReference<State> with CAS transitions

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Thread-safe circuit breaker using CAS for state transitions.
 *
 * States:
 * - CLOSED: Normal operation. Track failures.
 * - OPEN: All requests rejected immediately. Wait for reset timeout.
 * - HALF_OPEN: Allow limited requests through to test recovery.
 *
 * The key concurrency challenge: multiple threads may try to transition
 * the state simultaneously (e.g., from CLOSED to OPEN when threshold hit).
 * CAS ensures only ONE thread performs each transition.
 */
enum CircuitState { CLOSED, OPEN, HALF_OPEN } // enum = type-safe fixed states; used inside AtomicReference

class ConcurrentCircuitBreaker {
    private final CircuitBreakerConfig config;
    private final AtomicReference<CircuitState> state = new AtomicReference<>(CircuitState.CLOSED); // AtomicReference = CAS-based state transitions; only one thread wins each transition
    private final AtomicInteger failureCount = new AtomicInteger(0);   // AtomicInteger = lock-free; multiple threads increment safely
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicLong lastFailureTime = new AtomicLong(0);      // AtomicLong = timestamp updated by failing thread, read by all
    private final AtomicInteger halfOpenAttempts = new AtomicInteger(0);
    private final AtomicInteger rejectedCount = new AtomicInteger(0);
    private final AtomicInteger totalRequests = new AtomicInteger(0);

    public ConcurrentCircuitBreaker(CircuitBreakerConfig config) {
        this.config = config;
    }

    /**
     * Execute a request through the circuit breaker.
     * Returns the result or throws CircuitOpenException.
     */
    public <T> T execute(Supplier<T> request) {
        totalRequests.incrementAndGet();

        CircuitState currentState = state.get();

        switch (currentState) {
            case OPEN:
                if (shouldAttemptReset()) {
                    // Try to transition to HALF_OPEN
                    if (state.compareAndSet(CircuitState.OPEN, CircuitState.HALF_OPEN)) {
                        halfOpenAttempts.set(0);
                    }
                    // Fall through to HALF_OPEN handling
                } else {
                    rejectedCount.incrementAndGet();
                    throw new CircuitOpenException("Circuit is OPEN — request rejected");
                }
                break;
            case HALF_OPEN:
                if (halfOpenAttempts.incrementAndGet() > config.getHalfOpenMaxAttempts()) {
                    rejectedCount.incrementAndGet();
                    throw new CircuitOpenException("Circuit is HALF_OPEN — max attempts reached");
                }
                break;
            case CLOSED:
                // Normal flow
                break;
        }

        try {
            T result = request.get();
            onSuccess();
            return result;
        } catch (Exception e) {
            onFailure();
            throw e;
        }
    }

    private void onSuccess() {
        successCount.incrementAndGet();
        CircuitState currentState = state.get();
        if (currentState == CircuitState.HALF_OPEN) {
            // Success in HALF_OPEN — close the circuit
            state.compareAndSet(CircuitState.HALF_OPEN, CircuitState.CLOSED);
            failureCount.set(0);
        }
    }

    private void onFailure() {
        lastFailureTime.set(System.currentTimeMillis());
        int failures = failureCount.incrementAndGet();

        CircuitState currentState = state.get();
        if (currentState == CircuitState.HALF_OPEN) {
            // Failure in HALF_OPEN — reopen immediately
            state.compareAndSet(CircuitState.HALF_OPEN, CircuitState.OPEN);
        } else if (currentState == CircuitState.CLOSED && failures >= config.getFailureThreshold()) {
            // Threshold reached — open circuit (CAS ensures only one thread does this)
            state.compareAndSet(CircuitState.CLOSED, CircuitState.OPEN);
        }
    }

    private boolean shouldAttemptReset() {
        long elapsed = System.currentTimeMillis() - lastFailureTime.get();
        return elapsed >= config.getResetTimeoutMs();
    }

    public CircuitState getState() { return state.get(); }
    public int getFailureCount() { return failureCount.get(); }
    public int getSuccessCount() { return successCount.get(); }
    public int getRejectedCount() { return rejectedCount.get(); }
    public int getTotalRequests() { return totalRequests.get(); }
}

class CircuitOpenException extends RuntimeException { // extends RuntimeException = unchecked; no forced try/catch
    public CircuitOpenException(String message) {
        super(message);
    }
}

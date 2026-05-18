/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/CircuitBreaker.java — Sliding window metrics with atomic state transitions
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;

public class CircuitBreaker {
    // WHY AtomicReference for state: Lock-free state transitions using CAS.
    // Multiple threads can check state without blocking each other.
    private enum State { CLOSED, OPEN, HALF_OPEN } // enum = fixed set of states; type-safe instead of magic strings

    private final String name;
    private final CircuitBreakerConfig config;
    private final SlidingWindow window;                              // SlidingWindow = ring buffer for rolling failure rate
    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED); // AtomicReference = CAS-based lock-free state transitions
    private volatile long openedAt;                                  // volatile = all threads read the latest open timestamp
    private final AtomicInteger halfOpenSuccesses = new AtomicInteger(0); // AtomicInteger = lock-free probe tracking
    private final AtomicInteger halfOpenAttempts = new AtomicInteger(0);

    // Metrics — AtomicIntegers for lock-free concurrent updates from multiple threads
    private final AtomicInteger totalSuccess = new AtomicInteger(0);
    private final AtomicInteger totalFailure = new AtomicInteger(0);
    private final AtomicInteger totalRejected = new AtomicInteger(0);

    public CircuitBreaker(String name, CircuitBreakerConfig config) {
        this.name = name;
        this.config = config;
        this.window = new SlidingWindow(config.getWindowSize());
    }

    public <T> T execute(ServiceCall<T> call) throws Exception {
        State currentState = state.get();

        switch (currentState) {
            case OPEN:
                return handleOpen(call);
            case HALF_OPEN:
                return handleHalfOpen(call);
            default: // CLOSED
                return handleClosed(call);
        }
    }

    private <T> T handleClosed(ServiceCall<T> call) throws Exception {
        try {
            T result = call.call();
            window.record(true);
            totalSuccess.incrementAndGet();
            return result;
        } catch (Exception e) {
            window.record(false);
            totalFailure.incrementAndGet();
            // WHY rate-based: Trip only when failure RATE exceeds threshold over the window
            if (window.getCount() >= config.getWindowSize() &&
                window.getFailureRate() >= config.getFailureRateThreshold()) {
                tripOpen();
            }
            throw e;
        }
    }

    private <T> T handleOpen(ServiceCall<T> call) throws Exception {
        long elapsed = System.currentTimeMillis() - openedAt;
        if (elapsed >= config.getOpenTimeoutMs()) {
            // WHY CAS: Only one thread transitions to HALF_OPEN; others still fail fast
            if (state.compareAndSet(State.OPEN, State.HALF_OPEN)) {
                System.out.println("  [" + name + "] OPEN -> HALF_OPEN");
                halfOpenSuccesses.set(0);
                halfOpenAttempts.set(0);
            }
            return execute(call); // Retry in new state
        }
        totalRejected.incrementAndGet();
        throw new RuntimeException("Circuit OPEN - failing fast (" + (config.getOpenTimeoutMs() - elapsed) + "ms remaining)");
    }

    private <T> T handleHalfOpen(ServiceCall<T> call) throws Exception {
        int attempt = halfOpenAttempts.incrementAndGet();
        if (attempt > config.getHalfOpenProbeCount()) {
            // Exceeded probe count, wait for probes to finish
            totalRejected.incrementAndGet();
            throw new RuntimeException("Circuit HALF_OPEN - probe limit reached");
        }

        try {
            T result = call.call();
            int successes = halfOpenSuccesses.incrementAndGet();
            totalSuccess.incrementAndGet();
            System.out.println("  [" + name + "] Probe " + successes + "/" + config.getHalfOpenProbeCount() + " passed");
            // WHY configurable probe count: Single success may be a fluke;
            // require N consecutive successes for confidence.
            if (successes >= config.getHalfOpenProbeCount()) {
                if (state.compareAndSet(State.HALF_OPEN, State.CLOSED)) {
                    System.out.println("  [" + name + "] HALF_OPEN -> CLOSED");
                    window.reset();
                }
            }
            return result;
        } catch (Exception e) {
            totalFailure.incrementAndGet();
            tripOpen();
            throw e;
        }
    }

    private void tripOpen() {
        State prev = state.getAndSet(State.OPEN);
        if (prev != State.OPEN) {
            openedAt = System.currentTimeMillis();
            System.out.println("  [" + name + "] " + prev + " -> OPEN (failure rate: " +
                    String.format("%.0f%%", window.getFailureRate() * 100) + ")");
        }
    }

    public String getStateName() { return state.get().name(); }
    public String getMetrics() {
        return String.format("Metrics[success=%d, fail=%d, rejected=%d, windowRate=%.0f%%]",
                totalSuccess.get(), totalFailure.get(), totalRejected.get(),
                window.getFailureRate() * 100);
    }
}

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/CircuitBreaker.java — Wraps service calls with fail-fast protection using synchronized state
public class CircuitBreaker {
    private final String name;                    // private final = identifies this breaker instance
    private final CircuitBreakerConfig config;    // final = config never changes after construction
    private final CircuitBreakerMetrics metrics;
    private CircuitState currentState;            // private = mutable state; protected by synchronized methods

    public CircuitBreaker(String name, CircuitBreakerConfig config) {
        this.name = name;
        this.config = config;
        this.metrics = new CircuitBreakerMetrics();
        this.currentState = new ClosedState();
    }

    public synchronized <T> T execute(ServiceCall<T> call) throws Exception { // synchronized = only one thread executes at a time
        return currentState.execute(call, this);
    }

    public synchronized void transitionTo(CircuitState newState) { // synchronized = state transitions are atomic
        System.out.println("  [" + name + "] " + currentState.getName() + " -> " + newState.getName());
        this.currentState = newState;
    }

    public String getStateName() { return currentState.getName(); }
    public CircuitBreakerConfig getConfig() { return config; }
    public CircuitBreakerMetrics getMetrics() { return metrics; }
}

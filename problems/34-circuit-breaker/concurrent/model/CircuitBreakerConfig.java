/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/CircuitBreakerConfig.java — Configuration for circuit breaker behavior

class CircuitBreakerConfig {
    private final int failureThreshold;     // final = immutable config; safe to read from any thread
    private final long resetTimeoutMs;      // final = no synchronization needed to read these values
    private final int halfOpenMaxAttempts;  // private = encapsulated; accessed only via getters

    public CircuitBreakerConfig(int failureThreshold, long resetTimeoutMs, int halfOpenMaxAttempts) {
        this.failureThreshold = failureThreshold;
        this.resetTimeoutMs = resetTimeoutMs;
        this.halfOpenMaxAttempts = halfOpenMaxAttempts;
    }

    public int getFailureThreshold() { return failureThreshold; }
    public long getResetTimeoutMs() { return resetTimeoutMs; }
    public int getHalfOpenMaxAttempts() { return halfOpenMaxAttempts; }

    public static CircuitBreakerConfig defaultConfig() {
        return new CircuitBreakerConfig(5, 3000, 2);
    }

    public static CircuitBreakerConfig fastConfig() {
        return new CircuitBreakerConfig(3, 200, 1);
    }
}

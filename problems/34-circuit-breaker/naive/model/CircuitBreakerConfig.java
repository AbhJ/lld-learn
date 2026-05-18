/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/CircuitBreakerConfig.java — Configuration for thresholds and timeouts
public class CircuitBreakerConfig {
    private final int failureThreshold;     // private final = immutable config; set once, never modified
    private final long openTimeoutMs;       // how long to stay OPEN before trying HALF_OPEN
    private final int halfOpenTrialCount;   // private = only accessible via getters

    public CircuitBreakerConfig(int failureThreshold, long openTimeoutMs, int halfOpenTrialCount) {
        this.failureThreshold = failureThreshold;
        this.openTimeoutMs = openTimeoutMs;
        this.halfOpenTrialCount = halfOpenTrialCount;
    }

    public int getFailureThreshold() { return failureThreshold; }
    public long getOpenTimeoutMs() { return openTimeoutMs; }
    public int getHalfOpenTrialCount() { return halfOpenTrialCount; }
}

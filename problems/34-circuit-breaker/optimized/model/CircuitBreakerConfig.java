/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/CircuitBreakerConfig.java — Configuration with sliding window parameters
public class CircuitBreakerConfig {
    private final int windowSize;              // sliding window size = how many recent calls to consider
    private final double failureRateThreshold; // percentage-based = more robust than consecutive count
    private final long openTimeoutMs;          // final = immutable config safe to share across threads
    private final int halfOpenProbeCount;

    public CircuitBreakerConfig(int windowSize, double failureRateThreshold,
                                long openTimeoutMs, int halfOpenProbeCount) {
        this.windowSize = windowSize;
        this.failureRateThreshold = failureRateThreshold;
        this.openTimeoutMs = openTimeoutMs;
        this.halfOpenProbeCount = halfOpenProbeCount;
    }

    public int getWindowSize() { return windowSize; }
    public double getFailureRateThreshold() { return failureRateThreshold; }
    public long getOpenTimeoutMs() { return openTimeoutMs; }
    public int getHalfOpenProbeCount() { return halfOpenProbeCount; }
}

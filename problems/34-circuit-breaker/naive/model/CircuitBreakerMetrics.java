/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/CircuitBreakerMetrics.java — Tracks success/failure rates
import java.util.concurrent.atomic.AtomicInteger;

public class CircuitBreakerMetrics {
    private final AtomicInteger successCount = new AtomicInteger(0);  // private final = reference fixed; AtomicInteger for thread-safe counting
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicInteger rejectedCount = new AtomicInteger(0);

    public void recordSuccess() { successCount.incrementAndGet(); }
    public void recordFailure() { failureCount.incrementAndGet(); }
    public void recordRejected() { rejectedCount.incrementAndGet(); }

    public int getSuccessCount() { return successCount.get(); }
    public int getFailureCount() { return failureCount.get(); }
    public int getRejectedCount() { return rejectedCount.get(); }

    @Override
    public String toString() {
        return String.format("Metrics[success=%d, fail=%d, rejected=%d]",
                successCount.get(), failureCount.get(), rejectedCount.get());
    }
}

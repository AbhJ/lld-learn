/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/Throttler.java — Semaphore-based per-user rate limiting

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Per-user throttle using AtomicInteger for rate counting within a window.
 * Ensures no more than maxPerWindow notifications are delivered per user per window.
 */
class Throttler {
    private final int maxPerWindow;              // final = immutable config; safe for all threads to read
    private final ConcurrentHashMap<String, AtomicInteger> userCounters = new ConcurrentHashMap<>(); // ConcurrentHashMap = thread-safe map; per-bucket locking

    public Throttler(int maxPerWindow) {
        this.maxPerWindow = maxPerWindow;
    }

    /**
     * Try to acquire a permit for the given user.
     * Uses AtomicInteger with CAS loop to ensure thread-safe counting.
     * Returns true if under limit, false if throttled.
     */
    public boolean tryAcquire(String userId) {
        AtomicInteger counter = userCounters.computeIfAbsent(userId, k -> new AtomicInteger(0));

        while (true) {
            int current = counter.get();
            if (current >= maxPerWindow) {
                return false; // throttled
            }
            if (counter.compareAndSet(current, current + 1)) { // CAS = atomic compare-and-swap; only succeeds if value unchanged
                return true; // acquired
            }
            // CAS failed — another thread incremented, retry loop
        }
    }

    /**
     * Reset counters (called at start of new window).
     */
    public void resetWindow() {
        userCounters.clear();
    }

    public int getCount(String userId) {
        AtomicInteger counter = userCounters.get(userId);
        return counter == null ? 0 : counter.get();
    }

    public int getMaxPerWindow() { return maxPerWindow; }
}

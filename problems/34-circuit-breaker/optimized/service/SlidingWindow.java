/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/SlidingWindow.java — Ring buffer tracking last N call outcomes for failure rate calculation
public class SlidingWindow {
    // WHY ring buffer: Fixed memory O(windowSize), O(1) record/query,
    // naturally evicts old entries without shifting. Gives rolling failure rate
    // instead of naive consecutive-count which resets on a single success.
    private final boolean[] outcomes; // boolean[] ring buffer = fixed memory, automatic eviction of oldest
    private int head = 0;            // private = internal pointer; guarded by synchronized
    private int count = 0;
    private int failures = 0;

    public SlidingWindow(int size) {
        this.outcomes = new boolean[size];
    }

    public synchronized void record(boolean success) { // synchronized = only one thread records at a time
        if (count == outcomes.length) {
            // Evict oldest entry
            if (!outcomes[head]) {
                failures--; // Old failure being evicted
            }
        } else {
            count++;
        }
        outcomes[head] = success;
        if (!success) failures++;
        head = (head + 1) % outcomes.length;
    }

    // WHY failure rate (not count): A single timeout among 100 successes
    // shouldn't trip the breaker — percentage-based is more robust.
    public synchronized double getFailureRate() {
        if (count == 0) return 0.0;
        return (double) failures / count;
    }

    public synchronized int getFailureCount() { return failures; }
    public synchronized int getCount() { return count; }

    public synchronized void reset() {
        head = 0;
        count = 0;
        failures = 0;
    }
}

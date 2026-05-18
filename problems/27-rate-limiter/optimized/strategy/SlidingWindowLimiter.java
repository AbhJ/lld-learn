/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/SlidingWindowLimiter.java — Optimized: atomic operations with circular buffer
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLongArray;

public class SlidingWindowLimiter implements RateLimiter { // implements = fulfills the RateLimiter contract
    private int maxRequests;
    private long windowSizeMs;
    private int bucketCount;                             // circular buffer size; avoids scanning all timestamps
    private long bucketSizeMs;
    private Map<String, ClientWindow> windows;           // ConcurrentHashMap = per-client access without global lock

    public SlidingWindowLimiter(int maxRequests, long windowSizeMs) {
        this.maxRequests = maxRequests;
        this.windowSizeMs = windowSizeMs;
        // WHY: Circular buffer with 10 sub-windows gives O(1) check without scanning timestamps
        this.bucketCount = 10;
        this.bucketSizeMs = windowSizeMs / bucketCount;
        this.windows = new ConcurrentHashMap<>();
    }

    @Override
    public boolean allowRequest(Request request) {
        ClientWindow window = windows.computeIfAbsent(
            request.getClientId(), k -> new ClientWindow(bucketCount));
        return window.tryIncrement(request.getTimestamp(), maxRequests, bucketSizeMs, bucketCount, windowSizeMs);
    }

    @Override
    public String getAlgorithmName() { return "Sliding Window (atomic circular buffer)"; }

    private static class ClientWindow {                   // static = no reference to outer class needed
        private AtomicLongArray bucketTimestamps;         // AtomicLongArray = thread-safe array of timestamps
        private AtomicInteger[] bucketCounts;             // AtomicInteger = lock-free counter per bucket
        private int bucketCount;

        ClientWindow(int bucketCount) {
            this.bucketCount = bucketCount;
            this.bucketTimestamps = new AtomicLongArray(bucketCount);
            this.bucketCounts = new AtomicInteger[bucketCount];
            for (int i = 0; i < bucketCount; i++) {
                bucketCounts[i] = new AtomicInteger(0);
            }
        }

        boolean tryIncrement(long now, int maxRequests, long bucketSizeMs, int bucketCount, long windowSizeMs) {
            int currentBucket = (int) ((now / bucketSizeMs) % bucketCount);

            // Reset bucket if it's from a previous window cycle
            long bucketTime = bucketTimestamps.get(currentBucket);
            if (now - bucketTime >= windowSizeMs) {
                bucketTimestamps.set(currentBucket, now);
                bucketCounts[currentBucket].set(0);
            }

            // Count requests across all active buckets
            int total = 0;
            for (int i = 0; i < bucketCount; i++) {
                long ts = bucketTimestamps.get(i);
                if (now - ts < windowSizeMs) {
                    total += bucketCounts[i].get();
                }
            }

            if (total < maxRequests) {
                bucketCounts[currentBucket].incrementAndGet();
                return true;
            }
            return false;
        }
    }
}

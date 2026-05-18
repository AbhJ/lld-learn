/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/TokenBucketRateLimiter.java — AtomicInteger with CAS loop for token bucket

import java.util.concurrent.atomic.AtomicInteger;

public class TokenBucketRateLimiter {
    private final AtomicInteger tokens;    // AtomicInteger = thread-safe counter; CAS ensures no lost decrements
    private final int maxTokens;           // final = immutable config; safe for all threads to read

    public TokenBucketRateLimiter(int maxTokens) {
        this.maxTokens = maxTokens;
        this.tokens = new AtomicInteger(maxTokens);
    }

    /**
     * Try to consume one token. Uses CAS loop to ensure atomic decrement.
     * Only succeeds if tokens > 0 at the moment of CAS.
     */
    public RateLimitResult tryAcquire() {
        while (true) {
            int current = tokens.get();
            if (current <= 0) {
                return new RateLimitResult(false, 0);
            }
            if (tokens.compareAndSet(current, current - 1)) { // CAS = atomic compare-and-swap; succeeds only if no other thread changed it
                return new RateLimitResult(true, current - 1);
            }
            // CAS failed means another thread decremented first; retry with fresh value
        }
    }

    /**
     * Refill tokens (called by a background refill mechanism).
     */
    public void refill(int count) {
        while (true) {
            int current = tokens.get();
            int newVal = Math.min(current + count, maxTokens);
            if (tokens.compareAndSet(current, newVal)) {
                return;
            }
        }
    }

    public int getAvailableTokens() {
        return tokens.get();
    }

    public int getMaxTokens() {
        return maxTokens;
    }
}

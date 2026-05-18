/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/RateLimiter.java — Token bucket rate limiter per user with configurable limits

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Optimized: Token bucket rate limiter with per-user tracking.
 * Uses ConcurrentHashMap for thread safety without coarse-grained locking.
 */
public class RateLimiter {
    private int maxPerMinute;
    private ConcurrentHashMap<String, TokenBucket> buckets; // ConcurrentHashMap = thread-safe map; no global lock needed

    public RateLimiter(int maxPerMinute) {
        this.maxPerMinute = maxPerMinute;
        this.buckets = new ConcurrentHashMap<>();
    }

    public boolean allowSend(String userId, String channelType) {
        String key = userId + ":" + channelType;
        TokenBucket bucket = buckets.computeIfAbsent(key, k -> new TokenBucket(maxPerMinute));
        return bucket.tryConsume();
    }

    public void reset(String userId, String channelType) {
        buckets.remove(userId + ":" + channelType);
    }

    private static class TokenBucket {            // static inner class = doesn't need reference to outer RateLimiter
        private int tokens;
        private int maxTokens;
        private long lastRefillTime;
        private static final long REFILL_INTERVAL_MS = 60_000; // static final = compile-time constant shared by all

        TokenBucket(int maxTokens) {
            this.maxTokens = maxTokens;
            this.tokens = maxTokens;
            this.lastRefillTime = System.currentTimeMillis();
        }

        synchronized boolean tryConsume() {       // synchronized = only one thread at a time can consume
            refill();
            if (tokens > 0) {
                tokens--;
                return true;
            }
            return false;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRefillTime;
            if (elapsed >= REFILL_INTERVAL_MS) {
                tokens = maxTokens;
                lastRefillTime = now;
            }
        }
    }
}

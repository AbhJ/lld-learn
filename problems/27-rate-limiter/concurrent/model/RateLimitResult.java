/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/RateLimitResult.java — Result of a rate limit check

public class RateLimitResult {
    private final boolean allowed;          // final = immutable after construction; safe to share across threads
    private final int remainingTokens;      // final = set once; no synchronization needed to read

    public RateLimitResult(boolean allowed, int remainingTokens) {
        this.allowed = allowed;
        this.remainingTokens = remainingTokens;
    }

    public boolean isAllowed() { return allowed; }
    public int getRemainingTokens() { return remainingTokens; }
}

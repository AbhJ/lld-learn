/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/PoolConfig.java — Pool sizing configuration
public class PoolConfig {
    private int minSize;            // private = encapsulates minimum pool capacity
    private int maxSize;            // private = encapsulates maximum pool capacity
    private long borrowTimeoutMs;   // private = max wait time before giving up

    public PoolConfig(int minSize, int maxSize, long borrowTimeoutMs) {
        this.minSize = minSize; this.maxSize = maxSize; this.borrowTimeoutMs = borrowTimeoutMs;
    }
    public int getMinSize() { return minSize; }
    public int getMaxSize() { return maxSize; }
    public long getBorrowTimeoutMs() { return borrowTimeoutMs; }
}

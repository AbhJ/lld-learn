/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/PoolConfig.java — Pool sizing and timing configuration
public class PoolConfig {
    private final int minSize;         // private final = immutable after construction
    private final int maxSize;         // final = safe to share between threads without sync
    private final long maxIdleTimeMs;  // private = only accessible through getters

    public PoolConfig(int minSize, int maxSize, long maxIdleTimeMs) {
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.maxIdleTimeMs = maxIdleTimeMs;
    }

    public int getMinSize() { return minSize; }
    public int getMaxSize() { return maxSize; }
    public long getMaxIdleTimeMs() { return maxIdleTimeMs; }
}

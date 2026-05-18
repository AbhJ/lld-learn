/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/PoolConfig.java — Pool sizing and eviction configuration
public class PoolConfig {
    private final int minSize;              // final = immutable; pool never shrinks below this
    private final int maxSize;              // Semaphore permit count derived from this
    private final long maxIdleTimeMs;       // evictor removes objects idle longer than this
    private final long evictionIntervalMs;  // how often the background evictor thread runs

    public PoolConfig(int minSize, int maxSize, long maxIdleTimeMs, long evictionIntervalMs) {
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.maxIdleTimeMs = maxIdleTimeMs;
        this.evictionIntervalMs = evictionIntervalMs;
    }

    public int getMinSize() { return minSize; }
    public int getMaxSize() { return maxSize; }
    public long getMaxIdleTimeMs() { return maxIdleTimeMs; }
    public long getEvictionIntervalMs() { return evictionIntervalMs; }
}

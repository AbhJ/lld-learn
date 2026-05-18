/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/PoolConfig.java — Configuration for pool size, queue capacity, and keep-alive time
public class PoolConfig {
    private final int coreSize;          // final = value set once in constructor, never changes
    private final int maxSize;           // private = only this class can read/write these fields
    private final int queueCapacity;     // final = guarantees immutability after construction
    private final long keepAliveTimeMs;  // private final = safe to share across threads without locks

    public PoolConfig(int coreSize, int maxSize, int queueCapacity, long keepAliveTimeMs) {
        if (coreSize <= 0 || maxSize < coreSize || queueCapacity <= 0) {
            throw new IllegalArgumentException("Invalid pool configuration");
        }
        this.coreSize = coreSize;
        this.maxSize = maxSize;
        this.queueCapacity = queueCapacity;
        this.keepAliveTimeMs = keepAliveTimeMs;
    }

    public int getCoreSize() { return coreSize; }
    public int getMaxSize() { return maxSize; }
    public int getQueueCapacity() { return queueCapacity; }
    public long getKeepAliveTimeMs() { return keepAliveTimeMs; }

    @Override
    public String toString() {
        return String.format("PoolConfig[core=%d, max=%d, queue=%d, keepAlive=%dms]",
                coreSize, maxSize, queueCapacity, keepAliveTimeMs);
    }
}

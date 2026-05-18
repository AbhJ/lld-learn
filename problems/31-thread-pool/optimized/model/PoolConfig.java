/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/PoolConfig.java — Configuration for work-stealing pool
public class PoolConfig {
    private final int workerCount;         // final = immutable after construction; safe to share
    private final int localQueueCapacity;  // per-worker queue size for work-stealing deques

    public PoolConfig(int workerCount, int localQueueCapacity) {
        if (workerCount <= 0 || localQueueCapacity <= 0) {
            throw new IllegalArgumentException("Invalid pool configuration");
        }
        this.workerCount = workerCount;
        this.localQueueCapacity = localQueueCapacity;
    }

    public int getWorkerCount() { return workerCount; }
    public int getLocalQueueCapacity() { return localQueueCapacity; }

    @Override
    public String toString() {
        return String.format("PoolConfig[workers=%d, localQueue=%d]", workerCount, localQueueCapacity);
    }
}

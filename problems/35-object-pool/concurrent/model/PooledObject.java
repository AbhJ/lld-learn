/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/PooledObject.java — A pooled resource with ID and usage tracking

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class PooledObject {
    private final int id;                                          // final = safe publication; readable from any thread
    private final AtomicBoolean inUse = new AtomicBoolean(false);  // AtomicBoolean = CAS-based acquire prevents double-borrow
    private final AtomicInteger usageCount = new AtomicInteger(0); // AtomicInteger = thread-safe usage counter

    public PooledObject(int id) {
        this.id = id;
    }

    public int getId() { return id; }

    public boolean acquire() {
        return inUse.compareAndSet(false, true); // CAS = only one thread wins; prevents double-borrow
    }

    public void release() {
        inUse.set(false);
    }

    public boolean isInUse() { return inUse.get(); }

    public void use() {
        usageCount.incrementAndGet();
    }

    public int getUsageCount() { return usageCount.get(); }

    @Override
    public String toString() {
        return "PooledObject[id=" + id + ", inUse=" + inUse.get() + "]";
    }
}

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/PooledObject.java — Wraps pooled object with timestamp metadata
public class PooledObject<T> {
    private final T object;                // final = wrapped resource never changes
    private volatile long lastReturnedAt;  // volatile = evictor thread reads this without lock

    public PooledObject(T object) {
        this.object = object;
        this.lastReturnedAt = System.currentTimeMillis();
    }

    public T getObject() { return object; }
    public void markReturned() { lastReturnedAt = System.currentTimeMillis(); }
    public long getIdleTimeMs() { return System.currentTimeMillis() - lastReturnedAt; }
}

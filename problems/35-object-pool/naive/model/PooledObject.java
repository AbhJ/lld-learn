/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/PooledObject.java — Wraps a pooled object with tracking metadata
public class PooledObject<T> { // <T> = generic; wraps any type of pooled resource
    private final T object;           // private final = the wrapped resource; never swapped
    private final long createdAt;     // final = tracks object age for eviction decisions
    private long lastReturnedAt;      // private = mutable timestamp; updated on each return
    private int borrowCount;

    public PooledObject(T object) {
        this.object = object;
        this.createdAt = System.currentTimeMillis();
        this.lastReturnedAt = createdAt;
    }

    public T getObject() { return object; }
    public long getCreatedAt() { return createdAt; }
    public int getBorrowCount() { return borrowCount; }

    public void markBorrowed() { borrowCount++; }
    public void markReturned() { lastReturnedAt = System.currentTimeMillis(); }
    public long getIdleTimeMs() { return System.currentTimeMillis() - lastReturnedAt; }
}

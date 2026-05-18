/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Connection.java — Simulated database connection

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

class Connection {
    private static final AtomicInteger counter = new AtomicInteger(0); // static = shared across all instances; AtomicInteger = thread-safe ID generator
    private final String connectionId;          // final = set once; safe for threads to read without synchronization
    private final AtomicBoolean inUse = new AtomicBoolean(false); // AtomicBoolean = CAS-based acquire/release; no locks needed
    private final long createdAt;               // final = immutable timestamp; safe publication
    private volatile long lastUsedAt;           // volatile = visible to all threads immediately when updated
    private volatile boolean closed = false;    // volatile = shutdown flag seen by all threads instantly

    public Connection() {
        this.connectionId = "CONN-" + counter.incrementAndGet();
        this.createdAt = System.currentTimeMillis();
        this.lastUsedAt = this.createdAt;
    }

    public boolean acquire() {
        return inUse.compareAndSet(false, true); // CAS = only one thread can acquire; others get false
    }

    public void release() {
        lastUsedAt = System.currentTimeMillis();
        inUse.set(false);
    }

    public void close() {
        closed = true;
    }

    public boolean isInUse() { return inUse.get(); }
    public boolean isClosed() { return closed; }
    public String getConnectionId() { return connectionId; }
    public long getCreatedAt() { return createdAt; }
    public long getLastUsedAt() { return lastUsedAt; }

    /**
     * Simulate executing a query on this connection.
     */
    public String execute(String query) {
        if (closed) throw new IllegalStateException("Connection is closed");
        if (!inUse.get()) throw new IllegalStateException("Connection not acquired");
        lastUsedAt = System.currentTimeMillis();
        return "Result of: " + query + " [via " + connectionId + "]";
    }

    @Override
    public String toString() {
        return connectionId + (inUse.get() ? " [IN USE]" : " [IDLE]");
    }

    public static void resetCounter() { counter.set(0); }
}

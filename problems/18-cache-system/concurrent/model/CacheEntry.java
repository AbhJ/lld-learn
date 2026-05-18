/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/CacheEntry.java — Cache entry with expiration tracking

class CacheEntry<V> {
    private final String key;                    // final = set once, never changes; safe for threads to read
    private volatile V value;                    // volatile = value writes visible to all threads immediately
    private volatile long createdAt;             // volatile = timestamp visible across threads after update
    private volatile long lastAccessedAt;        // volatile = access time visible to all threads
    private final long ttlMillis;                // final = immutable TTL; safe for concurrent reads

    public CacheEntry(String key, V value, long ttlMillis) {
        this.key = key;
        this.value = value;
        this.ttlMillis = ttlMillis;
        this.createdAt = System.currentTimeMillis();
        this.lastAccessedAt = this.createdAt;
    }

    public String getKey() { return key; }
    public V getValue() { return value; }
    public long getLastAccessedAt() { return lastAccessedAt; }

    public void setValue(V value) {
        this.value = value;
        this.createdAt = System.currentTimeMillis();
        this.lastAccessedAt = this.createdAt;
    }

    public void touch() {
        this.lastAccessedAt = System.currentTimeMillis();
    }

    public boolean isExpired() {
        return (System.currentTimeMillis() - createdAt) > ttlMillis;
    }

    @Override
    public String toString() {
        return key + "=" + value + (isExpired() ? " [EXPIRED]" : " [VALID]");
    }
}

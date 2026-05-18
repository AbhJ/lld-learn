/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/CacheEntry.java — Cached key-value pair with metadata for eviction decisions

public class CacheEntry<V> {
    private String key;
    private V value;
    private long createdAt;
    private long lastAccessedAt;
    private int accessCount;
    private long ttlMillis; // -1 means no TTL

    public CacheEntry(String key, V value) {
        this.key = key;
        this.value = value;
        this.createdAt = System.currentTimeMillis();
        this.lastAccessedAt = this.createdAt;
        this.accessCount = 1;
        this.ttlMillis = -1;
    }

    public CacheEntry(String key, V value, long ttlMillis) {
        this(key, value);
        this.ttlMillis = ttlMillis;
    }

    public void access() {
        this.lastAccessedAt = System.currentTimeMillis();
        this.accessCount++;
    }

    public boolean isExpired() {
        if (ttlMillis < 0) return false;
        return System.currentTimeMillis() - createdAt > ttlMillis;
    }

    public String getKey() { return key; }
    public V getValue() { return value; }
    public void setValue(V value) { this.value = value; this.lastAccessedAt = System.currentTimeMillis(); }
    public long getCreatedAt() { return createdAt; }
    public long getLastAccessedAt() { return lastAccessedAt; }
    public int getAccessCount() { return accessCount; }
    public long getTtlMillis() { return ttlMillis; }

    @Override
    public String toString() {
        return String.format("Entry[%s=%s, accesses=%d]", key, value, accessCount);
    }
}

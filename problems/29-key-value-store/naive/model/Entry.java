/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Entry.java — Key-value entry with TTL
public class Entry {
    private String key;             // private = encapsulated; accessed only via getter
    private String value;           // private = mutable; can be updated via setValue()
    private long createdAt;
    private long expiresAt;         // private = 0 means no expiry; positive means TTL-based

    public Entry(String key, String value, long ttlMs) {
        this.key = key; this.value = value; this.createdAt = System.currentTimeMillis();
        this.expiresAt = (ttlMs > 0) ? createdAt + ttlMs : 0;
    }

    public String getKey() { return key; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public boolean isExpired() { return expiresAt > 0 && System.currentTimeMillis() > expiresAt; }
    public long getRemainingTtl() { return expiresAt == 0 ? -1 : Math.max(0, expiresAt - System.currentTimeMillis()); }
}

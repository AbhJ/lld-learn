/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/URLMapping.java — Maps short code to original URL
public class URLMapping {
    private String shortCode;       // private = encapsulates the generated short code
    private String longUrl;         // private = original URL hidden behind getter
    private long createdAt;
    private long expiresAt;
    private boolean active;         // private = mutable state; controlled via deactivate()

    public URLMapping(String shortCode, String longUrl, long ttlMs) {
        this.shortCode = shortCode; this.longUrl = longUrl;
        this.createdAt = System.currentTimeMillis();
        this.expiresAt = (ttlMs > 0) ? createdAt + ttlMs : 0;
        this.active = true;
    }

    public String getShortCode() { return shortCode; }
    public String getLongUrl() { return longUrl; }
    public boolean isActive() { return active; }
    public void deactivate() { this.active = false; }
    public boolean isExpired() { return expiresAt > 0 && System.currentTimeMillis() > expiresAt; }
    @Override public String toString() { return shortCode + " -> " + longUrl; }
}

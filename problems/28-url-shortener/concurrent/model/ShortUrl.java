/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/ShortUrl.java — Mapping between short code and original URL

public class ShortUrl {
    private final String shortCode;     // final = immutable after construction; safe to share across threads
    private final String originalUrl;   // final = never changes; no synchronization needed to read
    private final long createdAt;       // final = safe publication guaranteed by JMM

    public ShortUrl(String shortCode, String originalUrl) {
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.createdAt = System.nanoTime();
    }

    public String getShortCode() { return shortCode; }
    public String getOriginalUrl() { return originalUrl; }
    public long getCreatedAt() { return createdAt; }
}

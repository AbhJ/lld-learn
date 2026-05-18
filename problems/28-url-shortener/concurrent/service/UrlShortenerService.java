/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/UrlShortenerService.java — AtomicLong counter + ConcurrentHashMap.putIfAbsent

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class UrlShortenerService {
    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"; // static final = shared constant
    private static final int CODE_LENGTH = 7;

    private final AtomicLong counter = new AtomicLong(100000);            // AtomicLong = thread-safe sequential ID; getAndIncrement is atomic
    private final ConcurrentHashMap<String, ShortUrl> codeToUrl = new ConcurrentHashMap<>(); // ConcurrentHashMap = lock-free reads; putIfAbsent is atomic
    private final ConcurrentHashMap<String, String> urlToCode = new ConcurrentHashMap<>();   // ConcurrentHashMap = atomic dedup check across threads

    /**
     * Shorten a URL. Uses AtomicLong for unique sequential ID generation
     * and putIfAbsent for collision-free registration.
     */
    public String shorten(String originalUrl) {
        // Check if already shortened
        String existingCode = urlToCode.get(originalUrl);
        if (existingCode != null) return existingCode;

        // Generate unique code from sequential counter
        long id = counter.getAndIncrement();
        String code = encode(id);

        ShortUrl shortUrl = new ShortUrl(code, originalUrl);

        // Atomically register — putIfAbsent guarantees no collision
        ShortUrl existing = codeToUrl.putIfAbsent(code, shortUrl);
        if (existing != null) {
            // Extremely unlikely with sequential IDs, but handle gracefully
            return existing.getShortCode();
        }

        // Register reverse mapping (race-safe: first one wins)
        urlToCode.putIfAbsent(originalUrl, code);
        return code;
    }

    public String resolve(String code) {
        ShortUrl shortUrl = codeToUrl.get(code);
        return shortUrl != null ? shortUrl.getOriginalUrl() : null;
    }

    private String encode(long num) {
        StringBuilder sb = new StringBuilder();
        while (sb.length() < CODE_LENGTH) {
            sb.append(BASE62.charAt((int) (num % 62)));
            num /= 62;
        }
        return sb.reverse().toString();
    }

    public int getUrlCount() { return codeToUrl.size(); }
    public long getCounterValue() { return counter.get(); }
}

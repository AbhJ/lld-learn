/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/URLShortener.java — Optimized: AtomicLong counter guarantees unique codes
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class URLShortener {
    private String baseUrl;
    private EncodingStrategy strategy;
    private Map<String, URLMapping> shortToMapping;      // HashMap = O(1) lookup by short code
    private Map<String, String> longToShort;             // HashMap = reverse index to prevent duplicate entries
    private AtomicLong idCounter;                        // AtomicLong = thread-safe counter; guarantees unique IDs without locks

    public URLShortener(String baseUrl, EncodingStrategy strategy) {
        this.baseUrl = baseUrl; this.strategy = strategy;
        this.shortToMapping = new HashMap<>(); this.longToShort = new HashMap<>();
        this.idCounter = new AtomicLong(100000);
    }

    public String shorten(String longUrl) {
        if (longToShort.containsKey(longUrl)) return baseUrl + "/" + longToShort.get(longUrl);

        // WHY: getAndIncrement is atomic — guaranteed unique, no collision possible
        long id = idCounter.getAndIncrement();
        String shortCode = strategy.encode(longUrl, id);

        URLMapping mapping = new URLMapping(shortCode, longUrl, 0);
        shortToMapping.put(shortCode, mapping);
        longToShort.put(longUrl, shortCode);
        return baseUrl + "/" + shortCode;
    }

    public String resolve(String shortUrl) {
        String code = shortUrl.replace(baseUrl + "/", "");
        URLMapping mapping = shortToMapping.get(code);
        if (mapping == null) throw new IllegalArgumentException("Not found: " + shortUrl);
        if (!mapping.isActive()) throw new IllegalStateException("Deactivated: " + shortUrl);
        if (mapping.isExpired()) throw new IllegalStateException("Expired: " + shortUrl);
        return mapping.getLongUrl();
    }

    public int getTotalMappings() { return shortToMapping.size(); }
}

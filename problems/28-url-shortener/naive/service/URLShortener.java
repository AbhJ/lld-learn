/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/URLShortener.java — Naive: random code with collision check loop
import java.util.HashMap;
import java.util.Map;

public class URLShortener {
    private String baseUrl;
    private EncodingStrategy strategy;                   // private = pluggable encoding hidden from callers
    private Map<String, URLMapping> shortToMapping;      // Map = O(1) lookup from short code to mapping
    private Map<String, String> longToShort;             // Map = reverse lookup to detect duplicate URLs
    private long idCounter;

    public URLShortener(String baseUrl, EncodingStrategy strategy) {
        this.baseUrl = baseUrl; this.strategy = strategy;
        this.shortToMapping = new HashMap<>(); this.longToShort = new HashMap<>();
        this.idCounter = 100000;
    }

    public String shorten(String longUrl) {
        if (longToShort.containsKey(longUrl)) return baseUrl + "/" + longToShort.get(longUrl);

        // Naive: generate random code and check for collisions — may loop many times
        String shortCode;
        int attempts = 0;
        do {
            shortCode = strategy.encode(longUrl, ++idCounter);
            attempts++;
            if (attempts > 100) throw new RuntimeException("Unable to generate unique code");
        } while (shortToMapping.containsKey(shortCode));

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

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Demonstrates URL shortener with atomic counter (no collision checks)
public class Main {
    public static void main(String[] args) {
        System.out.println("=== URL Shortener Demo (Optimized) ===");
        System.out.println("Optimization: AtomicLong counter + Base62 = guaranteed unique, O(1)\n");

        URLShortener shortener = new URLShortener("https://short.ly", new EncodingStrategy.Base62Encoding());

        System.out.println("--- Shortening URLs (counter-based, no collision check) ---");
        String s1 = shortener.shorten("https://www.example.com/very/long/path");
        System.out.println("https://www.example.com/very/long/path → " + s1);

        String s2 = shortener.shorten("https://docs.google.com/doc/abc123");
        System.out.println("https://docs.google.com/doc/abc123 → " + s2);

        System.out.println("\n--- Resolution ---");
        System.out.println(s1 + " → " + shortener.resolve(s1));

        System.out.println("\n--- Duplicate URL returns same code ---");
        String s3 = shortener.shorten("https://www.example.com/very/long/path");
        System.out.println("Same URL again → " + s3 + " (same: " + s1.equals(s3) + ")");

        System.out.println("\nTotal mappings: " + shortener.getTotalMappings());
        System.out.println("\n=== Demo Complete ===");
    }
}

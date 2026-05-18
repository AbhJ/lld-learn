/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/EncodingStrategy.java — Counter-based Base62 encoding (no collisions)
// DESIGN PATTERN: Strategy
import java.util.concurrent.atomic.AtomicLong;

public interface EncodingStrategy {            // interface = contract; encoding algorithms are pluggable
    String encode(String url, long id);

    // Base62 gives short, URL-safe codes: 62^6 = 56 billion possible codes
    class Base62Encoding implements EncodingStrategy { // implements = fulfills the EncodingStrategy contract
        private static final String CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"; // static final = shared constant; never changes

        @Override
        public String encode(String url, long id) {
            StringBuilder sb = new StringBuilder();
            long num = id;
            while (num > 0) {
                sb.append(CHARS.charAt((int) (num % 62)));
                num /= 62;
            }
            while (sb.length() < 6) sb.append('0');
            return sb.reverse().toString();
        }
    }
}

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/EncodingStrategy.java — URL encoding algorithms
// DESIGN PATTERN: Strategy
import java.util.Random;

public interface EncodingStrategy {            // interface = contract; different encodings can be swapped in
    String encode(String url, long id);

    // Naive: random generation — may collide, requires collision check loop
    class RandomEncoding implements EncodingStrategy { // implements = fulfills the EncodingStrategy contract
        private static final String CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"; // static final = shared constant
        private Random random = new Random();

        @Override
        public String encode(String url, long id) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 7; i++) sb.append(CHARS.charAt(random.nextInt(62)));
            return sb.toString();
        }
    }
}

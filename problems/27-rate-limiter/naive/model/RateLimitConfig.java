/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/RateLimitConfig.java — Configuration for rate limiters
public class RateLimitConfig {
    private int capacity;           // private = only accessible via getter; encapsulates config
    private long windowSizeMs;      // private = hides implementation detail from outside

    public RateLimitConfig(int capacity, long windowSizeMs) {
        this.capacity = capacity; this.windowSizeMs = windowSizeMs;
    }
    public int getCapacity() { return capacity; }
    public long getWindowSizeMs() { return windowSizeMs; }
}

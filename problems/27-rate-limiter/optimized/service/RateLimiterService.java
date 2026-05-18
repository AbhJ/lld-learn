/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/RateLimiterService.java — Creates rate limiters
public class RateLimiterService {
    public static RateLimiter createSlidingWindow(int maxRequests, long windowMs) { // static = factory method; no instance needed
        return new SlidingWindowLimiter(maxRequests, windowMs);
    }
}

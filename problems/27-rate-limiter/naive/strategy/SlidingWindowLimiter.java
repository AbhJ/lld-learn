/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/SlidingWindowLimiter.java — Naive: synchronized counter with LinkedList
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class SlidingWindowLimiter implements RateLimiter { // implements = fulfills the RateLimiter contract
    private int maxRequests;
    private long windowSizeMs;
    private Map<String, Queue<Long>> requestLogs; // Map = per-client request timestamps; Queue = FIFO ordering

    public SlidingWindowLimiter(int maxRequests, long windowSizeMs) {
        this.maxRequests = maxRequests; this.windowSizeMs = windowSizeMs;
        this.requestLogs = new HashMap<>();
    }

    @Override
    public synchronized boolean allowRequest(Request request) { // synchronized = locks entire object; only one thread enters at a time
        String clientId = request.getClientId();
        long now = request.getTimestamp();
        Queue<Long> log = requestLogs.computeIfAbsent(clientId, k -> new LinkedList<>());
        // Remove expired entries
        while (!log.isEmpty() && log.peek() <= now - windowSizeMs) log.poll();
        if (log.size() < maxRequests) { log.add(now); return true; }
        return false;
    }

    @Override public String getAlgorithmName() { return "Sliding Window (synchronized)"; }
}

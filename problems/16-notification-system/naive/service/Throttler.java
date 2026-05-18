/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/Throttler.java — Rate-limits notification delivery per user/channel

import java.util.*;
import java.time.LocalDateTime;

public class Throttler {
    private int maxPerMinute;                    // private = only this class controls the limit
    private Map<String, List<LocalDateTime>> sendTimes; // key: userId+channel

    public Throttler(int maxPerMinute) {
        this.maxPerMinute = maxPerMinute;
        this.sendTimes = new HashMap<>();
    }

    public boolean allowSend(String userId, String channelType) {
        String key = userId + ":" + channelType;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = now.minusMinutes(1);

        List<LocalDateTime> times = sendTimes.computeIfAbsent(key, k -> new ArrayList<>());

        // Remove expired entries
        times.removeIf(t -> t.isBefore(windowStart));

        if (times.size() >= maxPerMinute) {
            return false;
        }

        times.add(now);
        return true;
    }

    public void reset(String userId, String channelType) {
        String key = userId + ":" + channelType;
        sendTimes.remove(key);
    }

    public void resetAll() {
        sendTimes.clear();
    }

    public int getMaxPerMinute() { return maxPerMinute; }
    public void setMaxPerMinute(int max) { this.maxPerMinute = max; }
}

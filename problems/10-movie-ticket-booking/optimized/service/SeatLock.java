/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/SeatLock.java — ConcurrentHashMap seat locking with per-show ReentrantLock

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

class SeatLock {
    private String seatId;
    private String showId;
    private String userId;
    private long expiryTime;

    public SeatLock(String seatId, String showId, String userId, long lockDurationMs) {
        this.seatId = seatId; this.showId = showId; this.userId = userId;
        this.expiryTime = System.currentTimeMillis() + lockDurationMs;
    }

    public boolean isExpired() { return System.currentTimeMillis() > expiryTime; }
    public String getSeatId() { return seatId; }
    public String getShowId() { return showId; }
    public String getUserId() { return userId; }
}

class SeatLockManager {
    private ConcurrentHashMap<String, SeatLock> locks; // ConcurrentHashMap = thread-safe lock registry; O(1) lookup
    private ConcurrentHashMap<String, ReentrantLock> showLocks; // per-show lock = finer granularity than global lock
    private long defaultLockDurationMs;

    public SeatLockManager(long defaultLockDurationMs) {
        this.locks = new ConcurrentHashMap<>();
        this.showLocks = new ConcurrentHashMap<>();
        this.defaultLockDurationMs = defaultLockDurationMs;
    }

    private String getKey(String showId, String seatId) { return showId + "_" + seatId; }

    private ReentrantLock getShowLock(String showId) {
        return showLocks.computeIfAbsent(showId, k -> new ReentrantLock());
    }

    public boolean lockSeat(String showId, String seatId, String userId) {
        String key = getKey(showId, seatId);
        SeatLock existing = locks.get(key);
        if (existing != null) {
            if (existing.isExpired()) locks.remove(key, existing);
            else if (!existing.getUserId().equals(userId)) return false;
            else return true;
        }
        SeatLock newLock = new SeatLock(seatId, showId, userId, defaultLockDurationMs);
        SeatLock prev = locks.putIfAbsent(key, newLock);
        if (prev == null) return true;
        if (prev.isExpired()) { locks.replace(key, prev, newLock); return true; }
        return prev.getUserId().equals(userId);
    }

    public boolean lockSeats(String showId, List<String> seatIds, String userId) {
        ReentrantLock showLock = getShowLock(showId);
        showLock.lock();
        try {
            for (String seatId : seatIds) {
                String key = getKey(showId, seatId);
                SeatLock existing = locks.get(key);
                if (existing != null && !existing.isExpired() && !existing.getUserId().equals(userId)) return false;
            }
            for (String seatId : seatIds) {
                String key = getKey(showId, seatId);
                locks.put(key, new SeatLock(seatId, showId, userId, defaultLockDurationMs));
            }
            return true;
        } finally {
            showLock.unlock();
        }
    }

    public void unlockSeat(String showId, String seatId) { locks.remove(getKey(showId, seatId)); }
    public void unlockSeats(String showId, List<String> seatIds) { for (String s : seatIds) unlockSeat(showId, s); }

    public String getLockedBy(String showId, String seatId) {
        SeatLock lock = locks.get(getKey(showId, seatId));
        return (lock != null && !lock.isExpired()) ? lock.getUserId() : null;
    }
}

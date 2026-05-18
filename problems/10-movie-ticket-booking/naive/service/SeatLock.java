/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/SeatLock.java — Synchronized seat locking with expiry (naive: global lock)

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class SeatLock {
    private String seatId;              // private = which seat is locked
    private String showId;              // private = which show the lock belongs to
    private String userId;              // private = who holds the lock
    private long expiryTime;            // private = lock auto-expires after this timestamp

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
    private Map<String, SeatLock> locks; // private = lock registry hidden; access via lock/unlock
    private long defaultLockDurationMs; // private = lock timeout configured at construction

    public SeatLockManager(long defaultLockDurationMs) {
        this.locks = new HashMap<>();
        this.defaultLockDurationMs = defaultLockDurationMs;
    }

    private String getKey(String showId, String seatId) { return showId + "_" + seatId; }

    public synchronized boolean lockSeat(String showId, String seatId, String userId) { // synchronized = one thread at a time acquires locks
        String key = getKey(showId, seatId);
        SeatLock existing = locks.get(key);
        if (existing != null) {
            if (existing.isExpired()) locks.remove(key);
            else if (!existing.getUserId().equals(userId)) return false;
            else return true;
        }
        locks.put(key, new SeatLock(seatId, showId, userId, defaultLockDurationMs));
        return true;
    }

    public synchronized boolean lockSeats(String showId, List<String> seatIds, String userId) {
        for (String seatId : seatIds) {
            String key = getKey(showId, seatId);
            SeatLock existing = locks.get(key);
            if (existing != null && !existing.isExpired() && !existing.getUserId().equals(userId)) return false;
        }
        for (String seatId : seatIds) lockSeat(showId, seatId, userId);
        return true;
    }

    public synchronized void unlockSeat(String showId, String seatId) { locks.remove(getKey(showId, seatId)); }
    public synchronized void unlockSeats(String showId, List<String> seatIds) { for (String s : seatIds) unlockSeat(showId, s); }
    public synchronized String getLockedBy(String showId, String seatId) {
        SeatLock lock = locks.get(getKey(showId, seatId));
        return (lock != null && !lock.isExpired()) ? lock.getUserId() : null;
    }
}

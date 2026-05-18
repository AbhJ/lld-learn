/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/RoomBookingService.java — ConcurrentSkipListMap + ReentrantLock per room for atomic conflict check

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicInteger;

public class RoomBookingService {
    private final Map<String, ConcurrentSkipListMap<TimeSlot, Booking>> roomBookings; // ConcurrentSkipListMap = sorted, lock-free reads for time-based lookups
    private final Map<String, ReentrantLock> roomLocks;  // ReentrantLock = per-room lock; atomic conflict-check-then-book
    private final AtomicInteger successCount;            // AtomicInteger = thread-safe booking counter
    private final AtomicInteger rejectCount;             // AtomicInteger = thread-safe rejection counter

    public RoomBookingService(List<String> roomIds) {
        this.roomBookings = new HashMap<>();
        this.roomLocks = new HashMap<>();
        this.successCount = new AtomicInteger(0);
        this.rejectCount = new AtomicInteger(0);

        for (String roomId : roomIds) {
            roomBookings.put(roomId, new ConcurrentSkipListMap<>());
            roomLocks.put(roomId, new ReentrantLock());
        }
    }

    /**
     * Book a room. Uses per-room ReentrantLock for atomic conflict-check-then-book.
     */
    public boolean bookRoom(String roomId, Booking booking) {
        ReentrantLock lock = roomLocks.get(roomId);
        if (lock == null) return false;

        lock.lock();
        try {
            ConcurrentSkipListMap<TimeSlot, Booking> bookings = roomBookings.get(roomId);
            TimeSlot requested = booking.getTimeSlot();

            // Check for conflicts — look at nearby time slots
            TimeSlot searchStart = new TimeSlot(requested.getStartTime() - 1, requested.getStartTime());
            TimeSlot searchEnd = new TimeSlot(requested.getEndTime(), requested.getEndTime() + 1);

            // Check all bookings that could potentially overlap
            for (Map.Entry<TimeSlot, Booking> entry : bookings.entrySet()) {
                if (entry.getKey().overlaps(requested)) {
                    rejectCount.incrementAndGet();
                    return false;
                }
            }

            bookings.put(requested, booking);
            successCount.incrementAndGet();
            return true;
        } finally {
            lock.unlock();
        }
    }

    public int getSuccessCount() { return successCount.get(); }
    public int getRejectCount() { return rejectCount.get(); }

    public int getBookingCount(String roomId) {
        ConcurrentSkipListMap<TimeSlot, Booking> bookings = roomBookings.get(roomId);
        return bookings != null ? bookings.size() : 0;
    }
}

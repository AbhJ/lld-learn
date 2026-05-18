/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/BookingService.java — Thread-safe booking with ReentrantLock per show

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicInteger;

class BookingService {
    private final ConcurrentHashMap<String, Show> shows = new ConcurrentHashMap<>(); // ConcurrentHashMap = thread-safe show registry
    private final ConcurrentHashMap<String, ReentrantLock> showLocks = new ConcurrentHashMap<>(); // per-show lock = parallel bookings across different shows
    private final ConcurrentHashMap<String, Booking> bookings = new ConcurrentHashMap<>(); // ConcurrentHashMap = thread-safe booking storage
    private final AtomicInteger successCount = new AtomicInteger(0); // AtomicInteger = lock-free counter
    private final AtomicInteger failCount = new AtomicInteger(0); // AtomicInteger = lock-free counter

    public void addShow(Show show) {
        shows.put(show.getShowId(), show);
        showLocks.put(show.getShowId(), new ReentrantLock());
    }

    /**
     * Book seats for a user. Uses per-show locking to allow parallel bookings
     * across different shows while serializing within the same show.
     *
     * Flow:
     * 1. Acquire show lock
     * 2. Try to lock all requested seats (CAS: AVAILABLE -> LOCKED)
     * 3. If all succeed, confirm booking (LOCKED -> BOOKED)
     * 4. If any fail, release already-locked seats (LOCKED -> AVAILABLE)
     */
    public Booking bookSeats(String userId, String showId, List<String> seatIds) {
        Show show = shows.get(showId);
        if (show == null) return null;

        ReentrantLock lock = showLocks.get(showId);
        lock.lock();
        try {
            List<Seat> lockedSeats = new ArrayList<>();

            // Try to lock all seats
            for (String seatId : seatIds) {
                Seat seat = show.getSeat(seatId);
                if (seat == null || !seat.tryLock()) {
                    // Rollback: release any seats we already locked
                    for (Seat locked : lockedSeats) {
                        locked.release();
                    }
                    failCount.incrementAndGet();
                    return null;
                }
                lockedSeats.add(seat);
            }

            // All seats locked — confirm booking
            for (Seat seat : lockedSeats) {
                seat.confirmBooking();
            }

            Booking booking = new Booking(userId, show, lockedSeats);
            bookings.put(booking.getBookingId(), booking);
            successCount.incrementAndGet();
            return booking;
        } finally {
            lock.unlock();
        }
    }

    public int getSuccessCount() { return successCount.get(); }
    public int getFailCount() { return failCount.get(); }
    public Show getShow(String showId) { return shows.get(showId); }
}

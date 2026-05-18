/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Booking.java — A confirmed booking record

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class Booking {
    private static final AtomicInteger counter = new AtomicInteger(0); // AtomicInteger = thread-safe ID generation
    private final String bookingId;     // final = immutable; safe to share across threads
    private final String userId;        // final = immutable after construction
    private final Show show;            // final = show reference never changes
    private final List<Seat> seats;     // final = seat list reference stable after construction
    private final long timestamp;       // final = creation time immutable

    public Booking(String userId, Show show, List<Seat> seats) {
        this.bookingId = "BK-" + counter.incrementAndGet();
        this.userId = userId;
        this.show = show;
        this.seats = seats;
        this.timestamp = System.currentTimeMillis();
    }

    public String getBookingId() { return bookingId; }
    public String getUserId() { return userId; }
    public Show getShow() { return show; }
    public List<Seat> getSeats() { return seats; }
    public long getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return bookingId + " [User=" + userId + ", Seats=" + seats.size() + "]";
    }

    public static void resetCounter() { counter.set(0); }
}

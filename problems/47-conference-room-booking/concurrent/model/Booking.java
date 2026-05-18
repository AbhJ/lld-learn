/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Booking.java — Booking record for a conference room

public class Booking {
    private final String bookingId;  // final = booking ID never changes; safe to share across threads
    private final String bookedBy;   // final = who booked; immutable for safe publication
    private final TimeSlot timeSlot; // final = time range fixed; threads see consistent value
    private final String roomId;     // final = room assignment immutable once created

    public Booking(String bookingId, String bookedBy, TimeSlot timeSlot, String roomId) {
        this.bookingId = bookingId;
        this.bookedBy = bookedBy;
        this.timeSlot = timeSlot;
        this.roomId = roomId;
    }

    public String getBookingId() { return bookingId; }
    public String getBookedBy() { return bookedBy; }
    public TimeSlot getTimeSlot() { return timeSlot; }
    public String getRoomId() { return roomId; }

    @Override
    public String toString() {
        return bookingId + " by " + bookedBy + " " + timeSlot;
    }
}

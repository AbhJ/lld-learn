/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Booking.java — Confirmed ticket booking with cancellation support

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

enum BookingStatus { CONFIRMED, CANCELLED }

class Booking {
    private static AtomicInteger counter = new AtomicInteger(0); // AtomicInteger = thread-safe ID generation without synchronized
    private String bookingId;
    private String userId;
    private Show show;
    private List<Seat> seats;
    private Payment payment;
    private volatile BookingStatus status; // volatile = status change visible to all threads immediately

    public Booking(String userId, Show show, List<Seat> seats, Payment payment) {
        this.bookingId = "BK-" + counter.incrementAndGet(); this.userId = userId; this.show = show; this.seats = seats; this.payment = payment; this.status = BookingStatus.CONFIRMED;
    }

    public boolean cancel() {
        if (status == BookingStatus.CANCELLED) return false;
        status = BookingStatus.CANCELLED;
        payment.refund();
        for (Seat seat : seats) show.setSeatStatus(seat.getSeatId(), SeatStatus.AVAILABLE);
        return true;
    }

    public double getTotalAmount() { double t = 0; for (Seat s : seats) t += s.getPrice(); return t; }
    public String getBookingId() { return bookingId; }
    public String getUserId() { return userId; }
    public Show getShow() { return show; }
    public List<Seat> getSeats() { return seats; }
    public Payment getPayment() { return payment; }
    public BookingStatus getStatus() { return status; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(bookingId).append(": ").append(userId).append(" | ").append(show.getMovie().getTitle()).append(" | Seats: [");
        for (int i = 0; i < seats.size(); i++) { if (i > 0) sb.append(", "); sb.append(seats.get(i).getSeatId()); }
        sb.append("] | $").append(String.format("%.2f", getTotalAmount())).append(" | ").append(status);
        return sb.toString();
    }
    public static void resetCounter() { counter.set(0); }
}

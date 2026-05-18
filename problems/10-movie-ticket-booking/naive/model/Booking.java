/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Booking.java — Confirmed ticket booking with cancellation support

import java.util.List;

enum BookingStatus { CONFIRMED, CANCELLED } // enum = fixed set of booking states; safer than strings

class Booking {
    private static int counter = 0;     // static = shared across all Bookings; auto-generates unique IDs
    private String bookingId;           // private = ID hidden; access via getter
    private String userId;              // private = user identity encapsulated
    private Show show;                  // private = show reference encapsulated
    private List<Seat> seats;           // private = booked seats list encapsulated
    private Payment payment;            // private = payment details encapsulated
    private BookingStatus status;       // private = only cancel() can change status

    public Booking(String userId, Show show, List<Seat> seats, Payment payment) {
        this.bookingId = "BK-" + (++counter); this.userId = userId; this.show = show; this.seats = seats; this.payment = payment; this.status = BookingStatus.CONFIRMED;
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
    public static void resetCounter() { counter = 0; }
}

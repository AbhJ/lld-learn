/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/FlightSeatMap.java — AtomicReferenceArray for seat map with CAS to claim seats

import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.atomic.AtomicInteger;

public class FlightSeatMap {
    private final AtomicReferenceArray<Passenger> seats;  // AtomicReferenceArray = per-element CAS; no global lock needed
    private final int totalSeats;                         // final = capacity fixed at creation
    private final AtomicInteger seatsBooked = new AtomicInteger(0);     // AtomicInteger = lock-free booking counter
    private final AtomicInteger bookingAttempts = new AtomicInteger(0); // AtomicInteger = tracks total attempts
    private final AtomicInteger failedAttempts = new AtomicInteger(0);  // AtomicInteger = tracks failed attempts

    public FlightSeatMap(int totalSeats) {
        this.totalSeats = totalSeats;
        this.seats = new AtomicReferenceArray<>(totalSeats);
    }

    /**
     * Try to book a specific seat for a passenger.
     * Uses compareAndSet(null, passenger) — only succeeds if seat is empty.
     */
    public boolean bookSeat(int seatNumber, Passenger passenger) {
        bookingAttempts.incrementAndGet();
        if (seatNumber < 0 || seatNumber >= totalSeats) {
            failedAttempts.incrementAndGet();
            return false;
        }
        if (seats.compareAndSet(seatNumber, null, passenger)) { // CAS = only books if seat is null (empty)
            seatsBooked.incrementAndGet();
            return true;
        }
        failedAttempts.incrementAndGet();
        return false; // Seat already taken
    }

    /**
     * Try to book any available seat for a passenger.
     * Scans seats and tries CAS on each empty one.
     */
    public int bookAnySeat(Passenger passenger) {
        bookingAttempts.incrementAndGet();
        for (int i = 0; i < totalSeats; i++) {
            if (seats.get(i) == null) {
                if (seats.compareAndSet(i, null, passenger)) { // CAS = prevents double-booking same seat
                    seatsBooked.incrementAndGet();
                    return i;
                }
            }
        }
        failedAttempts.incrementAndGet();
        return -1; // No seats available
    }

    public Passenger getSeatOccupant(int seatNumber) {
        if (seatNumber < 0 || seatNumber >= totalSeats) return null;
        return seats.get(seatNumber);
    }

    public int getTotalSeats() { return totalSeats; }
    public int getSeatsBooked() { return seatsBooked.get(); }
    public int getBookingAttempts() { return bookingAttempts.get(); }
    public int getFailedAttempts() { return failedAttempts.get(); }

    public int countOccupied() {
        int count = 0;
        for (int i = 0; i < totalSeats; i++) {
            if (seats.get(i) != null) count++;
        }
        return count;
    }
}

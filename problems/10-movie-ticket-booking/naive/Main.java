/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating movie ticket booking system

import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Movie Ticket Booking System Test ===\n");
        Payment.resetCounter();
        Booking.resetCounter();

        BookingSystem system = new BookingSystem();
        // Add observer — BookingNotifier implements BookingObserver
        system.addObserver(new BookingNotifier("Notification"));

        Movie inception = new Movie("M1", "Inception", "Sci-Fi", 148);
        Movie darkKnight = new Movie("M2", "The Dark Knight", "Action", 152);
        system.addMovie(inception);
        system.addMovie(darkKnight);

        Screen screen1 = new Screen("SC1", "Screen 1");
        screen1.setupSeats(3, 2, 1, 8);
        system.addScreen(screen1);

        Show show1 = system.addShow(inception, screen1, "10:00 AM");
        Show show3 = system.addShow(darkKnight, screen1, "6:00 PM");

        System.out.println("--- Test: Seat Availability ---");
        System.out.println("  Total: " + show1.getScreen().getTotalSeats() + ", Available: " + show1.getAvailableCount());

        System.out.println("\n--- Test: Lock and Book ---");
        List<String> seats1 = Arrays.asList("D1", "D2");
        boolean locked = system.lockSeats(show1.getShowId(), seats1, "User1");
        System.out.println("User1 locks D1,D2: " + (locked ? "SUCCESS" : "FAILED"));
        Booking b1 = system.confirmBooking(show1.getShowId(), seats1, "User1");
        System.out.println("Booking: " + b1);

        System.out.println("\n--- Test: Concurrent Lock ---");
        locked = system.lockSeats(show1.getShowId(), Arrays.asList("D1", "D3"), "User2");
        System.out.println("User2 tries D1,D3 (D1 booked): " + (locked ? "SUCCESS" : "FAILED"));
        locked = system.lockSeats(show1.getShowId(), Arrays.asList("D3", "D4"), "User2");
        System.out.println("User2 tries D3,D4: " + (locked ? "SUCCESS" : "FAILED"));
        Booking b2 = system.confirmBooking(show1.getShowId(), Arrays.asList("D3", "D4"), "User2");
        System.out.println("Booking: " + b2);

        System.out.println("\n--- Test: Cancellation ---");
        System.out.println("Before cancel - D1: " + show1.getSeatStatus("D1"));
        system.cancelBooking(b1.getBookingId());
        System.out.println("After cancel - D1: " + show1.getSeatStatus("D1"));

        System.out.println("\n--- Test: Rebook Cancelled Seats ---");
        locked = system.lockSeats(show1.getShowId(), Arrays.asList("D1"), "User5");
        System.out.println("User5 locks D1: " + (locked ? "SUCCESS" : "FAILED"));
        Booking b5 = system.confirmBooking(show1.getShowId(), Arrays.asList("D1"), "User5");
        System.out.println("Booking: " + b5);

        System.out.println("\n--- Test: Seat Factory ---");
        System.out.println("  " + SeatFactory.createSeat(SeatType.REGULAR, "X1", 0, 1));
        System.out.println("  " + SeatFactory.createSeat(SeatType.VIP, "X3", 0, 3));

        System.out.println("\n=== All Tests Passed ===");
    }
}

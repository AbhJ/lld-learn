/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Demonstrates airline reservation with BitSet seat map for O(1) availability
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Airline Reservation (Optimized: BitSet Seats) Demo ===\n");
        Booking.resetCounter();

        AirlineSystem airline = new AirlineSystem();
        Flight f1 = new Flight("AA101", "NYC", "LAX", 250.00, 10, 4, 2);
        airline.addFlight(f1);

        System.out.println("--- Seat Availability (BitSet) ---");
        System.out.println("  FIRST: " + f1.getAvailable("FIRST"));
        System.out.println("  BUSINESS: " + f1.getAvailable("BUSINESS"));
        System.out.println("  ECONOMY: " + f1.getAvailable("ECONOMY"));

        System.out.println("\n--- Bookings ---");
        Passenger alice = new Passenger("Alice", "alice@mail.com");
        Booking b1 = airline.book(alice, f1, "BUSINESS");
        Booking b2 = airline.book(new Passenger("Bob", "bob@mail.com"), f1, "ECONOMY");
        Booking b3 = airline.book(new Passenger("Carol", "c@mail.com"), f1, "FIRST");

        System.out.println("\n--- Availability After Bookings ---");
        System.out.println("  FIRST: " + f1.getAvailable("FIRST"));
        System.out.println("  BUSINESS: " + f1.getAvailable("BUSINESS"));
        System.out.println("  Occupancy: " + String.format("%.0f%%", f1.getOccupancy() * 100));

        System.out.println("\n--- Cancel and Rebook ---");
        airline.cancel(b2, f1);
        System.out.println("  ECONOMY after cancel: " + f1.getAvailable("ECONOMY"));
        Booking b4 = airline.book(new Passenger("Dave", "d@mail.com"), f1, "ECONOMY");

        System.out.println("\n--- Fill All First Class ---");
        airline.book(new Passenger("Eve", ""), f1, "FIRST");
        airline.book(new Passenger("Frank", ""), f1, "FIRST"); // Should fail

        System.out.println("\n--- Check-In ---");
        airline.checkIn(b1);

        System.out.println("\n--- High-Volume Test (BitSet Speed) ---");
        Flight bigFlight = new Flight("BB999", "SFO", "JFK", 300.00, 200, 50, 10);
        airline.addFlight(bigFlight);
        long start = System.nanoTime();
        for (int i = 0; i < 200; i++) {
            airline.book(new Passenger("P" + i, ""), bigFlight, "ECONOMY");
        }
        long elapsed = (System.nanoTime() - start) / 1_000_000;
        System.out.println("  Booked 200 economy seats in " + elapsed + "ms");
        System.out.println("  Economy remaining: " + bigFlight.getAvailable("ECONOMY"));

        System.out.println("\n=== Airline Reservation (Optimized) Demo Complete ===");
    }
}

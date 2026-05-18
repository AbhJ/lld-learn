/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Demonstrates airline reservation with linear seat search
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Airline Reservation (Naive) Demo ===\n");
        Booking.resetCounter();

        AirlineSystem airline = new AirlineSystem();
        Flight f1 = new Flight("AA101", "NYC", "LAX", 250.00);
        f1.addSeats(10, 4, 2);
        Flight f2 = new Flight("AA202", "NYC", "LAX", 280.00);
        f2.addSeats(8, 3, 1);
        airline.addFlight(f1);
        airline.addFlight(f2);

        System.out.println("--- Search ---");
        List<Flight> results = airline.search("NYC", "LAX");
        System.out.println("  Found: " + results.size() + " flights");

        System.out.println("\n--- Booking ---");
        Passenger alice = new Passenger("Alice", "alice@mail.com");
        Passenger bob = new Passenger("Bob", "bob@mail.com");
        Booking b1 = airline.book(alice, f1, "BUSINESS");
        Booking b2 = airline.book(bob, f1, "ECONOMY");

        System.out.println("\n--- Check-In ---");
        airline.checkIn(b1);

        System.out.println("\n--- Cancel ---");
        airline.cancel(b2);
        System.out.println("  Economy available after cancel: " + f1.getAvailable("ECONOMY"));

        System.out.println("\n--- Sell Out ---");
        for (int i = 0; i < 2; i++) airline.book(new Passenger("P" + i, ""), f1, "FIRST");
        airline.book(new Passenger("Extra", ""), f1, "FIRST"); // Should fail

        System.out.println("\n=== Airline Reservation (Naive) Demo Complete ===");
    }
}

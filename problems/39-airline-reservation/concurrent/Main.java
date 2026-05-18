/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — 50 passengers competing for 30 seats, exactly 30 succeed

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Airline Reservation Demo ===\n");

        int totalSeats = 30;
        int passengerCount = 50;
        FlightSeatMap flight = new FlightSeatMap(totalSeats);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(passengerCount);
        AtomicInteger successfulBookings = new AtomicInteger(0);
        AtomicInteger failedBookings = new AtomicInteger(0);

        System.out.println("Scenario: 50 passengers compete for 30 seats simultaneously.");
        System.out.println("Expected: Exactly 30 bookings succeed, 20 fail. No double-booking.\n");

        for (int p = 0; p < passengerCount; p++) {
            final int passengerId = p;
            new Thread(() -> {
                try {
                    startLatch.await();
                    Passenger passenger = new Passenger("Passenger-" + passengerId, passengerId);
                    int seat = flight.bookAnySeat(passenger);
                    if (seat >= 0) {
                        successfulBookings.incrementAndGet();
                    } else {
                        failedBookings.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }, "Passenger-" + p).start();
        }

        startLatch.countDown();
        doneLatch.await();

        // Verify no double-booking: each occupied seat has exactly one passenger
        int occupiedCount = flight.countOccupied();
        boolean noDoubleBooking = true;
        for (int i = 0; i < totalSeats; i++) {
            Passenger occupant = flight.getSeatOccupant(i);
            if (occupant != null) {
                // Check no other seat has the same passenger
                for (int j = i + 1; j < totalSeats; j++) {
                    Passenger other = flight.getSeatOccupant(j);
                    if (other != null && other.getId() == occupant.getId()) {
                        noDoubleBooking = false;
                    }
                }
            }
        }

        System.out.println("--- Results ---");
        System.out.println("Total seats: " + totalSeats);
        System.out.println("Passengers: " + passengerCount);
        System.out.println("Successful bookings: " + successfulBookings.get());
        System.out.println("Failed bookings: " + failedBookings.get());
        System.out.println("Occupied seats: " + occupiedCount);
        System.out.println("No double-booking: " + noDoubleBooking);

        boolean passed = successfulBookings.get() == totalSeats
                && failedBookings.get() == (passengerCount - totalSeats)
                && occupiedCount == totalSeats
                && noDoubleBooking;
        System.out.println("\nCorrectness check: " + (passed ? "PASSED" : "FAILED"));
    }
}

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating hotel management system

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Hotel Management System Test ===\n");
        Guest.resetCounter();
        Booking.resetCounter();

        Hotel hotel = new Hotel("Grand Plaza Hotel");
        // Add observer — NotificationService implements BookingObserver
        hotel.addObserver(new NotificationService("Notification"));

        System.out.println("--- Test: Room Setup ---");
        hotel.addRoom("SINGLE", "101");
        hotel.addRoom("SINGLE", "102");
        hotel.addRoom("DOUBLE", "201");
        hotel.addRoom("DOUBLE", "202");
        hotel.addRoom("SUITE", "301");
        System.out.println(hotel.getRoomStatus());

        System.out.println("\n--- Test: Booking ---");
        Guest john = new Guest("John Doe", "john@email.com", "555-0101");
        Guest jane = new Guest("Jane Smith", "jane@email.com", "555-0102");
        LocalDate checkIn = LocalDate.of(2026, 5, 10);
        Booking b1 = hotel.bookRoom(john, "Double", checkIn, 3);
        Booking b2 = hotel.bookRoom(jane, "Suite", checkIn, 2);

        System.out.println("\n--- Test: Check-In ---");
        hotel.checkIn(b1);
        hotel.checkIn(b2);

        System.out.println("\n--- Test: Room Service ---");
        hotel.orderRoomService("201", "Breakfast", 15.00);
        hotel.orderRoomService("201", "Minibar", 25.00);

        System.out.println("\n--- Test: Check-Out ---");
        Bill bill1 = hotel.checkOut(b1);
        System.out.println(bill1.generateInvoice());

        System.out.println("\n--- Test: Weekend Pricing ---");
        hotel.setPricingStrategy(new WeekendPricing(1.5));
        Bill bill2 = hotel.checkOut(b2);
        System.out.println(bill2.generateInvoice());

        System.out.println("\n--- Test: Room Factory ---");
        Room r1 = RoomFactory.createRoom("SINGLE", "401");
        Room r2 = RoomFactory.createRoom("SUITE", "403");
        System.out.println("Created: " + r1);
        System.out.println("Created: " + r2);

        System.out.println("\n=== All Tests Passed ===");
    }
}

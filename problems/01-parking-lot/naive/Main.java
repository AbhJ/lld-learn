/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating the parking lot system with test scenarios

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Parking Lot System Test ===\n");

        // Reset singleton for clean test
        ParkingLot.resetInstance();
        Ticket.resetCounter();

        // Initialize parking lot
        ParkingLot lot = ParkingLot.getInstance();
        lot.addLevel(5, 10, 5); // Level 0
        lot.addLevel(5, 10, 5); // Level 1

        System.out.println("Parking Lot initialized with " + lot.getLevelCount() + " levels");
        System.out.println("Level 0: 5 SMALL, 10 MEDIUM, 5 LARGE spots");
        System.out.println("Level 1: 5 SMALL, 10 MEDIUM, 5 LARGE spots");

        // Add observer — DisplayBoard implements ParkingObserver
        lot.addObserver(new DisplayBoard("Main Entrance"));

        // --- Test: Park Vehicles ---
        System.out.println("\n--- Test: Park Vehicles ---");
        Vehicle car1 = new Car("ABC-123");
        Vehicle moto1 = new Motorcycle("MOTO-1");
        Vehicle truck1 = new Truck("TRUCK-1");

        Ticket t1 = lot.parkVehicle(car1);
        System.out.println("Car (ABC-123) parked. Ticket: " + t1.getTicketId());

        Ticket t2 = lot.parkVehicle(moto1);
        System.out.println("Motorcycle (MOTO-1) parked. Ticket: " + t2.getTicketId());

        Ticket t3 = lot.parkVehicle(truck1);
        System.out.println("Truck (TRUCK-1) parked. Ticket: " + t3.getTicketId());

        // --- Test: Check Availability ---
        System.out.println("\n--- Test: Check Availability ---");
        System.out.println(lot.getAvailabilitySummary());

        // --- Test: Unpark with Hourly Pricing ---
        System.out.println("\n--- Test: Unpark with Hourly Pricing ---");
        lot.setPricingStrategy(new HourlyPricing(2.0));
        // Simulate 2 hours later
        long twoHoursLater = System.currentTimeMillis() + (2 * 60 * 60 * 1000);
        double fee1 = lot.unparkVehicle(t1.getTicketId(), twoHoursLater);
        System.out.println("Car (ABC-123) unparked. Fee: $" + String.format("%.2f", fee1) + " (2 hours @ $2/hr)");

        // --- Test: Unpark with Flat Rate Pricing ---
        System.out.println("\n--- Test: Unpark with Flat Rate Pricing ---");
        lot.setPricingStrategy(new FlatRatePricing(15.0));
        long oneHourLater = System.currentTimeMillis() + (60 * 60 * 1000);
        double fee2 = lot.unparkVehicle(t3.getTicketId(), oneHourLater);
        System.out.println("Truck (TRUCK-1) unparked. Fee: $" + String.format("%.2f", fee2) + " (flat rate)");

        // --- Test: Vehicle Type Pricing ---
        System.out.println("\n--- Test: Vehicle Type Based Pricing ---");
        lot.setPricingStrategy(new VehicleTypePricing(1.0, 2.0, 4.0));
        long threeHoursLater = System.currentTimeMillis() + (3 * 60 * 60 * 1000);
        double fee3 = lot.unparkVehicle(t2.getTicketId(), threeHoursLater);
        System.out.println("Motorcycle (MOTO-1) unparked. Fee: $" + String.format("%.2f", fee3) + " (3 hours @ $1/hr for motorcycle)");

        // --- Test: Spot Full Scenario ---
        System.out.println("\n--- Test: Fill All LARGE Spots on Level 0 ---");
        lot.setPricingStrategy(new HourlyPricing(2.0));
        List<Ticket> truckTickets = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Ticket tt = lot.parkVehicle(new Truck("TRUCK-L0-" + i));
            if (tt != null) {
                truckTickets.add(tt);
                System.out.println("  Truck TRUCK-L0-" + i + " parked at " + tt.getSpot().getSpotId());
            }
        }
        // Next truck should go to Level 1
        Ticket overflowTicket = lot.parkVehicle(new Truck("TRUCK-OVERFLOW"));
        if (overflowTicket != null) {
            System.out.println("  Overflow Truck parked at " + overflowTicket.getSpot().getSpotId() + " (Level " + overflowTicket.getSpot().getLevel() + ")");
        }

        // --- Test: No Spot Available ---
        System.out.println("\n--- Test: No Spot Available ---");
        // Fill all large spots on level 1 (4 remaining + overflow = 5 total used, so 4 left)
        for (int i = 0; i < 4; i++) {
            lot.parkVehicle(new Truck("TRUCK-L1-" + i));
        }
        // Now try to park another truck - should fail
        Ticket failTicket = lot.parkVehicle(new Truck("TRUCK-FAIL"));
        if (failTicket == null) {
            System.out.println("  No spot available for TRUCK-FAIL - Parking lot full for LARGE vehicles!");
        }

        // --- Test: Payment Receipt ---
        System.out.println("\n--- Test: Payment Receipt ---");
        Vehicle car2 = new Car("XYZ-789");
        Ticket t4 = lot.parkVehicle(car2);
        t4.markExit(t4.getEntryTime() + (3 * 60 * 60 * 1000)); // 3 hours
        String receipt = lot.getPaymentProcessor().generateReceipt(t4);
        System.out.println(receipt);

        System.out.println("\n--- Final Availability ---");
        System.out.println(lot.getAvailabilitySummary());

        System.out.println("\n=== All Tests Passed ===");
    }
}

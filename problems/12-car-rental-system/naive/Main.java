/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating the car rental system

/*
 * VARIATIONS FREQUENTLY ASKED:
 * 1. Peer-to-peer car sharing (Turo) - Owner listings, insurance, damage claims
 * 2. One-way rentals - Different pickup/return locations, relocation fee
 * 3. Fleet management - Maintenance scheduling, mileage tracking, fuel policy
 * 4. Corporate accounts - Bulk rates, pre-approved drivers, department billing
 * 5. Damage assessment - Before/after photos, dispute resolution, deductibles
 *
 * See VARIATIONS.md for full solution approaches.
 */
import java.time.LocalDate;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Car Rental System Demo ===\n");

        RentalSystem system = new RentalSystem();

        // Setup locations
        Location downtown = new Location("LOC-1", "Downtown Office", "123 Main St");
        Location airport = new Location("LOC-2", "Airport Terminal", "456 Airport Rd");

        // Register vehicles using factory
        Vehicle car1 = system.createVehicle("CAR", "V-1", "Toyota", "Camry", 2024, "ABC-123", 45.00, downtown);
        Vehicle suv1 = system.createVehicle("SUV", "V-2", "Toyota", "RAV4", 2024, "DEF-456", 75.00, downtown);
        Vehicle suv2 = system.createVehicle("SUV", "V-3", "Ford", "Explorer", 2023, "GHI-789", 85.00, airport);
        Vehicle van1 = system.createVehicle("VAN", "V-4", "Honda", "Odyssey", 2024, "JKL-012", 90.00, downtown);
        Vehicle truck1 = system.createVehicle("TRUCK", "V-5", "Ford", "F-150", 2024, "MNO-345", 100.00, airport);

        system.addVehicle(car1);
        system.addVehicle(suv1);
        system.addVehicle(suv2);
        system.addVehicle(van1);
        system.addVehicle(truck1);
        System.out.println("Registered " + system.getVehicles().size() + " vehicles.\n");

        // Create customers
        Customer alice = new Customer("C-1", "Alice Johnson", "alice@email.com", "555-0101", "DL-12345");
        Customer bob = new Customer("C-2", "Bob Smith", "bob@email.com", "555-0102", "DL-67890");
        Customer charlie = new Customer("C-3", "Charlie Brown", "charlie@email.com", "555-0103", "DL-11111");

        // --- Test 1: Search Available Vehicles ---
        System.out.println("--- Test 1: Search Available SUVs ---");
        List<Vehicle> availableSUVs = system.searchAvailable("SUV");
        for (Vehicle v : availableSUVs) {
            System.out.println("  Found: " + v);
        }
        System.out.println();

        // --- Test 2: Make Reservation with Builder ---
        System.out.println("--- Test 2: Make Reservation (Daily Pricing) ---");
        Reservation res1 = new Reservation.Builder(alice, suv1)
                .startDate(LocalDate.of(2026, 5, 15))
                .endDate(LocalDate.of(2026, 5, 20))
                .pickupLocation(downtown)
                .dropoffLocation(downtown)
                .addInsurance(Insurance.basic())
                .pricingStrategy(new DailyPricing())
                .build();
        system.makeReservation(res1);
        System.out.println("Reservation: " + res1);
        System.out.printf("Vehicle cost: $%.2f for %d days%n", res1.getVehicleCost(), res1.getDays());
        System.out.printf("Insurance cost: $%.2f%n", res1.getInsuranceCost());
        System.out.printf("Total: $%.2f%n", res1.getTotalCost());
        System.out.println();

        // --- Test 3: Weekly Pricing ---
        System.out.println("--- Test 3: Reservation with Weekly Pricing ---");
        Reservation res2 = new Reservation.Builder(bob, truck1)
                .startDate(LocalDate.of(2026, 5, 15))
                .endDate(LocalDate.of(2026, 5, 29))
                .pickupLocation(airport)
                .dropoffLocation(downtown)
                .addInsurance(Insurance.premium())
                .addInsurance(Insurance.liability())
                .pricingStrategy(new WeeklyPricing())
                .build();
        system.makeReservation(res2);
        System.out.println("Reservation: " + res2);
        System.out.printf("Vehicle cost (weekly discount): $%.2f for %d days%n", res2.getVehicleCost(), res2.getDays());
        System.out.printf("Insurance cost: $%.2f%n", res2.getInsuranceCost());
        System.out.printf("Total: $%.2f%n", res2.getTotalCost());
        System.out.println();

        // --- Test 4: Waitlist and Notification ---
        System.out.println("--- Test 4: Waitlist and Notification ---");
        system.addToWaitlist("SUV", charlie);

        // SUV is not available now
        List<Vehicle> availableNow = system.searchAvailable("SUV", downtown);
        System.out.println("Available SUVs at downtown: " + availableNow.size());

        // Complete reservation - triggers notification
        System.out.println("\nCompleting Alice's reservation...");
        Bill bill1 = system.completeReservation(res1);
        System.out.println(bill1);
        System.out.println();

        // --- Test 5: Weekend Pricing ---
        System.out.println("--- Test 5: Weekend Pricing ---");
        Reservation res3 = new Reservation.Builder(charlie, suv1)
                .startDate(LocalDate.of(2026, 5, 23))
                .endDate(LocalDate.of(2026, 5, 25))
                .pickupLocation(downtown)
                .pricingStrategy(new WeekendPricing())
                .build();
        system.makeReservation(res3);
        System.out.printf("Weekend rate for %d days: $%.2f%n", res3.getDays(), res3.getVehicleCost());
        System.out.println();

        // --- Test 6: Cancel Reservation ---
        System.out.println("--- Test 6: Cancel Reservation ---");
        system.cancelReservation(res3);
        System.out.println("Reservation cancelled. SUV available: " + suv1.isAvailable());
        System.out.println();

        // --- Test 7: Double-booking Prevention ---
        System.out.println("--- Test 7: Double-booking Prevention ---");
        suv1.setAvailable(false); // simulate rented
        Reservation res4 = new Reservation.Builder(bob, suv1)
                .startDate(LocalDate.of(2026, 6, 1))
                .endDate(LocalDate.of(2026, 6, 5))
                .pickupLocation(downtown)
                .build();
        Reservation result = system.makeReservation(res4);
        System.out.println("Booking attempt result: " + (result == null ? "Rejected (vehicle unavailable)" : "Accepted"));
        System.out.println();

        System.out.println("=== Car Rental System Demo Complete ===");
    }
}

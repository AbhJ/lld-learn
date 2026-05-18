/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating optimized car rental with indexed vehicle lookup

import java.time.LocalDate;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Car Rental System (Optimized) ===\n");

        RentalSystem system = new RentalSystem();

        Location downtown = new Location("LOC-1", "Downtown Office", "123 Main St");
        Location airport = new Location("LOC-2", "Airport Terminal", "456 Airport Rd");

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

        Customer alice = new Customer("C-1", "Alice Johnson", "alice@email.com", "555-0101", "DL-12345");
        Customer bob = new Customer("C-2", "Bob Smith", "bob@email.com", "555-0102", "DL-67890");
        Customer charlie = new Customer("C-3", "Charlie Brown", "charlie@email.com", "555-0103", "DL-11111");

        // --- Test 1: Indexed Search ---
        System.out.println("--- Test 1: Search Available SUVs (indexed O(1) type lookup) ---");
        List<Vehicle> availableSUVs = system.searchAvailable("SUV");
        for (Vehicle v : availableSUVs) {
            System.out.println("  Found: " + v);
        }
        System.out.println();

        // --- Test 2: Cheapest Available ---
        System.out.println("--- Test 2: Cheapest Available SUV (O(1) PriorityQueue peek) ---");
        Vehicle cheapest = system.getCheapestAvailable("SUV");
        System.out.println("  Cheapest SUV: " + cheapest);
        System.out.println();

        // --- Test 3: Make Reservation ---
        System.out.println("--- Test 3: Make Reservation (Daily Pricing) ---");
        Reservation res1 = new Reservation.Builder(alice, suv1)
                .startDate(LocalDate.of(2026, 5, 15))
                .endDate(LocalDate.of(2026, 5, 20))
                .pickupLocation(downtown)
                .dropoffLocation(downtown)
                .addInsurance(Insurance.basic())
                .pricingStrategy(new DailyPricing())
                .build();
        system.makeReservation(res1);
        System.out.printf("Total: $%.2f%n", res1.getTotalCost());
        System.out.println();

        // --- Test 4: Index updated after reservation ---
        System.out.println("--- Test 4: Index Updated After Reservation ---");
        availableSUVs = system.searchAvailable("SUV", downtown);
        System.out.println("  Available SUVs at downtown: " + availableSUVs.size());
        System.out.println();

        // --- Test 5: Complete and re-index ---
        System.out.println("--- Test 5: Complete Reservation (re-indexes vehicle) ---");
        system.addToWaitlist("SUV", charlie);
        Bill bill = system.completeReservation(res1);
        System.out.println(bill);
        availableSUVs = system.searchAvailable("SUV", downtown);
        System.out.println("  Available SUVs at downtown after return: " + availableSUVs.size());
        System.out.println();

        System.out.println("=== Car Rental Demo Complete ===");
    }
}

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — 15 customers competing for 5 SUVs, exactly 5 get reservations

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Car Rental System Demo ===\n");

        RentalService service = new RentalService();

        // Add 5 SUVs to the fleet
        for (int i = 0; i < 5; i++) {
            service.addVehicle(new Vehicle("SUV-" + i, "SUV Model-" + i, VehicleType.SUV, 75.0));
        }

        int customerCount = 15;
        System.out.println("Scenario: " + customerCount + " customers simultaneously trying to reserve SUVs.");
        System.out.println("Available SUVs: 5");
        System.out.println("Expected: Exactly 5 get reservations, 10 are denied.\n");

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(customerCount);
        AtomicInteger reserved = new AtomicInteger(0);
        AtomicInteger denied = new AtomicInteger(0);
        List<String> results = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < customerCount; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    String customerName = "Customer-" + id;
                    Reservation res = service.reserve(customerName, VehicleType.SUV);
                    if (res != null) {
                        reserved.incrementAndGet();
                        results.add("  [RESERVED] " + customerName + " got " + res.getVehicle().getName());
                    } else {
                        denied.incrementAndGet();
                        results.add("  [DENIED]   " + customerName + " — no SUVs available");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }, "Customer-" + id).start();
        }

        startLatch.countDown();
        doneLatch.await();

        // Print results
        results.sort(String::compareTo);
        for (String r : results) {
            System.out.println(r);
        }

        // Verification
        System.out.println("\n--- Summary ---");
        System.out.println("Customers: " + customerCount);
        System.out.println("SUVs available: 5");
        System.out.println("Reservations made: " + reserved.get());
        System.out.println("Reservations denied: " + denied.get());
        System.out.println("Remaining SUVs: " + service.getAvailableCount(VehicleType.SUV));

        boolean correctReserved = reserved.get() == 5;
        boolean correctDenied = denied.get() == 10;
        boolean noRemainingCars = service.getAvailableCount(VehicleType.SUV) == 0;

        System.out.println("\nExactly 5 reservations: " + (correctReserved ? "PASSED" : "FAILED"));
        System.out.println("Exactly 10 denied: " + (correctDenied ? "PASSED" : "FAILED"));
        System.out.println("No SUVs remaining: " + (noRemainingCars ? "PASSED" : "FAILED"));

        boolean allPassed = correctReserved && correctDenied && noRemainingCars;
        System.out.println("\nOverall: " + (allPassed ? "ALL TESTS PASSED" : "SOME TESTS FAILED"));
    }
}

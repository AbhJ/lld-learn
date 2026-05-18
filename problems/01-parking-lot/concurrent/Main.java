/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — Spawns 20 threads all trying to park simultaneously

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Parking Lot Demo ===\n");

        // Create a parking lot with limited spots: 3 small, 5 medium, 2 large = 10 total
        ParkingLot lot = new ParkingLot();
        lot.addLevel(3, 5, 2);

        // 20 threads try to park cars simultaneously, but only 5 MEDIUM spots exist
        int threadCount = 20;
        CountDownLatch startLatch = new CountDownLatch(1);  // All threads start at once
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        List<String> results = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        System.out.println("Scenario: 20 cars trying to park, but only 5 MEDIUM spots available.");
        System.out.println("Expected: Exactly 5 succeed, 15 are rejected.\n");

        for (int i = 0; i < threadCount; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    startLatch.await(); // Wait for signal — all threads start simultaneously
                    Vehicle car = new Car("CAR-" + id);
                    Ticket ticket = lot.parkVehicle(car);
                    if (ticket != null) {
                        successCount.incrementAndGet();
                        results.add("  [PARKED] " + car + " -> " + ticket.getSpot().getSpotId());
                    } else {
                        failCount.incrementAndGet();
                        results.add("  [REJECTED] " + car + " — no spots available");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }, "Parker-" + i).start();
        }

        // Fire! All threads start at once
        startLatch.countDown();
        doneLatch.await();

        // Print results
        results.sort(String::compareTo);
        for (String r : results) {
            System.out.println(r);
        }

        System.out.println("\n--- Summary ---");
        System.out.println("Threads attempted: " + threadCount);
        System.out.println("Successfully parked: " + successCount.get());
        System.out.println("Correctly rejected: " + failCount.get());
        System.out.println("Active tickets: " + lot.getActiveTicketCount());

        // Verify correctness
        boolean correct = successCount.get() == 5 && failCount.get() == 15;
        System.out.println("\nCorrectness check: " + (correct ? "PASSED" : "FAILED"));
        if (!correct) {
            System.out.println("  ERROR: Expected 5 parked + 15 rejected, but got " +
                    successCount.get() + " parked + " + failCount.get() + " rejected");
        }

        System.out.println("\n--- Availability After Parking ---");
        System.out.println(lot.getAvailabilitySummary());
    }
}

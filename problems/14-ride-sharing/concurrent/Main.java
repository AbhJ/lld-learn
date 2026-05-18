/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — 20 riders competing for 5 drivers, only 5 succeed, others get re-queued

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Ride-Sharing Demo ===\n");

        ConcurrentMatchingService service = new ConcurrentMatchingService();

        // Register 5 drivers
        for (int i = 0; i < 5; i++) {
            service.registerDriver(new Driver("D-" + i, "Driver-" + i, 37.7 + i * 0.01, -122.4));
        }

        // 20 riders competing simultaneously
        int riderCount = 20;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(riderCount);
        AtomicInteger matched = new AtomicInteger(0);
        AtomicInteger queued = new AtomicInteger(0);
        List<String> results = Collections.synchronizedList(new ArrayList<>());

        System.out.println("Scenario: 20 riders request rides simultaneously, but only 5 drivers available.");
        System.out.println("Expected: Exactly 5 get matched, 15 go to wait queue.\n");

        for (int i = 0; i < riderCount; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    Rider rider = new Rider("R-" + id, "Rider-" + id, 37.7, -122.4);
                    Trip trip = service.requestRide(rider);
                    if (trip != null) {
                        matched.incrementAndGet();
                        results.add("  [MATCHED] " + rider.getName() + " -> " + trip.getDriver().getName());
                    } else {
                        queued.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }, "Rider-" + id).start();
        }

        startLatch.countDown();
        doneLatch.await();

        // Print matches
        results.sort(String::compareTo);
        for (String r : results) {
            System.out.println(r);
        }
        System.out.println("  [QUEUED] " + queued.get() + " riders waiting for a driver");

        // Verify driver states
        System.out.println("\nDriver states:");
        for (Driver d : service.getDrivers()) {
            System.out.println("  " + d);
        }

        // Verify: exactly 5 matched, 15 queued, no driver assigned to multiple riders
        Set<String> assignedDrivers = new HashSet<>();
        boolean noDuplicateAssignment = true;
        for (String r : results) {
            for (Driver d : service.getDrivers()) {
                if (r.contains(d.getName())) {
                    if (!assignedDrivers.add(d.getDriverId())) {
                        noDuplicateAssignment = false;
                    }
                }
            }
        }

        System.out.println("\n--- Summary ---");
        System.out.println("Riders: " + riderCount);
        System.out.println("Drivers: 5");
        System.out.println("Matched: " + matched.get());
        System.out.println("Queued: " + queued.get());
        System.out.println("Wait queue size: " + service.getWaitQueueSize());
        System.out.println("No duplicate driver assignment: " + (noDuplicateAssignment ? "PASSED" : "FAILED"));

        boolean correct = matched.get() == 5 && queued.get() == 15;
        System.out.println("\nCorrectness check: " + (correct ? "PASSED" : "FAILED"));
        if (!correct) {
            System.out.println("  Expected: 5 matched + 15 queued, got: " +
                    matched.get() + " matched + " + queued.get() + " queued");
        }
    }
}

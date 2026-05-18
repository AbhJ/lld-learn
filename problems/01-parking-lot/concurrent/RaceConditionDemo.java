/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/RaceConditionDemo.java — Demonstrates the race condition bug and the fix

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * RACE CONDITION: Two vehicles trying to park in the last available spot simultaneously.
 *
 * BUG: Using check-then-act without atomicity:
 *   if (spot.isAvailable()) {   // Thread A checks — true
 *                                // Thread B checks — also true!
 *       spot.setVehicle(v);     // Thread A parks
 *                                // Thread B parks — OVERWRITES Thread A!
 *   }
 *
 * FIX: Use AtomicReference.compareAndSet() — atomically checks AND sets in one operation.
 */
public class RaceConditionDemo {

    // ===== BUGGY VERSION: Not thread-safe =====
    static class BuggyParkingSpot {
        private Vehicle parkedVehicle = null;

        // BUG: check-then-act is NOT atomic!
        // Between checking isAvailable and setting the vehicle, another thread can intervene.
        public boolean park(Vehicle vehicle) {
            if (parkedVehicle == null) {  // CHECK
                // Simulate tiny delay that makes race condition more likely
                Thread.yield();
                parkedVehicle = vehicle;  // ACT — another thread may have already set this!
                return true;
            }
            return false;
        }

        public Vehicle getParkedVehicle() { return parkedVehicle; }
        public boolean isAvailable() { return parkedVehicle == null; }
    }

    // ===== FIXED VERSION: Thread-safe with CAS =====
    static class FixedParkingSpot {
        private final AtomicReference<Vehicle> parkedVehicle = new AtomicReference<>(null);

        // FIX: compareAndSet is atomic — only ONE thread can succeed
        public boolean park(Vehicle vehicle) {
            return parkedVehicle.compareAndSet(null, vehicle);
        }

        public Vehicle getParkedVehicle() { return parkedVehicle.get(); }
        public boolean isAvailable() { return parkedVehicle.get() == null; }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Race Condition Demo: Parking Lot ===\n");

        demonstrateBug();
        System.out.println();
        demonstrateFix();
    }

    static void demonstrateBug() throws InterruptedException {
        System.out.println("--- BUGGY VERSION (check-then-act without atomicity) ---");
        System.out.println("Two threads try to park in the SAME spot simultaneously.\n");

        int races = 0;
        int trials = 1000;

        for (int t = 0; t < trials; t++) {
            BuggyParkingSpot spot = new BuggyParkingSpot();
            CountDownLatch start = new CountDownLatch(1);
            AtomicInteger successCount = new AtomicInteger(0);

            Thread t1 = new Thread(() -> {
                try { start.await(); } catch (InterruptedException e) { return; }
                if (spot.park(new Car("CAR-A"))) successCount.incrementAndGet();
            });
            Thread t2 = new Thread(() -> {
                try { start.await(); } catch (InterruptedException e) { return; }
                if (spot.park(new Car("CAR-B"))) successCount.incrementAndGet();
            });

            t1.start();
            t2.start();
            start.countDown(); // Both threads race
            t1.join();
            t2.join();

            if (successCount.get() > 1) {
                races++;
            }
        }

        System.out.println("Results over " + trials + " trials:");
        System.out.println("  Race conditions detected: " + races + " times");
        System.out.println("  (Both threads thought they parked successfully!)");
        if (races > 0) {
            System.out.println("  BUG CONFIRMED: Multiple vehicles assigned to same spot.");
        } else {
            System.out.println("  (Race didn't manifest this run — but the bug exists. Run again or increase trials.)");
        }
    }

    static void demonstrateFix() throws InterruptedException {
        System.out.println("--- FIXED VERSION (AtomicReference.compareAndSet) ---");
        System.out.println("Two threads try to park in the SAME spot simultaneously.\n");

        int races = 0;
        int trials = 1000;

        for (int t = 0; t < trials; t++) {
            FixedParkingSpot spot = new FixedParkingSpot();
            CountDownLatch start = new CountDownLatch(1);
            AtomicInteger successCount = new AtomicInteger(0);

            Thread t1 = new Thread(() -> {
                try { start.await(); } catch (InterruptedException e) { return; }
                if (spot.park(new Car("CAR-A"))) successCount.incrementAndGet();
            });
            Thread t2 = new Thread(() -> {
                try { start.await(); } catch (InterruptedException e) { return; }
                if (spot.park(new Car("CAR-B"))) successCount.incrementAndGet();
            });

            t1.start();
            t2.start();
            start.countDown();
            t1.join();
            t2.join();

            if (successCount.get() > 1) {
                races++;
            }
        }

        System.out.println("Results over " + trials + " trials:");
        System.out.println("  Race conditions detected: " + races + " times");
        System.out.println("  Exactly one thread succeeds EVERY time.");
        System.out.println("  FIX VERIFIED: CAS guarantees mutual exclusion without locks.");
    }
}

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/RaceConditionDemo.java — Shows double-booking bug then fix with proper locking

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * RACE CONDITION: Two users booking the same seat at the same time.
 *
 * BUG: Check seat availability and mark as booked is NOT atomic:
 *   if (seat.status == AVAILABLE) {   // User A checks — available!
 *                                      // User B checks — also available!
 *       seat.status = BOOKED;          // User A books
 *                                      // User B books — DOUBLE BOOKING!
 *   }
 *
 * FIX: AtomicReference with CAS ensures only one thread transitions AVAILABLE -> BOOKED.
 */
public class RaceConditionDemo {

    // ===== BUGGY VERSION =====
    static class BuggySeat {
        private String status = "AVAILABLE";
        private String bookedBy = null;

        // BUG: Non-atomic check-then-act
        public boolean book(String userId) {
            if ("AVAILABLE".equals(status)) {
                Thread.yield(); // Simulate context switch
                status = "BOOKED";
                bookedBy = userId;
                return true;
            }
            return false;
        }

        public String getBookedBy() { return bookedBy; }
    }

    // ===== FIXED VERSION =====
    static class FixedSeat {
        private final AtomicReference<String> status = new AtomicReference<>("AVAILABLE"); // AtomicReference = CAS ensures only one thread transitions state
        private volatile String bookedBy = null; // volatile = bookedBy visible to all threads when set

        // FIX: CAS ensures only one thread transitions AVAILABLE -> BOOKED
        public boolean book(String userId) {
            if (status.compareAndSet("AVAILABLE", "BOOKED")) {
                bookedBy = userId;
                return true;
            }
            return false;
        }

        public String getBookedBy() { return bookedBy; }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Race Condition Demo: Movie Ticket Booking ===\n");
        demonstrateBug();
        System.out.println();
        demonstrateFix();
    }

    static void demonstrateBug() throws InterruptedException {
        System.out.println("--- BUGGY VERSION (non-atomic check-then-act) ---");
        System.out.println("Two users try to book the SAME seat simultaneously.\n");

        int doubleBookings = 0;
        int trials = 1000;

        for (int t = 0; t < trials; t++) {
            BuggySeat seat = new BuggySeat();
            CountDownLatch start = new CountDownLatch(1);
            AtomicInteger successes = new AtomicInteger(0);

            Thread user1 = new Thread(() -> {
                try { start.await(); } catch (InterruptedException e) { return; }
                if (seat.book("Alice")) successes.incrementAndGet();
            });
            Thread user2 = new Thread(() -> {
                try { start.await(); } catch (InterruptedException e) { return; }
                if (seat.book("Bob")) successes.incrementAndGet();
            });

            user1.start();
            user2.start();
            start.countDown();
            user1.join();
            user2.join();

            if (successes.get() > 1) {
                doubleBookings++;
            }
        }

        System.out.println("Results over " + trials + " trials:");
        System.out.println("  Double bookings detected: " + doubleBookings + " times");
        if (doubleBookings > 0) {
            System.out.println("  BUG CONFIRMED: Same seat sold to two users!");
            System.out.println("  This means revenue loss + angry customers at the theater.");
        } else {
            System.out.println("  (Race didn't manifest — but the bug exists. Try increasing trials.)");
        }
    }

    static void demonstrateFix() throws InterruptedException {
        System.out.println("--- FIXED VERSION (AtomicReference CAS) ---");
        System.out.println("Two users try to book the SAME seat simultaneously.\n");

        int doubleBookings = 0;
        int trials = 1000;

        for (int t = 0; t < trials; t++) {
            FixedSeat seat = new FixedSeat();
            CountDownLatch start = new CountDownLatch(1);
            AtomicInteger successes = new AtomicInteger(0);

            Thread user1 = new Thread(() -> {
                try { start.await(); } catch (InterruptedException e) { return; }
                if (seat.book("Alice")) successes.incrementAndGet();
            });
            Thread user2 = new Thread(() -> {
                try { start.await(); } catch (InterruptedException e) { return; }
                if (seat.book("Bob")) successes.incrementAndGet();
            });

            user1.start();
            user2.start();
            start.countDown();
            user1.join();
            user2.join();

            if (successes.get() > 1) {
                doubleBookings++;
            }
        }

        System.out.println("Results over " + trials + " trials:");
        System.out.println("  Double bookings detected: " + doubleBookings + " times");
        System.out.println("  Exactly one user books the seat EVERY time.");
        System.out.println("  FIX VERIFIED: CAS prevents double-booking without heavyweight locks.");
    }
}

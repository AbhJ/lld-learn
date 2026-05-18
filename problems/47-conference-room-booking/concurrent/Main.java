/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — 20 bookings for same room/time, exactly 1 succeeds

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Conference Room Booking Demo ===\n");
        System.out.println("Race condition: Two meetings booked in same room at overlapping times.\n");

        List<String> rooms = List.of("Room-A", "Room-B");
        RoomBookingService service = new RoomBookingService(rooms);

        int threadCount = 20;
        long slotStart = 900; // 9:00 AM
        long slotEnd = 1000;  // 10:00 AM

        // Test 1: 20 threads booking exact same slot in Room-A
        System.out.println("--- Test 1: 20 threads, same room, same time ---");
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger wins = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    TimeSlot slot = new TimeSlot(slotStart, slotEnd);
                    Booking booking = new Booking("BK-" + id, "User-" + id, slot, "Room-A");
                    if (service.bookRoom("Room-A", booking)) {
                        wins.incrementAndGet();
                        System.out.println("  User-" + id + " BOOKED Room-A " + slot);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        doneLatch.await();

        // Test 2: Non-overlapping bookings should all succeed
        System.out.println("\n--- Test 2: Non-overlapping bookings ---");
        int nonOverlapCount = 5;
        int nonOverlapSuccess = 0;
        for (int i = 0; i < nonOverlapCount; i++) {
            long start = 1100 + (i * 200);
            long end = start + 100;
            TimeSlot slot = new TimeSlot(start, end);
            Booking booking = new Booking("NOL-" + i, "UserB-" + i, slot, "Room-B");
            if (service.bookRoom("Room-B", booking)) {
                nonOverlapSuccess++;
            }
        }

        System.out.println("  Non-overlapping bookings succeeded: " + nonOverlapSuccess + "/" + nonOverlapCount);

        // Results
        System.out.println("\n--- Results ---");
        System.out.println("Threads competing for same slot: " + threadCount);
        System.out.println("Winners (same slot): " + wins.get());
        System.out.println("Total successful bookings: " + service.getSuccessCount());
        System.out.println("Total rejections: " + service.getRejectCount());
        System.out.println("Room-A bookings: " + service.getBookingCount("Room-A"));
        System.out.println("Room-B bookings: " + service.getBookingCount("Room-B"));

        boolean exactlyOneWins = (wins.get() == 1);
        boolean allNonOverlap = (nonOverlapSuccess == nonOverlapCount);
        boolean rejectsCorrect = (service.getRejectCount() == threadCount - 1);

        System.out.println("\nExactly one winner: " + exactlyOneWins);
        System.out.println("All non-overlapping accepted: " + allNonOverlap);
        System.out.println("Rejections = " + (threadCount - 1) + ": " + rejectsCorrect);

        boolean passed = exactlyOneWins && allNonOverlap && rejectsCorrect;
        System.out.println("\nCorrectness check: " + (passed ? "PASSED" : "FAILED"));
    }
}

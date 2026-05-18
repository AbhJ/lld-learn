/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — 10 threads booking same time range, only 1 succeeds

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Calendar System Demo ===\n");
        System.out.println("Race condition: Two people booking overlapping time slots simultaneously.\n");

        CalendarService calendar = new CalendarService();
        int threadCount = 10;
        long slotStart = 1000L; // Same time slot for all
        long slotEnd = 2000L;

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    Event event = new Event(
                        "evt-" + threadId,
                        "Meeting-" + threadId,
                        slotStart,
                        slotEnd,
                        "User-" + threadId
                    );
                    boolean success = calendar.book(event);
                    if (success) {
                        successCount.incrementAndGet();
                        System.out.println("  User-" + threadId + " BOOKED successfully");
                    } else {
                        System.out.println("  User-" + threadId + " REJECTED (conflict)");
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

        // Now test non-overlapping bookings — should all succeed
        System.out.println("\n--- Non-overlapping bookings test ---");
        int nonOverlapSuccess = 0;
        for (int i = 0; i < 5; i++) {
            long start = 3000L + (i * 1000);
            long end = start + 500;
            Event event = new Event("nol-" + i, "Separate-" + i, start, end, "User-X");
            if (calendar.book(event)) {
                nonOverlapSuccess++;
            }
        }

        System.out.println("\n--- Results ---");
        System.out.println("Threads competing for same slot: " + threadCount);
        System.out.println("Successful bookings for same slot: " + successCount.get());
        System.out.println("Rejected bookings: " + calendar.getRejectedCount());
        System.out.println("Non-overlapping bookings succeeded: " + nonOverlapSuccess + "/5");
        System.out.println("Total events in calendar: " + calendar.getBookedCount());

        boolean exactlyOneWins = (successCount.get() == 1);
        boolean allNonOverlap = (nonOverlapSuccess == 5);
        boolean totalCorrect = (calendar.getBookedCount() == 6); // 1 overlapping + 5 non-overlapping

        System.out.println("\nExactly one winner: " + exactlyOneWins);
        System.out.println("Non-overlapping all accepted: " + allNonOverlap);

        boolean passed = exactlyOneWins && allNonOverlap && totalCorrect;
        System.out.println("\nCorrectness check: " + (passed ? "PASSED" : "FAILED"));
    }
}

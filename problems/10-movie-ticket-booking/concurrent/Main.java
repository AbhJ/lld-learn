/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — 50 threads trying to book same seats, only correct number succeeds

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Movie Ticket Booking Demo ===\n");

        BookingService service = new BookingService();

        // Create a show with 5 rows x 5 columns = 25 seats
        Show show = new Show("SHOW-1", "Inception", "7:00 PM", 5, 5);
        service.addShow(show);

        // 50 users all trying to book seat A1 and A2 (same 2 seats)
        int threadCount = 50;
        List<String> targetSeats = List.of("A1", "A2");
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<String> results = Collections.synchronizedList(new ArrayList<>());

        System.out.println("Scenario: 50 users all trying to book seats A1+A2 for the same show.");
        System.out.println("Expected: Exactly 1 succeeds, 49 are rejected (no double-booking).\n");

        for (int i = 0; i < threadCount; i++) {
            final String userId = "USER-" + i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    Booking booking = service.bookSeats(userId, "SHOW-1", targetSeats);
                    if (booking != null) {
                        successCount.incrementAndGet();
                        results.add("  [BOOKED] " + userId + " -> " + booking.getBookingId());
                    } else {
                        failCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }, "Booker-" + i).start();
        }

        startLatch.countDown();
        doneLatch.await();

        // Print results
        for (String r : results) {
            System.out.println(r);
        }
        System.out.println("  [REJECTED] " + failCount.get() + " users could not book (seats taken)");

        System.out.println("\n--- Summary ---");
        System.out.println("Threads attempted: " + threadCount);
        System.out.println("Successfully booked: " + successCount.get());
        System.out.println("Correctly rejected: " + failCount.get());
        System.out.println("Seat A1 status: " + show.getSeat("A1").getStatus());
        System.out.println("Seat A2 status: " + show.getSeat("A2").getStatus());

        boolean correct = successCount.get() == 1 && failCount.get() == 49;
        System.out.println("\nCorrectness check: " + (correct ? "PASSED" : "FAILED"));
        System.out.println("Total booked seats in show: " + show.getBookedCount() + "/25");
    }
}

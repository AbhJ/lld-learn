/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — 20 guests booking 5 rooms, exactly 5 succeed

import model.Room;
import model.Booking;
import service.HotelService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Hotel Management Demo ===\n");

        final int NUM_ROOMS = 5;
        final int NUM_GUESTS = 20;

        HotelService hotel = new HotelService();
        for (int i = 1; i <= NUM_ROOMS; i++) {
            hotel.addRoom(new Room(i, "DOUBLE", 150.0));
        }

        System.out.println("Hotel: " + NUM_ROOMS + " rooms available");
        System.out.println("Guests attempting to book simultaneously: " + NUM_GUESTS + "\n");

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(NUM_GUESTS);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < NUM_GUESTS; i++) {
            final String guestName = "Guest-" + i;
            Thread t = new Thread(() -> {
                try {
                    startLatch.await();
                } catch (InterruptedException ignored) {}

                Booking booking = hotel.bookRoom(guestName, "2026-05-13");
                if (booking != null) {
                    successCount.incrementAndGet();
                    System.out.println("  [SUCCESS] " + booking);
                } else {
                    failCount.incrementAndGet();
                    System.out.println("  [REJECTED] " + guestName + " — no rooms available");
                }
                doneLatch.countDown();
            });
            threads.add(t);
            t.start();
        }

        // Release all threads simultaneously
        startLatch.countDown();
        doneLatch.await();

        System.out.println("\n--- Results ---");
        System.out.println("Rooms booked: " + hotel.getBookedCount() + "/" + NUM_ROOMS);
        System.out.println("Rooms available: " + hotel.getAvailableCount());

        System.out.println("\nRoom status:");
        for (Room r : hotel.getRooms()) {
            System.out.println("  " + r);
        }

        int succeeded = successCount.get();
        int rejected = failCount.get();

        System.out.println("\n" + NUM_GUESTS + " threads attempted, " + succeeded + " succeeded, " + rejected + " correctly rejected");

        boolean passed = (succeeded == NUM_ROOMS) && (rejected == NUM_GUESTS - NUM_ROOMS)
                && (hotel.getBookedCount() == NUM_ROOMS);
        System.out.println("Correctness check: " + (passed ? "PASSED" : "FAILED"));
    }
}

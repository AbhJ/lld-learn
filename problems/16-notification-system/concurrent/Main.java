/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — 100 events for same user with limit=5/second, exactly 5 delivered

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Notification System Demo ===\n");

        int rateLimit = 5;
        Throttler throttler = new Throttler(rateLimit);
        NotificationService service = new NotificationService(throttler);

        int eventCount = 100;
        String targetUser = "user-alice";

        System.out.println("Scenario: " + eventCount + " events all targeting " + targetUser + " simultaneously.");
        System.out.println("Rate limit: " + rateLimit + " notifications per window.");
        System.out.println("Expected: Exactly " + rateLimit + " delivered, " +
                (eventCount - rateLimit) + " throttled.\n");

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(eventCount);
        AtomicInteger deliveredCount = new AtomicInteger(0);
        AtomicInteger throttledCount = new AtomicInteger(0);

        for (int i = 0; i < eventCount; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    Notification notif = new Notification(targetUser, "Event-" + id);
                    boolean sent = service.send(notif);
                    if (sent) {
                        deliveredCount.incrementAndGet();
                    } else {
                        throttledCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }, "Event-" + id).start();
        }

        // Release all threads simultaneously
        startLatch.countDown();
        doneLatch.await();

        // Print delivered notifications
        System.out.println("Delivered notifications:");
        for (Notification n : service.getDelivered()) {
            System.out.println("  [DELIVERED] " + n.getMessage() + " -> " + n.getUserId());
        }

        System.out.println("\nThrottled: " + throttledCount.get() + " notifications suppressed.");

        // Verification
        System.out.println("\n--- Summary ---");
        System.out.println("Total events: " + eventCount);
        System.out.println("Rate limit: " + rateLimit + " per window");
        System.out.println("Delivered: " + deliveredCount.get());
        System.out.println("Throttled: " + throttledCount.get());
        System.out.println("Throttler counter: " + throttler.getCount(targetUser));

        boolean correctDelivered = deliveredCount.get() == rateLimit;
        boolean correctThrottled = throttledCount.get() == (eventCount - rateLimit);
        boolean noOverdelivery = deliveredCount.get() <= rateLimit;
        boolean totalCorrect = deliveredCount.get() + throttledCount.get() == eventCount;

        System.out.println("\nExactly " + rateLimit + " delivered: " + (correctDelivered ? "PASSED" : "FAILED"));
        System.out.println("Exactly " + (eventCount - rateLimit) + " throttled: " + (correctThrottled ? "PASSED" : "FAILED"));
        System.out.println("No over-delivery: " + (noOverdelivery ? "PASSED" : "FAILED"));
        System.out.println("Total accounted for: " + (totalCorrect ? "PASSED" : "FAILED"));

        boolean allPassed = correctDelivered && correctThrottled && noOverdelivery && totalCorrect;
        System.out.println("\nOverall: " + (allPassed ? "ALL TESTS PASSED" : "SOME TESTS FAILED"));
    }
}

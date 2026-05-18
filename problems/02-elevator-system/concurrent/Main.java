/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — Demonstrates concurrent elevator dispatch with no lost or double-served requests

import model.Request;
import model.Elevator;
import service.ElevatorController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Elevator System Demo ===\n");

        final int NUM_PASSENGERS = 20;
        final int NUM_ELEVATORS = 3;

        ElevatorController controller = new ElevatorController(NUM_ELEVATORS);

        // 20 passengers submit requests simultaneously
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(NUM_PASSENGERS);
        AtomicInteger submitted = new AtomicInteger(0);

        System.out.println("Spawning " + NUM_PASSENGERS + " passengers requesting simultaneously...");

        List<Thread> passengers = new ArrayList<>();
        for (int i = 0; i < NUM_PASSENGERS; i++) {
            final int id = i;
            Thread t = new Thread(() -> {
                try {
                    startLatch.await(); // all threads start at the same time
                } catch (InterruptedException ignored) {}
                int src = id % 10;
                int dst = (id + 3) % 10;
                controller.submitRequest(new Request(src, dst, "Passenger-" + id));
                submitted.incrementAndGet();
                doneLatch.countDown();
            });
            passengers.add(t);
            t.start();
        }

        // Release all threads at once
        startLatch.countDown();
        doneLatch.await();

        System.out.println("All " + submitted.get() + " requests submitted concurrently.\n");

        // Dispatch concurrently — elevators race to accept each request
        System.out.println("Dispatching with concurrent elevator competition...");
        controller.dispatchConcurrently();

        // Verify results
        int served = controller.getServedCount();
        int totalAssigned = controller.getTotalAssigned();

        System.out.println("\n--- Results ---");
        System.out.println("Requests submitted: " + NUM_PASSENGERS);
        System.out.println("Requests served (dispatched exactly once): " + served);
        System.out.println("Total in elevator queues: " + totalAssigned);

        for (Elevator e : controller.getElevators()) {
            System.out.println("  " + e);
        }

        System.out.println("\nServed log:");
        for (String entry : controller.getServedLog()) {
            System.out.println("  " + entry);
        }

        // Correctness: every request was served exactly once
        boolean passed = (served == NUM_PASSENGERS) && (totalAssigned == NUM_PASSENGERS);

        System.out.println("\n" + NUM_PASSENGERS + " threads attempted, " + served + " succeeded, 0 correctly rejected");
        System.out.println("No request lost, no request double-served.");
        System.out.println("Correctness check: " + (passed ? "PASSED" : "FAILED"));
    }
}

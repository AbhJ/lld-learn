/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — Timer and emergency threads competing, verify no invalid state

import model.Signal;
import service.TrafficController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Traffic Signal Demo ===\n");

        final int TIMER_CYCLES = 50;
        final int EMERGENCY_OVERRIDES = 30;

        TrafficController controller = new TrafficController();

        System.out.println("Initial state: " + controller.getNorthSouth() + ", " + controller.getEastWest());
        System.out.println("Timer cycles to run: " + TIMER_CYCLES);
        System.out.println("Emergency overrides to run: " + EMERGENCY_OVERRIDES + "\n");

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        // Timer thread — cycles the signals regularly
        Thread timerThread = new Thread(() -> {
            try {
                startLatch.await();
            } catch (InterruptedException ignored) {}
            for (int i = 0; i < TIMER_CYCLES; i++) {
                controller.timerCycle();
                try { Thread.sleep(1); } catch (InterruptedException ignored) {}
            }
            doneLatch.countDown();
        });

        // Emergency thread — randomly forces signals
        Thread emergencyThread = new Thread(() -> {
            try {
                startLatch.await();
            } catch (InterruptedException ignored) {}
            for (int i = 0; i < EMERGENCY_OVERRIDES; i++) {
                String direction = (i % 2 == 0) ? "NS" : "EW";
                controller.emergencyOverride(direction);
                try { Thread.sleep(1); } catch (InterruptedException ignored) {}
            }
            doneLatch.countDown();
        });

        timerThread.start();
        emergencyThread.start();

        // Release both threads simultaneously
        startLatch.countDown();
        doneLatch.await();

        // Print results
        System.out.println("--- Results ---");
        System.out.println("Timer transitions completed: " + controller.getTimerTransitions());
        System.out.println("Emergency overrides completed: " + controller.getEmergencyOverrides());
        System.out.println("Final state: " + controller.getNorthSouth() + ", " + controller.getEastWest());

        System.out.println("\nState log (last 10 entries):");
        List<String> log = controller.getStateLog();
        int start = Math.max(0, log.size() - 10);
        for (int i = start; i < log.size(); i++) {
            System.out.println("  " + log.get(i));
        }

        int totalAttempts = TIMER_CYCLES + EMERGENCY_OVERRIDES;
        int totalCompleted = controller.getTimerTransitions() + controller.getEmergencyOverrides();
        boolean passed = !controller.isInvalidStateDetected();

        System.out.println("\n" + totalAttempts + " threads attempted, " + totalCompleted + " succeeded, 0 correctly rejected");
        System.out.println("No two-green invalid state detected: " + passed);
        System.out.println("Correctness check: " + (passed ? "PASSED" : "FAILED"));
    }
}

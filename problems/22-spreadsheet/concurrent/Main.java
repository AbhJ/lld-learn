/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — 10 threads editing interdependent cells simultaneously, verify formulas consistent

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Spreadsheet Demo ===\n");

        Spreadsheet sheet = new Spreadsheet();

        // Setup: A1..A10 are raw values, B1 = SUM(A1..A10)
        for (int i = 1; i <= 10; i++) {
            sheet.setCellValue("A" + i, 1.0);
        }
        String[] deps = new String[10];
        for (int i = 0; i < 10; i++) deps[i] = "A" + (i + 1);
        sheet.setCellFormula("B1", "SUM(A1:A10)", deps);

        int threadCount = 10;
        int opsPerThread = 200;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicBoolean errorFound = new AtomicBoolean(false);
        AtomicInteger totalEdits = new AtomicInteger(0);
        AtomicInteger staleRetries = new AtomicInteger(0);

        System.out.println("Scenario: 10 threads each edit A1..A10 (200 ops each)");
        System.out.println("  while B1 = SUM(A1:A10) is recalculated.");
        System.out.println("Expected: No torn reads, versions monotonically increase.\n");

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < opsPerThread; i++) {
                        String cellId = "A" + ((threadId % 10) + 1);
                        double newVal = threadId * 1000.0 + i;
                        long prevVersion = sheet.getCellVersion(cellId);

                        sheet.setCellValue(cellId, newVal);
                        totalEdits.incrementAndGet();

                        long newVersion = sheet.getCellVersion(cellId);
                        if (newVersion < prevVersion) {
                            errorFound.set(true); // version went backwards
                        }

                        // Recalculate B1
                        boolean success = sheet.recalculate("B1", deps);
                        if (!success) {
                            staleRetries.incrementAndGet();
                        }

                        // Read B1 — should never be NaN or negative
                        double b1 = sheet.getCellValue("B1");
                        if (Double.isNaN(b1) || Double.isInfinite(b1)) {
                            errorFound.set(true);
                        }
                    }
                } catch (Exception e) {
                    errorFound.set(true);
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            }, "Editor-" + t).start();
        }

        startLatch.countDown();
        doneLatch.await();

        // Final recalculation
        sheet.recalculate("B1", deps);

        // Verify final state
        double sum = 0;
        for (int i = 1; i <= 10; i++) {
            sum += sheet.getCellValue("A" + i);
        }
        double b1Final = sheet.getCellValue("B1");
        boolean sumMatches = Math.abs(sum - b1Final) < 0.001;

        System.out.println("--- Results ---");
        System.out.println("Total edits: " + totalEdits.get());
        System.out.println("Stale-read retries: " + staleRetries.get());
        System.out.println("Final SUM(A1:A10) = " + sum);
        System.out.println("Final B1 = " + b1Final);
        System.out.println("Sum matches B1 (after final recalc): " + sumMatches);
        System.out.println("No version anomalies: " + !errorFound.get());

        boolean passed = !errorFound.get() && sumMatches;
        System.out.println("\nCorrectness check: " + (passed ? "PASSED" : "FAILED"));
    }
}

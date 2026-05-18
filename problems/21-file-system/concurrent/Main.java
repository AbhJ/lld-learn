/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — 10 threads creating/deleting files in same dir, verify directory always consistent

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent File System Demo ===\n");

        FileSystem fs = new FileSystem();
        fs.createDirectory("/", "shared");

        int threadCount = 10;
        int opsPerThread = 100;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicBoolean inconsistencyFound = new AtomicBoolean(false);
        AtomicInteger totalCreates = new AtomicInteger(0);
        AtomicInteger totalDeletes = new AtomicInteger(0);

        System.out.println("Scenario: 10 threads each perform 100 create/delete operations");
        System.out.println("  in the same directory simultaneously.");
        System.out.println("Expected: Directory listing is always consistent (no corruption).\n");

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            new Thread(() -> {
                try {
                    startLatch.await();
                    Random random = new Random(threadId);
                    for (int i = 0; i < opsPerThread; i++) {
                        String fileName = "file-" + threadId + "-" + i + ".txt";
                        // Create
                        fs.createFile("/shared", fileName, "content-" + threadId + "-" + i);
                        totalCreates.incrementAndGet();

                        // Verify listing is a valid snapshot
                        Set<String> listing = fs.listDirectory("/shared");
                        if (listing == null) {
                            inconsistencyFound.set(true);
                        }

                        // Sometimes delete own files
                        if (random.nextBoolean()) {
                            fs.deleteFile("/shared", fileName);
                            totalDeletes.incrementAndGet();
                        }

                        // Verify child count is non-negative
                        int count = fs.getChildCount("/shared");
                        if (count < 0) {
                            inconsistencyFound.set(true);
                        }
                    }
                } catch (Exception e) {
                    inconsistencyFound.set(true);
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            }, "FileWorker-" + t).start();
        }

        startLatch.countDown();
        doneLatch.await();

        // Final verification
        Set<String> finalListing = fs.listDirectory("/shared");
        int finalCount = fs.getChildCount("/shared");
        boolean listingMatchesCount = (finalListing != null && finalListing.size() == finalCount);

        System.out.println("--- Results ---");
        System.out.println("Total creates: " + totalCreates.get());
        System.out.println("Total deletes: " + totalDeletes.get());
        System.out.println("Final file count: " + finalCount);
        System.out.println("Listing size matches count: " + listingMatchesCount);
        System.out.println("No inconsistency detected: " + !inconsistencyFound.get());

        boolean passed = !inconsistencyFound.get() && listingMatchesCount && finalCount >= 0;
        System.out.println("\nCorrectness check: " + (passed ? "PASSED" : "FAILED"));
    }
}

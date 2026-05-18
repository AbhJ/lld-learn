/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — 20 threads logging 100 messages each, verify no interleaving, all 2000 present

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Logging Framework Demo ===\n");

        int threadCount = 20;
        int messagesPerThread = 100;
        int totalExpected = threadCount * messagesPerThread;

        AsyncAppender appender = new AsyncAppender(totalExpected + 100);
        appender.start();
        Logger logger = new Logger("AppLogger", appender, LogLevel.DEBUG);

        System.out.println("Scenario: " + threadCount + " threads logging " + messagesPerThread + " messages each.");
        System.out.println("Expected: All " + totalExpected + " messages written, no interleaving, no loss.\n");

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger submitted = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int m = 0; m < messagesPerThread; m++) {
                        LogLevel level = LogLevel.values()[m % 4];
                        logger.log(level, "Thread-" + threadId + " message-" + m);
                        submitted.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }, "Worker-" + threadId).start();
        }

        // Release all threads simultaneously
        startLatch.countDown();
        doneLatch.await();

        // Shutdown appender and wait for drain
        appender.shutdown();

        // Verify output
        List<String> outputLines = new ArrayList<>(appender.getOutput());
        int writtenCount = appender.getWrittenCount();

        // Show sample output (first 10 and last 5)
        System.out.println("Sample output (first 10 entries):");
        for (int i = 0; i < Math.min(10, outputLines.size()); i++) {
            System.out.println("  " + outputLines.get(i));
        }
        if (outputLines.size() > 10) {
            System.out.println("  ... (" + (outputLines.size() - 15) + " more entries) ...");
            for (int i = Math.max(10, outputLines.size() - 5); i < outputLines.size(); i++) {
                System.out.println("  " + outputLines.get(i));
            }
        }

        // Check for interleaving (each line should be a complete formatted entry)
        boolean noInterleaving = true;
        for (String line : outputLines) {
            // Each line should match format: [XXXXX] [LEVEL] [thread] message
            if (!line.matches("\\[\\d{5}\\] \\[\\w+\\s*\\] \\[.+\\] .+")) {
                noInterleaving = false;
                System.out.println("  [INTERLEAVED] " + line);
                break;
            }
        }

        // Check all messages are present (no loss)
        Set<String> expectedMessages = new HashSet<>();
        for (int t = 0; t < threadCount; t++) {
            for (int m = 0; m < messagesPerThread; m++) {
                expectedMessages.add("Thread-" + t + " message-" + m);
            }
        }

        Set<String> actualMessages = new HashSet<>();
        for (String line : outputLines) {
            // Extract message part after the last ']'
            int lastBracket = line.lastIndexOf(']');
            if (lastBracket >= 0 && lastBracket + 2 < line.length()) {
                actualMessages.add(line.substring(lastBracket + 2));
            }
        }

        int missingCount = 0;
        for (String expected : expectedMessages) {
            if (!actualMessages.contains(expected)) {
                missingCount++;
            }
        }

        // Summary
        System.out.println("\n--- Summary ---");
        System.out.println("Threads: " + threadCount);
        System.out.println("Messages per thread: " + messagesPerThread);
        System.out.println("Total submitted: " + submitted.get());
        System.out.println("Total written: " + writtenCount);
        System.out.println("Missing messages: " + missingCount);

        boolean allWritten = writtenCount == totalExpected;
        boolean noneMissing = missingCount == 0;

        System.out.println("\nAll " + totalExpected + " messages written: " + (allWritten ? "PASSED" : "FAILED"));
        System.out.println("No interleaving: " + (noInterleaving ? "PASSED" : "FAILED"));
        System.out.println("No missing messages: " + (noneMissing ? "PASSED" : "FAILED"));

        boolean allPassed = allWritten && noInterleaving && noneMissing;
        System.out.println("\nOverall: " + (allPassed ? "ALL TESTS PASSED" : "SOME TESTS FAILED"));
    }
}

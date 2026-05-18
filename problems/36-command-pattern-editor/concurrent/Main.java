/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — 2 editor threads with interleaved edits, verify no lost edits

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Command Pattern Editor Demo ===\n");

        ConcurrentDocument doc = new ConcurrentDocument();
        int editsPerEditor = 50;
        int editorCount = 2;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(editorCount);
        AtomicInteger totalEditsAttempted = new AtomicInteger(0);

        System.out.println("Scenario: 2 editor threads each make 50 edits with optimistic locking.");
        System.out.println("  Each edit uses CAS to check version before applying.");
        System.out.println("Expected: All 100 edits eventually applied, no lost edits.\n");

        for (int e = 0; e < editorCount; e++) {
            final String editorName = "Editor-" + (char)('A' + e);
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < editsPerEditor; i++) {
                        String text = editorName + "-line-" + i;
                        doc.applyEditWithRetry(editorName, text);
                        totalEditsAttempted.incrementAndGet();
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }, editorName).start();
        }

        startLatch.countDown();
        doneLatch.await();

        int expectedTotal = editorCount * editsPerEditor;

        System.out.println("--- Results ---");
        System.out.println("Total edits applied: " + doc.getSuccessfulEdits());
        System.out.println("Conflicts detected (retried): " + doc.getConflictedEdits());
        System.out.println("Document version: " + doc.getVersion());
        System.out.println("Content lines: " + doc.getContentSize());
        System.out.println("History entries: " + doc.getHistorySize());
        System.out.println("Expected total: " + expectedTotal);

        boolean passed = doc.getSuccessfulEdits() == expectedTotal
                && doc.getContentSize() == expectedTotal
                && doc.getVersion() == expectedTotal;
        System.out.println("\nCorrectness check: " + (passed ? "PASSED" : "FAILED"));
    }
}

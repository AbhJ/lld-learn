/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — 5 collaborators editing simultaneously, verify no lost edits

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Document Editor Demo ===\n");
        System.out.println("Race condition: Two collaborators editing same paragraph —");
        System.out.println("one's changes overwrite the other's.\n");

        CollaborativeEditor editor = new CollaborativeEditor();
        editor.addSegment("para-1", "Initial content of paragraph 1");

        int collaboratorCount = 5;
        int editsPerCollaborator = 50;
        int totalAttempts = collaboratorCount * editsPerCollaborator;

        // Test 1: Optimistic CAS without retry — conflicts detected
        System.out.println("--- Test 1: CAS without retry (conflicts expected) ---");
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(collaboratorCount);
        AtomicInteger perThreadSuccess = new AtomicInteger(0);
        AtomicInteger perThreadConflict = new AtomicInteger(0);

        for (int c = 0; c < collaboratorCount; c++) {
            final String editorId = "Editor-" + c;
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int e = 0; e < editsPerCollaborator; e++) {
                        boolean ok = editor.editSegment("para-1", editorId, "edit-" + e);
                        if (ok) perThreadSuccess.incrementAndGet();
                        else perThreadConflict.incrementAndGet();
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        doneLatch.await();

        DocumentSegment seg = editor.getSegment("para-1");
        int versionAfterTest1 = seg.getVersion();

        System.out.println("Total attempts: " + totalAttempts);
        System.out.println("Successful edits: " + perThreadSuccess.get());
        System.out.println("Conflicts detected: " + perThreadConflict.get());
        System.out.println("Sum (success+conflict): " + (perThreadSuccess.get() + perThreadConflict.get()));
        System.out.println("Segment version: " + versionAfterTest1);
        System.out.println("Version matches successes: " + (versionAfterTest1 == perThreadSuccess.get()));

        // Test 2: CAS with retry — all edits eventually succeed
        System.out.println("\n--- Test 2: CAS with retry (all edits must succeed) ---");
        CollaborativeEditor editor2 = new CollaborativeEditor();
        editor2.addSegment("para-2", "Initial content");

        CountDownLatch startLatch2 = new CountDownLatch(1);
        CountDownLatch doneLatch2 = new CountDownLatch(collaboratorCount);

        for (int c = 0; c < collaboratorCount; c++) {
            final String editorId = "Editor-" + c;
            new Thread(() -> {
                try {
                    startLatch2.await();
                    for (int e = 0; e < editsPerCollaborator; e++) {
                        editor2.editWithRetry("para-2", editorId, "retry-edit-" + e);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch2.countDown();
                }
            }).start();
        }

        startLatch2.countDown();
        doneLatch2.await();

        DocumentSegment seg2 = editor2.getSegment("para-2");
        int finalVersion = seg2.getVersion();
        int expectedVersion = totalAttempts;

        System.out.println("Total edits attempted: " + totalAttempts);
        System.out.println("Final version: " + finalVersion);
        System.out.println("Expected version: " + expectedVersion);
        System.out.println("Conflicts encountered (retried): " + editor2.getConflictsDetected());
        System.out.println("All edits applied: " + (finalVersion == expectedVersion));

        // Correctness checks
        boolean test1Correct = (perThreadSuccess.get() + perThreadConflict.get() == totalAttempts)
                && (versionAfterTest1 == perThreadSuccess.get());
        boolean test2Correct = (finalVersion == expectedVersion);

        System.out.println("\n--- Summary ---");
        System.out.println("Test 1 (conflict detection): " + (test1Correct ? "PASSED" : "FAILED"));
        System.out.println("Test 2 (no lost edits with retry): " + (test2Correct ? "PASSED" : "FAILED"));
        System.out.println("\nCorrectness check: " + (test1Correct && test2Correct ? "PASSED" : "FAILED"));
    }
}

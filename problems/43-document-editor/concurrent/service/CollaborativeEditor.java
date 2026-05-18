/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/CollaborativeEditor.java — Manages concurrent edits with CAS optimistic concurrency

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CollaborativeEditor {
    private final ConcurrentHashMap<String, DocumentSegment> segments; // ConcurrentHashMap = thread-safe segment registry
    private final AtomicInteger successfulEdits;  // AtomicInteger = lock-free counter across threads
    private final AtomicInteger conflictDetected; // AtomicInteger = tracks CAS failures atomically

    public CollaborativeEditor() {
        this.segments = new ConcurrentHashMap<>();
        this.successfulEdits = new AtomicInteger(0);
        this.conflictDetected = new AtomicInteger(0);
    }

    public void addSegment(String segmentId, String initialContent) {
        segments.put(segmentId, new DocumentSegment(segmentId, initialContent));
    }

    /**
     * Attempt an edit with optimistic concurrency.
     * Reads current version, applies transform, CAS to commit.
     * Returns true if edit was applied, false if conflict.
     */
    public boolean editSegment(String segmentId, String editorId, String newContent) {
        DocumentSegment segment = segments.get(segmentId);
        if (segment == null) return false;

        int currentVersion = segment.getVersion();
        // Simulate read-modify-write with potential conflict window
        String editedContent = "[" + editorId + "] " + newContent;

        boolean success = segment.tryEdit(currentVersion, editedContent);
        if (success) {
            successfulEdits.incrementAndGet();
        } else {
            conflictDetected.incrementAndGet();
        }
        return success;
    }

    /**
     * Retry-based edit: keep trying until succeeds (for mandatory edits).
     */
    public void editWithRetry(String segmentId, String editorId, String newContent) {
        DocumentSegment segment = segments.get(segmentId);
        if (segment == null) return;

        while (true) {
            int currentVersion = segment.getVersion();
            String editedContent = "[" + editorId + " v" + (currentVersion + 1) + "] " + newContent;
            if (segment.tryEdit(currentVersion, editedContent)) {
                successfulEdits.incrementAndGet();
                return;
            }
            conflictDetected.incrementAndGet();
            // Retry with fresh version
        }
    }

    public DocumentSegment getSegment(String segmentId) {
        return segments.get(segmentId);
    }

    public int getSuccessfulEdits() { return successfulEdits.get(); }
    public int getConflictsDetected() { return conflictDetected.get(); }
}

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/ConcurrentDocument.java — AtomicInteger version + CAS-based optimistic locking

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConcurrentDocument {
    private final AtomicInteger version = new AtomicInteger(0);       // AtomicInteger = lock-free version counter for CAS
    private final CopyOnWriteArrayList<String> content = new CopyOnWriteArrayList<>(); // CopyOnWriteArrayList = safe concurrent reads without locking
    private final CopyOnWriteArrayList<EditCommand> history = new CopyOnWriteArrayList<>(); // thread-safe append-only log

    private final AtomicInteger successfulEdits = new AtomicInteger(0);  // AtomicInteger = lock-free success counter
    private final AtomicInteger conflictedEdits = new AtomicInteger(0);  // AtomicInteger = lock-free conflict counter

    /**
     * Apply an edit using optimistic locking.
     * Only succeeds if the document version matches the editor's expected version.
     */
    public boolean applyEdit(EditCommand command) {
        // CAS: only apply if version hasn't changed since editor last read it
        int currentVersion = version.get();
        if (currentVersion != command.getExpectedVersion()) {
            conflictedEdits.incrementAndGet();
            return false; // Conflict — editor must re-read and retry
        }

        // Try to advance version atomically
        if (version.compareAndSet(currentVersion, currentVersion + 1)) { // CAS = atomic compare-and-swap; no locks needed
            content.add(command.getText());
            history.add(command);
            successfulEdits.incrementAndGet();
            return true;
        }

        conflictedEdits.incrementAndGet();
        return false; // CAS failed — concurrent edit
    }

    /**
     * Apply edit with retry loop — keeps retrying until it succeeds.
     */
    public void applyEditWithRetry(String editor, String text) {
        while (true) {
            int ver = version.get();
            EditCommand cmd = new EditCommand(editor, text, ver);
            if (applyEdit(cmd)) return;
            // Re-read version and retry
            Thread.yield();
        }
    }

    public int getVersion() { return version.get(); }
    public int getContentSize() { return content.size(); }
    public int getHistorySize() { return history.size(); }
    public int getSuccessfulEdits() { return successfulEdits.get(); }
    public int getConflictedEdits() { return conflictedEdits.get(); }

    public String getContentAt(int index) {
        return index < content.size() ? content.get(index) : null;
    }
}

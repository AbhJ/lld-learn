/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/DocumentSegment.java — Document segment with AtomicInteger version for CAS-based optimistic concurrency

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class DocumentSegment {
    private final String segmentId;           // final = safe publication; identity never changes
    private final AtomicInteger version;      // AtomicInteger = CAS-based version for optimistic concurrency
    private final AtomicReference<String> content; // AtomicReference = atomic content swap on successful CAS

    public DocumentSegment(String segmentId, String initialContent) {
        this.segmentId = segmentId;
        this.version = new AtomicInteger(0);
        this.content = new AtomicReference<>(initialContent);
    }

    /**
     * CAS-based edit: read version, prepare edit, CAS(oldVersion, newVersion).
     * Returns true if edit applied, false if conflict detected.
     */
    public boolean tryEdit(int expectedVersion, String newContent) {
        if (version.compareAndSet(expectedVersion, expectedVersion + 1)) {
            content.set(newContent);
            return true;
        }
        return false; // Conflict — another editor changed version
    }

    public int getVersion() { return version.get(); }
    public String getContent() { return content.get(); }
    public String getSegmentId() { return segmentId; }

    @Override
    public String toString() {
        return segmentId + " v" + version.get() + ": " + content.get();
    }
}

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Version.java — Captures a snapshot of the document at a point in time
import java.time.LocalDateTime;

public class Version {
    private String versionId;                 // private = version identity encapsulated
    private Document snapshot;                // private = full document state at this point in time
    private String authorId;                  // private = who created this version
    private LocalDateTime timestamp;          // private = when this version was saved
    private String description;               // private = human-readable label for version

    public Version(String versionId, Document snapshot, String authorId, String description) {
        this.versionId = versionId;
        this.snapshot = snapshot;
        this.authorId = authorId;
        this.timestamp = LocalDateTime.now();
        this.description = description;
    }

    public String getVersionId() { return versionId; }
    public Document getSnapshot() { return snapshot; }
    public String getAuthorId() { return authorId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getDescription() { return description; }

    @Override
    public String toString() { return versionId; }
}

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Version.java — Captures a reverse-diff for memory-efficient undo
import java.time.LocalDateTime;

public class Version {
    private String versionId;
    private String snapshotText;
    private String authorId;
    private LocalDateTime timestamp;
    private String description;

    public Version(String versionId, String snapshotText, String authorId, String description) {
        this.versionId = versionId;
        this.snapshotText = snapshotText;
        this.authorId = authorId;
        this.timestamp = LocalDateTime.now();
        this.description = description;
    }

    public String getVersionId() { return versionId; }
    public String getSnapshotText() { return snapshotText; }
    public String getAuthorId() { return authorId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getDescription() { return description; }

    @Override
    public String toString() { return versionId; }
}

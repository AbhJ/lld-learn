/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/EditCommand.java — Immutable edit command with version tracking

public class EditCommand {
    private final String editor;             // final = safe publication; visible to all threads
    private final String text;               // final = immutable once constructed; no synchronization needed
    private final int expectedVersion;       // final = version snapshot for CAS comparison
    private final long timestamp;            // final = creation time for ordering/debugging

    public EditCommand(String editor, String text, int expectedVersion) {
        this.editor = editor;
        this.text = text;
        this.expectedVersion = expectedVersion;
        this.timestamp = System.nanoTime();
    }

    public String getEditor() { return editor; }
    public String getText() { return text; }
    public int getExpectedVersion() { return expectedVersion; }
    public long getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "Edit[" + editor + ": '" + text + "' @v" + expectedVersion + "]";
    }
}

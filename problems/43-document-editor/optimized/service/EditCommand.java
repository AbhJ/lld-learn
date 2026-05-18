/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/EditCommand.java — Encapsulates edits as undoable operations with reverse-diffs
public interface EditCommand {                // interface = pluggable command pattern for undo/redo
    void execute();
    void undo();
    String getDescription();
}

class InsertText implements EditCommand {
    private GapBuffer buffer;
    private int position;
    private String text;

    public InsertText(GapBuffer buffer, int position, String text) {
        this.buffer = buffer; this.position = position; this.text = text;
    }

    @Override public void execute() { buffer.insert(position, text); }
    // WHY: Reverse-diff approach — undo just deletes the exact chars inserted
    @Override public void undo() { buffer.delete(position, text.length()); }
    @Override public String getDescription() { return "Insert '" + text + "' at position " + position; }
}

class DeleteText implements EditCommand {
    private GapBuffer buffer;
    private int position;
    private int length;
    private String deletedText;

    public DeleteText(GapBuffer buffer, int position, int length) {
        this.buffer = buffer; this.position = position; this.length = length;
    }

    @Override public void execute() { deletedText = buffer.delete(position, length); }
    // WHY: Stores only the deleted text (reverse-diff) rather than full document snapshot
    @Override public void undo() { buffer.insert(position, deletedText); }
    @Override public String getDescription() { return "Delete " + length + " chars at position " + position; }
}

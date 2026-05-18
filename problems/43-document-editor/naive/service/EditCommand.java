/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/EditCommand.java — Encapsulates a document edit as an undoable operation
public interface EditCommand {                // interface = contract for undoable edit operations
    void execute();
    void undo();
    String getDescription();
}

class InsertText implements EditCommand {     // implements = fulfills the EditCommand contract
    private Document document;
    private int position;
    private String text;

    public InsertText(Document document, int position, String text) {
        this.document = document; this.position = position; this.text = text;
    }

    @Override public void execute() { document.insertText(position, text); }
    @Override public void undo() { document.deleteText(position, text.length()); }
    @Override public String getDescription() { return "Insert '" + text + "' at position " + position; }
}

class DeleteText implements EditCommand {     // implements = fulfills the EditCommand contract
    private Document document;
    private int position;
    private int length;
    private String deletedText;

    public DeleteText(Document document, int position, int length) {
        this.document = document; this.position = position; this.length = length;
    }

    @Override public void execute() { deletedText = document.deleteText(position, length); }
    @Override public void undo() { document.insertText(position, deletedText); }
    @Override public String getDescription() { return "Delete " + length + " chars at position " + position; }
}

class FormatText implements EditCommand {     // implements = fulfills the EditCommand contract
    private Document document;
    private int start;
    private int end;
    private Formatting.Style style;
    private Document beforeSnapshot;

    public FormatText(Document document, int start, int end, Formatting.Style style) {
        this.document = document; this.start = start; this.end = end; this.style = style;
    }

    @Override public void execute() { beforeSnapshot = document.snapshot(); document.applyFormatting(start, end, style); }
    @Override public void undo() { if (beforeSnapshot != null) document.restoreFrom(beforeSnapshot); }
    @Override public String getDescription() { return "Apply " + style + " to range [" + start + ", " + end + ")"; }
}

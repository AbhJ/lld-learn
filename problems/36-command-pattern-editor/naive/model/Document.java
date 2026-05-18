/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Document.java — Represents the text document being edited
public class Document {
    private StringBuilder content;     // private = only Document methods can modify text

    public Document() { this.content = new StringBuilder(); }
    public Document(String initial) { this.content = new StringBuilder(initial); }

    public void insert(int position, String text) { content.insert(position, text); }
    public String delete(int position, int length) {
        String deleted = content.substring(position, position + length);
        content.delete(position, position + length);
        return deleted;
    }
    public String getText() { return content.toString(); }
    public int length() { return content.length(); }
    public int indexOf(String text) { return content.indexOf(text); }

    // === Memento pattern ===
    // Document (originator) is the only class that can read the memento's
    // private state. Callers pass mementos around opaquely; they cannot peek.

    /** Capture current state into an opaque memento. */
    public DocumentMemento save() {
        return new DocumentMemento(content.toString());
    }

    /** Restore from a previously captured memento. */
    public void restore(DocumentMemento memento) {
        if (memento == null) throw new IllegalArgumentException("memento required");
        this.content = new StringBuilder(memento.getState()); // package-private getState — only Document calls this
    }

    @Override public String toString() { return "\"" + content + "\""; }
}

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Document.java — Text document with Memento-pattern snapshot/restore
//
// Document plays the *Originator* role in the Memento pattern. It hands out
// opaque DocumentMemento instances; only Document itself can read their state.
public class Document {
    private StringBuilder content;     // StringBuilder = mutable text; efficient for edits

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

    /** Capture current state into an opaque memento (Originator -> Memento). */
    public DocumentMemento save() {
        return new DocumentMemento(content.toString());
    }

    /** Restore from a previously captured memento. Only Document reads memento internals. */
    public void restore(DocumentMemento memento) {
        if (memento == null) throw new IllegalArgumentException("memento required");
        this.content = new StringBuilder(memento.getState()); // package-private accessor
    }

    @Override public String toString() { return "\"" + content + "\""; }
}

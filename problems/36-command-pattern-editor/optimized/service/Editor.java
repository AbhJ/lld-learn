/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/Editor.java — Editor with bounded history to prevent memory leaks
public class Editor {
    private final Document document;         // final = document ref set once at creation
    private final BoundedHistory history;    // BoundedHistory = circular buffer caps memory usage

    public Editor(int maxHistory) { this("", maxHistory); }
    public Editor(String text, int maxHistory) {
        this.document = new Document(text);
        this.history = new BoundedHistory(maxHistory, document);
    }

    public void insert(int pos, String text) { history.execute(new InsertCommand(document, pos, text)); }
    public void delete(int pos, int len) { history.execute(new DeleteCommand(document, pos, len)); }
    public void executeMacro(MacroCommand macro) { history.execute(macro); }
    public boolean undo() { return history.undo(); }
    public boolean redo() { return history.redo(); }

    public String getText() { return document.getText(); }
    public Document getDocument() { return document; }
    public BoundedHistory getHistory() { return history; }
    @Override public String toString() { return document.toString(); }
}

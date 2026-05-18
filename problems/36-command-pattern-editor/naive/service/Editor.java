/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/Editor.java — Orchestrates editing via command pattern
public class Editor {
    private final Document document;         // final = document reference never changes
    private final CommandHistory history;     // final = history reference never changes
    private final Clipboard clipboard;       // final = clipboard reference never changes

    public Editor() { this(""); }
    public Editor(String text) {
        this.document = new Document(text);
        this.history = new CommandHistory();
        this.clipboard = new Clipboard();
    }

    public void insert(int pos, String text) { history.execute(new InsertCommand(document, pos, text)); }
    public void delete(int pos, int len) { history.execute(new DeleteCommand(document, pos, len)); }
    public void replace(String old, String nw) { history.execute(new ReplaceCommand(document, old, nw)); }
    public void executeMacro(MacroCommand macro) { history.execute(macro); }
    public boolean undo() { return history.undo(); }
    public boolean redo() { return history.redo(); }

    public String getText() { return document.getText(); }
    public Document getDocument() { return document; }
    public Clipboard getClipboard() { return clipboard; }
    public CommandHistory getHistory() { return history; }
    @Override public String toString() { return document.toString(); }
}

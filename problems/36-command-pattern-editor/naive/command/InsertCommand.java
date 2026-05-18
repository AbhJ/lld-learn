/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// command/InsertCommand.java — Inserts text at a position with undo support
public class InsertCommand implements Command { // implements = fulfills the Command contract
    private final Document document;            // final = target document never reassigned
    private final int position;                 // final = insertion point fixed at creation
    private final String text;                  // final = text to insert is immutable

    public InsertCommand(Document document, int position, String text) {
        this.document = document;
        this.position = position;
        this.text = text;
    }

    @Override public void execute() { document.insert(position, text); }
    @Override public void undo() { document.delete(position, text.length()); }
    @Override public String getDescription() { return "Insert \"" + text + "\" at " + position; }
}

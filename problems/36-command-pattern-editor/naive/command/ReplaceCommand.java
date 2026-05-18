/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// command/ReplaceCommand.java — Replaces first occurrence of text
public class ReplaceCommand implements Command {   // implements = fulfills the Command contract
    private final Document document;               // final = document reference never changes
    private final String oldText;                  // final = search text is immutable
    private final String newText;                  // final = replacement text is immutable
    private int position = -1;                     // private = tracks where replacement occurred

    public ReplaceCommand(Document document, String oldText, String newText) {
        this.document = document;
        this.oldText = oldText;
        this.newText = newText;
    }

    @Override public void execute() {
        position = document.indexOf(oldText);
        if (position >= 0) {
            document.delete(position, oldText.length());
            document.insert(position, newText);
        }
    }
    @Override public void undo() {
        if (position >= 0) {
            document.delete(position, newText.length());
            document.insert(position, oldText);
        }
    }
    @Override public String getDescription() { return "Replace \"" + oldText + "\" -> \"" + newText + "\""; }
}

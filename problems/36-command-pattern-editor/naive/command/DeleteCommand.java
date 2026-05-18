/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// command/DeleteCommand.java — Deletes text range, saves for undo
public class DeleteCommand implements Command { // implements = fulfills the Command contract
    private final Document document;            // final = target document never reassigned
    private final int position;                 // final = deletion start is immutable once set
    private final int length;                   // final = deletion length is immutable once set
    private String deletedText;                 // private = internal state hidden from outside

    public DeleteCommand(Document document, int position, int length) {
        this.document = document;
        this.position = position;
        this.length = length;
    }

    @Override public void execute() { deletedText = document.delete(position, length); } // @Override = proves we implement Command
    @Override public void undo() { document.insert(position, deletedText); }
    @Override public String getDescription() { return "Delete " + length + " at " + position; }
}

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// command/DeleteCommand.java — Deletes text range with undo
public class DeleteCommand implements Command { // implements = fulfills the Command contract
    private final Document document;            // final = reference never reassigned after init
    private final int position;                 // final = deletion start fixed at creation
    private final int length;                   // final = deletion length fixed at creation
    private String deletedText;                 // stores removed text for undo restoration

    public DeleteCommand(Document document, int position, int length) {
        this.document = document;
        this.position = position;
        this.length = length;
    }

    @Override public void execute() { deletedText = document.delete(position, length); }
    @Override public void undo() { document.insert(position, deletedText); }
    @Override public String getDescription() { return "Delete " + length + " at " + position; }
}

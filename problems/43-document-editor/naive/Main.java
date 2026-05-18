/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating the document editor
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Document Editor Demo (Naive) ===");

        DocumentEditor editor = new DocumentEditor("Project Proposal");

        System.out.println("\n--- Text Editing ---");
        editor.insertText(0, "Hello World");
        editor.insertText(11, " - Welcome to the editor");
        System.out.println("Content: " + editor.getDocument().getFullText());

        System.out.println("\n--- Formatting ---");
        editor.formatText(0, 11, Formatting.Style.BOLD);

        System.out.println("\n--- Undo/Redo ---");
        editor.undo();
        System.out.println("After undo: " + editor.getDocument().getFullText());
        editor.redo();
        System.out.println("After redo");

        System.out.println("\n--- Version History ---");
        String v1 = editor.saveVersion("alice", "Initial draft");
        System.out.println("Saved version: " + v1);

        editor.insertText(editor.getDocument().length(), " More content.");
        String v2 = editor.saveVersion("alice", "Added content");
        System.out.println("Saved version: " + v2);

        editor.restoreVersion(v1);
        System.out.println("Restored to " + v1 + ": " + editor.getDocument().getFullText());

        System.out.println("\n--- Collaboration ---");
        Collaborator alice = editor.addCollaborator("alice", "Alice");
        alice.getCursor().moveTo(5);
        editor.insertText(alice.getCursor().getPosition(), " [edit]");
        System.out.println("Content: " + editor.getDocument().getFullText());

        System.out.println("\n=== Document Editor Demo Complete ===");
    }
}

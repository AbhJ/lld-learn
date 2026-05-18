/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating the optimized document editor
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Document Editor Demo (Optimized - GapBuffer) ===");

        DocumentEditor editor = new DocumentEditor("Project Proposal");

        System.out.println("\n--- Text Editing (O(1) inserts via GapBuffer) ---");
        editor.insertText(0, "Hello World");
        editor.insertText(11, " - Welcome to the editor");
        System.out.println("Content: " + editor.getFullText());

        System.out.println("\n--- Undo/Redo (reverse-diff, no full snapshots) ---");
        editor.undo();
        System.out.println("After undo: " + editor.getFullText());
        editor.redo();
        System.out.println("After redo: " + editor.getFullText());

        System.out.println("\n--- Version History ---");
        String v1 = editor.saveVersion("alice", "Initial draft");
        System.out.println("Saved version: " + v1);

        editor.insertText(editor.length(), " More content.");
        String v2 = editor.saveVersion("alice", "Added content");
        System.out.println("Saved version: " + v2);

        editor.restoreVersion(v1);
        System.out.println("Restored to " + v1 + ": " + editor.getFullText());

        System.out.println("\n--- Collaboration ---");
        Collaborator alice = editor.addCollaborator("alice", "Alice");
        alice.getCursor().moveTo(5);
        editor.insertText(alice.getCursor().getPosition(), " [edit]");
        System.out.println("Content: " + editor.getFullText());

        System.out.println("\n--- Delete Operations ---");
        editor.deleteText(5, 7);
        System.out.println("After delete: " + editor.getFullText());
        editor.undo();
        System.out.println("After undo: " + editor.getFullText());

        System.out.println("\n=== Document Editor Demo Complete ===");
    }
}

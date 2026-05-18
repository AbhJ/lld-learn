/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Demonstrates editor with unlimited undo stack
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Command Editor (Naive) Demo ===\n");

        Editor editor = new Editor();
        editor.insert(0, "Hello");
        editor.insert(5, " World!");
        System.out.println("  After inserts: " + editor);

        editor.delete(5, 6);
        System.out.println("  After delete: " + editor);

        editor.undo();
        System.out.println("  After undo: " + editor);
        editor.redo();
        System.out.println("  After redo: " + editor);

        // Macro
        Editor e2 = new Editor("Hello World");
        MacroCommand macro = new MacroCommand("swap");
        macro.addCommand(new DeleteCommand(e2.getDocument(), 6, 5));
        macro.addCommand(new InsertCommand(e2.getDocument(), 6, "Java"));
        e2.executeMacro(macro);
        System.out.println("  After macro: " + e2);
        e2.undo();
        System.out.println("  After undo macro: " + e2);

        System.out.println("  Undo depth: " + e2.getHistory().getUndoCount());
        System.out.println("\n=== Command Editor (Naive) Demo Complete ===");
    }
}

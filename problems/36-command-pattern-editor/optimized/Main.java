/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Demonstrates bounded circular-buffer history and composite macros
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Command Editor (Optimized: Bounded History) Demo ===\n");

        // --- Test 1: Basic Editing with Bounded History (max 5) ---
        System.out.println("--- Test 1: Bounded History (max=5) ---");
        Editor editor = new Editor(5);
        editor.insert(0, "A");
        editor.insert(1, "B");
        editor.insert(2, "C");
        editor.insert(3, "D");
        editor.insert(4, "E");
        System.out.println("  After 5 inserts: " + editor);
        System.out.println("  History size: " + editor.getHistory().getCurrentSize() + "/" + editor.getHistory().getMaxSize());

        // --- Test 2: Overflow (6th command evicts oldest) ---
        System.out.println("\n--- Test 2: History Overflow ---");
        editor.insert(5, "F");
        editor.insert(6, "G");
        System.out.println("  After 7 total inserts: " + editor);
        System.out.println("  History size capped at: " + editor.getHistory().getCurrentSize());

        // Undo all available (only 5 most recent)
        int undone = 0;
        while (editor.undo()) undone++;
        System.out.println("  Undone " + undone + " commands (max 5 available)");
        System.out.println("  Document: " + editor);

        // --- Test 3: Redo ---
        System.out.println("\n--- Test 3: Redo ---");
        editor.redo();
        editor.redo();
        System.out.println("  After 2 redos: " + editor);

        // --- Test 4: Macro as Single Undo Step ---
        System.out.println("\n--- Test 4: Macro (composite) ---");
        Editor e2 = new Editor("Hello World", 10);
        MacroCommand macro = new MacroCommand("transform");
        macro.addCommand(new DeleteCommand(e2.getDocument(), 6, 5));
        macro.addCommand(new InsertCommand(e2.getDocument(), 6, "Java"));
        macro.addCommand(new InsertCommand(e2.getDocument(), 10, "!"));
        e2.executeMacro(macro);
        System.out.println("  After macro (3 ops, 1 undo step): " + e2);
        e2.undo(); // Undoes entire macro
        System.out.println("  After single undo: " + e2);

        // --- Test 5: Memory Bounded ---
        System.out.println("\n--- Test 5: Memory Stays Bounded ---");
        Editor bigEditor = new Editor(10); // Max 10 history entries
        for (int i = 0; i < 1000; i++) {
            bigEditor.insert(i, String.valueOf((char)('A' + i % 26)));
        }
        System.out.println("  1000 edits done, history still: " + bigEditor.getHistory().getCurrentSize());
        System.out.println("  Last: " + bigEditor.getHistory().getLastDescription());

        System.out.println("\n=== Command Editor (Optimized) Demo Complete ===");
    }
}

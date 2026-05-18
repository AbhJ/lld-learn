/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/BoundedHistory.java — Circular buffer for bounded undo history (prevents memory leak)
public class BoundedHistory {
    // WHY circular buffer: Naive Stack grows unbounded — a 10-hour editing session
    // accumulates thousands of commands. Circular buffer caps memory at O(maxSize)
    // and naturally evicts the oldest operations.
    private final Command[] buffer;             // Command[] = fixed-size circular buffer for O(1) ops
    private final DocumentMemento[] snapshots;  // Memento pattern: opaque snapshots; this class is the *caretaker*
    private int head = 0;     // write position
    private int size = 0;     // current number of valid entries
    private int undoPos = 0;  // current position for undo/redo
    private final Document document;         // final = document ref never changes

    public BoundedHistory(int maxSize, Document document) {
        this.buffer = new Command[maxSize];
        this.snapshots = new DocumentMemento[maxSize];
        this.document = document;
    }

    public void execute(Command command) {
        command.execute();

        // WHY snapshot on boundary: When oldest entry is about to be overwritten,
        // save a snapshot so we can still "undo to beginning" of visible history.
        // BoundedHistory is the *caretaker* in the Memento pattern: it stores the
        // memento opaquely and never inspects its contents.
        if (size == buffer.length) {
            // About to overwrite oldest — take snapshot at eviction point
            snapshots[head] = null; // Clear old snapshot
        }
        if (size == 0 || (size == buffer.length && snapshots[(head) % buffer.length] == null)) {
            snapshots[head] = document.save();
        }

        buffer[head] = command;
        head = (head + 1) % buffer.length;
        if (size < buffer.length) size++;
        undoPos = 0; // Reset redo capability
    }

    public boolean undo() {
        if (undoPos >= size) return false;
        undoPos++;
        int idx = Math.floorMod(head - undoPos, buffer.length);
        Command cmd = buffer[idx];
        if (cmd == null) return false;
        cmd.undo();
        return true;
    }

    public boolean redo() {
        if (undoPos <= 0) return false;
        undoPos--;
        int idx = Math.floorMod(head - undoPos - 1, buffer.length);
        Command cmd = buffer[idx];
        if (cmd == null) return false;
        cmd.execute();
        return true;
    }

    public int getUndoDepth() { return size - undoPos; }
    public int getMaxSize() { return buffer.length; }
    public int getCurrentSize() { return size; }

    public String getLastDescription() {
        if (size == 0) return "none";
        int idx = Math.floorMod(head - 1, buffer.length);
        return buffer[idx] == null ? "none" : buffer[idx].getDescription();
    }
}

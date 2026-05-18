/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/CommandHistory.java — Unlimited undo/redo stacks (potential memory leak)
import java.util.Stack;

public class CommandHistory {
    private final Stack<Command> undoStack = new Stack<>();  // private final = stack ref never changes
    private final Stack<Command> redoStack = new Stack<>();  // private final = stack ref never changes

    public void execute(Command command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear();
    }

    public boolean undo() {
        if (undoStack.isEmpty()) return false;
        Command cmd = undoStack.pop();
        cmd.undo();
        redoStack.push(cmd);
        return true;
    }

    public boolean redo() {
        if (redoStack.isEmpty()) return false;
        Command cmd = redoStack.pop();
        cmd.execute();
        undoStack.push(cmd);
        return true;
    }

    public int getUndoCount() { return undoStack.size(); }
    public int getRedoCount() { return redoStack.size(); }
}

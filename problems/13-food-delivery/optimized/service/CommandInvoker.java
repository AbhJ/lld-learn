/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/CommandInvoker.java — Runs OrderCommands and keeps a LIFO history for undo

import java.util.Stack;

class CommandInvoker {
    private final Stack<OrderCommand> history = new Stack<>();   // LIFO: latest command undone first

    /** Run a command and push it onto history if it succeeds. */
    public boolean run(OrderCommand command) {
        boolean ok = command.execute();
        System.out.println("  [cmd] " + command.name() + " -> " + (ok ? "OK" : "FAILED"));
        if (ok) history.push(command);
        return ok;
    }

    /** Undo the most recent successful command. */
    public boolean undoLast() {
        if (history.isEmpty()) return false;
        OrderCommand last = history.pop();
        boolean ok = last.undo();
        System.out.println("  [cmd] undo " + last.name() + " -> " + (ok ? "OK" : "FAILED"));
        return ok;
    }

    public int historySize() { return history.size(); }
}

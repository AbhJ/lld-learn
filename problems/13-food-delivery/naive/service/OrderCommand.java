/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/OrderCommand.java — Command contract for order operations with undo support
//
// Each concrete command captures all the state it needs in its constructor
// (system + target order/inputs) and operates on that state in execute().
// undo() reverses the effect when possible (e.g. restore prior order state).

interface OrderCommand {
    /** Run the command. Returns true on success. */
    boolean execute();

    /** Reverse the command's effect. Returns true on success. */
    boolean undo();

    /** Short label used by the invoker for logging. */
    String name();
}

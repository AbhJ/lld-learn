/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// command/Command.java — Interface for all editor operations (supports undo/redo)
// DESIGN PATTERN: Command
public interface Command {              // interface = contract all commands must fulfill
    void execute();                     // any command must know how to run itself
    void undo();                        // any command must know how to reverse itself
    String getDescription();
}

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// command/MacroCommand.java — Composite macro that counts as single undo step
import java.util.ArrayList;
import java.util.List;

public class MacroCommand implements Command {           // implements = fulfills Command contract
    private final String name;                           // final = macro name set once
    private final List<Command> commands = new ArrayList<>(); // List = ordered sub-commands

    public MacroCommand(String name) { this.name = name; }
    public void addCommand(Command cmd) { commands.add(cmd); }

    @Override public void execute() { for (Command c : commands) c.execute(); }
    @Override public void undo() { for (int i = commands.size()-1; i >= 0; i--) commands.get(i).undo(); }
    @Override public String getDescription() { return "Macro \"" + name + "\" (" + commands.size() + " cmds)"; }
    public int getCommandCount() { return commands.size(); }
}

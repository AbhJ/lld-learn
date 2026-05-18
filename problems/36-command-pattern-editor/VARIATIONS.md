# Command Pattern Editor - Variations

## Variation 1: Collaborative Undo
**Learning Value:** Teaches multi-user undo coordination, conflict detection, and operational ownership in shared editing.

### Additional Requirements
- Multiple users editing simultaneously
- Each user has their own undo stack
- Operational transform to resolve conflicts
- Undo should only reverse the user's own operations

### Design Changes
- Add OperationalTransform engine for conflict resolution
- Add UserSession with per-user command history
- Add TransformableCommand interface
- Add ServerState that maintains canonical document state

### Solution Approach
Each user's operations are tagged with a user ID and a logical clock. When User A undoes their last operation, it must be transformed against any operations that User B performed after it. Use Operational Transform (OT): when undoing command C that was followed by commands D and E from other users, compute the inverse of C transformed against D and E. The server maintains the canonical operation log. Alternatively, use CRDTs (Conflict-free Replicated Data Types) for text, where undo is inserting a tombstone character. Each character has a unique ID, so concurrent operations on different characters never conflict.

### Key Classes to Add
```java
public class CollaborativeEditor {
    private final Map<String, UserSession> sessions;
    private final OperationLog serverLog;
    private final OTEngine otEngine;
    
    public void applyOperation(String userId, Command cmd) {
        int clientVersion = cmd.getBaseVersion();
        List<Command> concurrent = serverLog.getAfter(clientVersion);
        Command transformed = otEngine.transform(cmd, concurrent);
        serverLog.append(transformed);
        broadcastToOthers(userId, transformed);
    }
    
    public void undo(String userId) {
        UserSession session = sessions.get(userId);
        Command toUndo = session.getLastCommand();
        Command inverse = toUndo.inverse();
        // Transform inverse against all operations after toUndo
        Command transformedInverse = otEngine.transformAgainstSubsequent(inverse, toUndo);
        applyOperation(userId, transformedInverse);
    }
}

public interface TransformableCommand extends Command {
    Command transform(Command concurrent);
    Command inverse();
}
```

---

## Variation 2: Selective Undo
**Learning Value:** Introduces non-linear undo, dependency analysis, and partial history reversal.

### Additional Requirements
- Undo a specific command from history without undoing later commands
- Detect conflicts (can this command be undone independently?)
- Handle dependent commands (command B depends on command A)
- Show available undo targets to the user

### Design Changes
- Add DependencyTracker to identify command dependencies
- Add ConflictDetector to check if selective undo is safe
- Add SelectiveUndoEngine that computes the compensation
- Modify History to support non-sequential undo

### Solution Approach
Maintain a dependency graph between commands: command B depends on command A if B operated on content that A created or modified. When the user selects a specific command to undo, check the dependency graph: if later commands depend on it, either block the undo or compute a cascade (undo all dependents too). If independent, compute the inverse of the selected command, transform it against all subsequent commands (similar to OT), and apply it as a new command. The history now shows the selective undo as a new entry rather than removing the original.

### Key Classes to Add
```java
public class SelectiveUndoEngine {
    private final CommandHistory history;
    private final DependencyTracker dependencies;
    
    public boolean canSelectivelyUndo(Command target) {
        List<Command> dependents = dependencies.getDependents(target);
        return dependents.isEmpty(); // or offer cascade
    }
    
    public Command computeSelectiveUndo(Command target) {
        int targetIndex = history.indexOf(target);
        Command inverse = target.inverse();
        List<Command> subsequent = history.getCommandsAfter(targetIndex);
        
        for (Command later : subsequent) {
            inverse = transform(inverse, later);
        }
        return inverse;
    }
}

public class DependencyTracker {
    private final Map<Command, Set<Command>> dependencyGraph;
    
    public void recordDependency(Command dependent, Command dependency) {
        dependencyGraph.computeIfAbsent(dependency, k -> new HashSet<>()).add(dependent);
    }
    
    public Set<Command> getDependents(Command cmd) {
        return dependencyGraph.getOrDefault(cmd, Collections.emptySet());
    }
}
```

---

## Variation 3: Branching Undo Tree
**Learning Value:** Practices tree-based history management, branch navigation, and alternative timeline exploration.

### Additional Requirements
- Non-linear undo history (tree instead of stack)
- Navigate between branches (like git branches)
- Visualize the undo tree
- Never lose any state (all paths preserved)

### Design Changes
- Replace linear undo/redo stacks with UndoTree structure
- Add TreeNode with parent and children
- Add BranchNavigator to move between branches
- Add TreeVisualizer for displaying the tree

### Solution Approach
Instead of a linear stack where new edits after an undo discard the redo stack, maintain a tree. Each node represents a document state. Undo moves to the parent node. A new edit after an undo creates a new child branch rather than discarding the old branch. The user can navigate to any node in the tree to restore that state. Track the "current" pointer. Time-ordered traversal visits nodes by timestamp regardless of branch (useful for "undo by time"). This ensures no state is ever lost, unlike traditional linear undo.

### Key Classes to Add
```java
public class UndoTree {
    private UndoNode root;
    private UndoNode current;
    
    class UndoNode {
        Command command;
        UndoNode parent;
        List<UndoNode> children; // branches
        long timestamp;
        int branchIndex; // which child is the "active" branch
    }
    
    public void execute(Command cmd) {
        UndoNode node = new UndoNode(cmd, current, System.currentTimeMillis());
        current.children.add(node);
        current = node;
        cmd.execute();
    }
    
    public void undo() {
        if (current.parent == null) return;
        current.command.undo();
        current = current.parent;
    }
    
    public void redo() {
        if (current.children.isEmpty()) return;
        UndoNode next = current.children.get(current.branchIndex);
        next.command.execute();
        current = next;
    }
    
    public void switchBranch(int branchIndex) {
        // undo to common ancestor, then redo down new branch
    }
}
```

---

## Variation 4: Macro Recording with Loops
**Learning Value:** Explores trade-offs between expressiveness and complexity in recorded automation with control flow.

### Additional Requirements
- Record a sequence of commands as a macro
- Edit recorded macros (remove/reorder steps)
- Add conditions and loops to macros
- Parameterize macros for reuse

### Design Changes
- Add MacroRecorder to capture commands during recording
- Add MacroEditor for modifying recorded macros
- Add ConditionalCommand and LoopCommand
- Add MacroParameter for parameterized macros

### Solution Approach
MacroRecorder intercepts all commands during recording mode, storing them in a list. The macro can be replayed as-is, or edited: remove steps, reorder them, or wrap groups in a LoopCommand (repeat N times or until condition). ConditionalCommand checks a predicate before executing. Parameters allow macros to be generic (e.g., "find and replace X with Y" where X and Y are parameters filled at execution time). The macro itself is a CompositeCommand, so it integrates with undo (undoing a macro execution undoes all its steps atomically).

### Key Classes to Add
```java
public class MacroRecorder {
    private final List<Command> recordedCommands = new ArrayList<>();
    private boolean recording;
    
    public void startRecording() { recording = true; recordedCommands.clear(); }
    public void stopRecording() { recording = false; }
    
    public void intercept(Command cmd) {
        if (recording) recordedCommands.add(cmd);
    }
    
    public EditableMacro toMacro(String name) {
        return new EditableMacro(name, new ArrayList<>(recordedCommands));
    }
}

public class LoopCommand implements Command {
    private final Command body;
    private final int iterations; // or Predicate<EditorState> condition;
    
    public void execute() {
        for (int i = 0; i < iterations; i++) {
            body.execute();
        }
    }
}

public class ConditionalCommand implements Command {
    private final Predicate<EditorState> condition;
    private final Command thenCmd;
    private final Command elseCmd;
    
    public void execute() {
        if (condition.test(EditorState.current())) thenCmd.execute();
        else if (elseCmd != null) elseCmd.execute();
    }
}
```

---

## Variation 5: Plugin Commands
**Learning Value:** Deepens understanding of plugin architecture, command registration, and extensible operation systems.

### Additional Requirements
- Third-party plugins can register new command types
- Command registry for discovery and invocation
- Extension API with lifecycle hooks
- Permission model for plugin commands

### Design Changes
- Add CommandRegistry for dynamic command registration
- Add PluginAPI interface that plugins implement
- Add PluginManager for loading/unloading plugins
- Add PermissionModel to restrict plugin capabilities

### Solution Approach
Define a CommandRegistry where plugins register their command types with metadata (name, category, keybinding, icon). Plugins implement a PluginAPI interface and are loaded dynamically (via ServiceLoader or a plugin classloader). Each plugin declares required permissions (file access, network, etc.). The editor invokes plugin commands through the same Command interface, so undo/redo works naturally. Plugin commands participate in macro recording. The registry provides discovery (list all commands, search by name/category) and the editor UI can dynamically build menus from registered commands.

### Key Classes to Add
```java
public class CommandRegistry {
    private final Map<String, CommandDescriptor> commands = new ConcurrentHashMap<>();
    
    public void register(String id, CommandDescriptor descriptor) {
        commands.put(id, descriptor);
    }
    
    public Command createCommand(String id, Map<String, Object> params) {
        CommandDescriptor desc = commands.get(id);
        return desc.getFactory().create(params);
    }
    
    public List<CommandDescriptor> search(String query) {
        return commands.values().stream()
            .filter(d -> d.matches(query))
            .toList();
    }
}

public interface PluginAPI {
    String getId();
    void onLoad(PluginContext context);
    void onUnload();
    List<CommandDescriptor> getCommands();
    Set<Permission> getRequiredPermissions();
}

public class PluginManager {
    private final Map<String, PluginAPI> plugins = new HashMap<>();
    private final CommandRegistry registry;
    
    public void loadPlugin(String jarPath) {
        PluginAPI plugin = loadFromJar(jarPath);
        if (checkPermissions(plugin)) {
            plugin.onLoad(createContext(plugin));
            plugin.getCommands().forEach(cmd -> registry.register(cmd.getId(), cmd));
            plugins.put(plugin.getId(), plugin);
        }
    }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

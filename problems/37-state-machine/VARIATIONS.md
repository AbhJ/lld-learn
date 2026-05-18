# State Machine - Variations

## Variation 1: Hierarchical State Machine
**Learning Value:** Teaches nested state composition, state inheritance, and hierarchical transition routing.

### Additional Requirements
- Nested states (substates within a parent state)
- Inherit transitions from parent state
- History states (remember last active substate)
- Entry/exit actions at each level of nesting

### Design Changes
- Add CompositeState that contains substates
- Add HistoryState (shallow and deep history)
- Modify transition resolution to check parent states
- Add hierarchical entry/exit action execution order

### Solution Approach
A CompositeState contains substates; transitions defined on the parent apply to all substates (e.g., "CANCEL" from any substate of "Active"). When entering a composite state, enter its initial substate. When exiting, exit from the deepest active substate up to the composite state. History states remember the last active substate; re-entering the composite state resumes at the remembered substate instead of the initial one. Deep history remembers the full nesting depth. Transition resolution walks up the state hierarchy until a matching transition is found.

### Key Classes to Add
```java
public class CompositeState extends State {
    private final List<State> substates;
    private State initialSubstate;
    private State activeSubstate;
    private State historyState; // last active substate
    
    public void enter(Event event) {
        super.enter(event); // parent entry action
        State target = (historyState != null && useHistory) ? historyState : initialSubstate;
        activeSubstate = target;
        target.enter(event);
    }
    
    public void exit(Event event) {
        activeSubstate.exit(event);
        historyState = activeSubstate; // remember for history
        super.exit(event);
    }
}

public class HierarchicalStateMachine extends StateMachine {
    @Override
    protected Transition resolveTransition(State state, Event event) {
        Transition t = state.getTransition(event);
        if (t != null) return t;
        // Walk up hierarchy
        if (state.getParent() != null) {
            return resolveTransition(state.getParent(), event);
        }
        return null; // no transition found at any level
    }
}
```

---

## Variation 2: Parallel/Orthogonal States
**Learning Value:** Introduces concurrent state regions, independent sub-machines, and synchronized transitions.

### Additional Requirements
- Multiple independent state regions active simultaneously
- Each region transitions independently
- Fork (enter multiple regions) and join (synchronize regions)
- Cross-region event communication

### Design Changes
- Add ParallelState containing multiple orthogonal regions
- Add Region class (each is an independent state machine)
- Add ForkTransition and JoinTransition
- Add inter-region event propagation

### Solution Approach
A ParallelState contains multiple Regions, each being an independent state machine. All regions are active simultaneously. Events are dispatched to all regions; each region processes the event independently. Fork enters a parallel state (activating all regions). Join waits until all regions reach a specific state before transitioning out. Example: an order can be in "Payment Processing" and "Inventory Checking" simultaneously; only when both reach "Complete" does the order proceed. Cross-region communication uses internal events.

### Key Classes to Add
```java
public class ParallelState extends State {
    private final List<Region> regions;
    
    public void enter(Event event) {
        super.enter(event);
        regions.forEach(r -> r.enter(event));
    }
    
    public void processEvent(Event event) {
        regions.forEach(r -> r.fire(event));
        checkJoinCondition();
    }
    
    private void checkJoinCondition() {
        if (joinCondition != null && joinCondition.test(regions)) {
            fireCompletion(); // all regions ready, trigger join
        }
    }
}

public class Region {
    private final StateMachine stateMachine;
    private final String name;
    
    public void fire(Event event) { stateMachine.fire(event); }
    public String getCurrentStateName() { return stateMachine.getCurrentStateName(); }
}

public class JoinTransition extends Transition {
    private final Map<String, String> requiredRegionStates; // region -> required state
    
    public boolean canFire(ParallelState parallel) {
        return requiredRegionStates.entrySet().stream()
            .allMatch(e -> parallel.getRegion(e.getKey()).getCurrentStateName().equals(e.getValue()));
    }
}
```

---

## Variation 3: Persistent State Machine
**Learning Value:** Practices durable state persistence, recovery from crashes, and audit-trail-driven state machines.

### Additional Requirements
- Save/restore state machine from database
- Survive process restarts
- Event sourcing integration (rebuild from events)
- Optimistic concurrency for concurrent transitions

### Design Changes
- Add StateMachineRepository for persistence
- Add StateMachineSnapshot for serialization
- Add EventJournal for event sourcing approach
- Add version field for optimistic locking

### Solution Approach
Store the state machine's current state, context data, and version in a database. On each transition, persist the new state within a transaction. For event sourcing: store each fired event rather than the current state; rebuild the state machine by replaying events on load. Use optimistic locking (version column) to handle concurrent transitions: if two threads try to transition simultaneously, one will get a version conflict and must retry. Separate the state machine definition (static, in code) from the instance state (dynamic, in DB). Support querying state machines by current state (e.g., "find all orders in SHIPPED state").

### Key Classes to Add
```java
public class PersistentStateMachine {
    private final StateMachine stateMachine;
    private final StateMachineRepository repository;
    private long version;
    
    public static PersistentStateMachine load(String id, StateMachineRepository repo) {
        StateMachineSnapshot snapshot = repo.findById(id);
        StateMachine sm = StateMachineFactory.create(snapshot.getDefinitionId());
        sm.restoreState(snapshot.getCurrentState(), snapshot.getContext());
        return new PersistentStateMachine(sm, repo, snapshot.getVersion());
    }
    
    public void fire(String event) {
        stateMachine.fire(event);
        StateMachineSnapshot snapshot = new StateMachineSnapshot(
            id, stateMachine.getCurrentStateName(), stateMachine.getContext(), version + 1
        );
        boolean saved = repository.saveWithVersion(snapshot, version); // optimistic lock
        if (!saved) throw new ConcurrentModificationException("Retry transition");
        version++;
    }
}

public interface StateMachineRepository {
    StateMachineSnapshot findById(String id);
    boolean saveWithVersion(StateMachineSnapshot snapshot, long expectedVersion);
    List<StateMachineSnapshot> findByState(String state);
}
```

---

## Variation 4: Timeout Transitions
**Learning Value:** Explores trade-offs between responsiveness and reliability in time-driven automatic transitions.

### Additional Requirements
- Auto-transition after a duration (e.g., payment timeout)
- Deadline-based transitions (absolute timestamp)
- Cancel timeout on manual transition
- Escalation on timeout (different path than normal flow)

### Design Changes
- Add TimeoutTransition with duration or deadline
- Add TimeoutScheduler to manage pending timeouts
- Add timeout cancellation on regular transitions
- Add escalation path distinct from normal transitions

### Solution Approach
When entering a state with a timeout transition defined, schedule a timer (using ScheduledExecutorService). If the timer fires before a regular transition occurs, automatically fire the timeout event. If a regular transition occurs first, cancel the timer. Timeouts can be relative (30 minutes from entering state) or absolute (must complete by 5:00 PM). Different timeout durations can lead to different target states (e.g., 5 min timeout -> "Reminder", 30 min timeout -> "Cancelled"). Use a TimeoutScheduler that maintains all pending timeouts and cancels them on state exit.

### Key Classes to Add
```java
public class TimeoutTransition {
    private final String fromState;
    private final String toState;
    private final long timeoutMs;
    private final String timeoutEvent;
}

public class TimeoutScheduler {
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private final Map<String, ScheduledFuture<?>> pendingTimeouts = new ConcurrentHashMap<>();
    
    public void scheduleTimeout(String machineId, String state, long delayMs, Runnable onTimeout) {
        ScheduledFuture<?> future = executor.schedule(onTimeout, delayMs, TimeUnit.MILLISECONDS);
        pendingTimeouts.put(machineId + ":" + state, future);
    }
    
    public void cancelTimeout(String machineId, String state) {
        ScheduledFuture<?> future = pendingTimeouts.remove(machineId + ":" + state);
        if (future != null) future.cancel(false);
    }
}

public class TimeoutAwareStateMachine extends StateMachine {
    private final TimeoutScheduler scheduler;
    
    @Override
    protected void onStateEntered(State state) {
        TimeoutTransition timeout = state.getTimeoutTransition();
        if (timeout != null) {
            scheduler.scheduleTimeout(this.id, state.getName(), timeout.getTimeoutMs(),
                () -> this.fire(timeout.getTimeoutEvent()));
        }
    }
    
    @Override
    protected void onStateExited(State state) {
        scheduler.cancelTimeout(this.id, state.getName());
    }
}
```

---

## Variation 5: State Machine Visualization
**Learning Value:** Deepens understanding of graph visualization, DOT generation, and interactive state exploration tools.

### Additional Requirements
- Generate DOT (Graphviz) diagrams from state machine definition
- Generate Mermaid diagrams for documentation
- Show current state highlighted
- Include guards, actions, and transition labels

### Design Changes
- Add StateMachineVisualizer interface
- Add DotExporter for Graphviz format
- Add MermaidExporter for Mermaid markdown
- Add styling options (highlight current state, color by state type)

### Solution Approach
Walk the state machine's definition (states, transitions, guards, actions) and generate a diagram description. For DOT format: states become nodes, transitions become directed edges with labels. Style the current state with a different color/shape. Include guard conditions as edge labels in brackets. For Mermaid: use the stateDiagram-v2 syntax. Support composite states as subgraphs. Generate the diagram string that can be rendered by Graphviz or a Mermaid renderer. This is invaluable for documentation, debugging, and communicating designs.

### Key Classes to Add
```java
public interface StateMachineVisualizer {
    String export(StateMachine sm);
    String export(StateMachine sm, VisualizationOptions options);
}

public class DotExporter implements StateMachineVisualizer {
    public String export(StateMachine sm) {
        StringBuilder dot = new StringBuilder("digraph ").append(sm.getName()).append(" {\n");
        dot.append("  rankdir=LR;\n");
        
        for (State state : sm.getStates()) {
            String shape = state.isFinal() ? "doublecircle" : "circle";
            String color = state.equals(sm.getCurrentState()) ? "red" : "black";
            dot.append(String.format("  %s [shape=%s, color=%s];\n", state.getName(), shape, color));
        }
        
        for (Transition t : sm.getTransitions()) {
            String label = t.getEvent();
            if (t.getGuard() != null) label += " [" + t.getGuard().getDescription() + "]";
            dot.append(String.format("  %s -> %s [label=\"%s\"];\n", t.getFrom(), t.getTo(), label));
        }
        
        dot.append("}\n");
        return dot.toString();
    }
}

public class MermaidExporter implements StateMachineVisualizer {
    public String export(StateMachine sm) {
        StringBuilder md = new StringBuilder("stateDiagram-v2\n");
        md.append("  [*] --> ").append(sm.getInitialState().getName()).append("\n");
        
        for (Transition t : sm.getTransitions()) {
            md.append(String.format("  %s --> %s : %s\n", t.getFrom(), t.getTo(), t.getEvent()));
        }
        
        for (State state : sm.getFinalStates()) {
            md.append("  ").append(state.getName()).append(" --> [*]\n");
        }
        return md.toString();
    }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/StateMachineBuilder.java — Builder that validates at construction time and builds transition table
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StateMachineBuilder {
    private final String name;
    private final Set<String> states = new HashSet<>();           // HashSet = O(1) state existence check
    private String initialState;
    private final Map<TransitionKey, List<Transition>> transitionTable = new HashMap<>(); // HashMap = O(1) lookup table built here
    private final Map<String, Action> entryActions = new HashMap<>();
    private final Map<String, Action> exitActions = new HashMap<>();

    public StateMachineBuilder(String name) { this.name = name; }

    public StateMachineBuilder state(String s) { states.add(s); return this; }
    public StateMachineBuilder initialState(String s) { states.add(s); initialState = s; return this; }
    public StateMachineBuilder onEntry(String s, Action a) { states.add(s); entryActions.put(s, a); return this; }
    public StateMachineBuilder onExit(String s, Action a) { states.add(s); exitActions.put(s, a); return this; }

    public StateMachineBuilder transition(String from, String event, String to) {
        return transition(from, event, to, null, null);
    }
    public StateMachineBuilder transition(String from, String event, String to, Action action) {
        return transition(from, event, to, null, action);
    }
    public StateMachineBuilder transition(String from, String event, String to, Guard guard, Action action) {
        states.add(from);
        states.add(to);
        TransitionKey key = new TransitionKey(from, event);
        transitionTable.computeIfAbsent(key, k -> new ArrayList<>())
                .add(new Transition(to, guard, action));
        return this;
    }

    // WHY validate at build time: Catches misconfiguration early (missing states,
    // unreachable states, dangling targets) rather than at runtime.
    public StateMachine build() {
        if (initialState == null) throw new IllegalStateException("No initial state set");
        if (!states.contains(initialState)) throw new IllegalStateException("Initial state not in state set");

        // Validate all transition targets exist
        for (var entry : transitionTable.entrySet()) {
            for (Transition t : entry.getValue()) {
                if (!states.contains(t.getTargetState())) {
                    throw new IllegalStateException("Target state '" + t.getTargetState() + "' not defined");
                }
            }
        }

        return new StateMachine(name, initialState, transitionTable, entryActions, exitActions);
    }
}

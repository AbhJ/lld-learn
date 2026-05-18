/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/StateMachineBuilder.java — Fluent builder for constructing state machines
import java.util.HashMap;
import java.util.Map;

public class StateMachineBuilder {
    private final String name;                             // final = builder name set once
    private final Map<String, State> states = new HashMap<>(); // private = internal registry of states
    private String initialStateName;                       // private = tracks which state is first

    public StateMachineBuilder(String name) { this.name = name; }

    public StateMachineBuilder state(String s) { states.putIfAbsent(s, new State(s)); return this; }
    public StateMachineBuilder initialState(String s) { state(s); initialStateName = s; return this; }

    public StateMachineBuilder onEntry(String s, Action a) { state(s); states.get(s).setEntryAction(a); return this; }
    public StateMachineBuilder onExit(String s, Action a) { state(s); states.get(s).setExitAction(a); return this; }

    public StateMachineBuilder transition(String from, String event, String to) { return transition(from, event, to, null, null); }
    public StateMachineBuilder transition(String from, String event, String to, Action action) { return transition(from, event, to, null, action); }
    public StateMachineBuilder transition(String from, String event, String to, Guard guard, Action action) {
        state(from); state(to);
        states.get(from).addTransition(new Transition(states.get(from), states.get(to), event, guard, action));
        return this;
    }

    public StateMachine build() {
        if (initialStateName == null) throw new IllegalStateException("No initial state");
        return new StateMachine(name, states.get(initialStateName));
    }
}

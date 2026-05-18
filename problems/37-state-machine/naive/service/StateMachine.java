/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/StateMachine.java — Executes transitions via if-else linear search in State
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StateMachine {
    private final String name;                             // final = machine name set once
    private State currentState;                            // private = only machine controls state changes
    private final List<String> history = new ArrayList<>(); // private = internal transition log

    public StateMachine(String name, State initialState) {
        this.name = name;
        this.currentState = initialState;
        history.add(initialState.getName());
    }

    public boolean fire(Event event) {
        Transition t = currentState.findTransition(event);
        if (t == null) {
            System.out.println("  [" + name + "] No transition for '" + event.getName() + "' in " + currentState.getName());
            return false;
        }
        currentState.onExit(event);
        t.executeAction(event);
        State oldState = currentState;
        currentState = t.getTarget();
        currentState.onEntry(event);
        history.add(currentState.getName());
        System.out.println("  [" + name + "] " + oldState.getName() + " --[" + event.getName() + "]--> " + currentState.getName());
        return true;
    }

    public boolean fire(String eventName) { return fire(new Event(eventName)); }
    public String getCurrentStateName() { return currentState.getName(); }
    public List<String> getHistory() { return history; }
}

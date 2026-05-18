/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/State.java — Named state with entry/exit actions and transitions list
import java.util.ArrayList;
import java.util.List;

public class State {
    private final String name;                                  // final = state name is immutable
    private final List<Transition> transitions = new ArrayList<>(); // private = only this class manages transitions
    private Action entryAction;                                 // private = action run when entering this state
    private Action exitAction;                                  // private = action run when leaving this state

    public State(String name) { this.name = name; }

    public void addTransition(Transition t) { transitions.add(t); }
    public void setEntryAction(Action a) { this.entryAction = a; }
    public void setExitAction(Action a) { this.exitAction = a; }

    public void onEntry(Event e) { if (entryAction != null) entryAction.execute(e); }
    public void onExit(Event e) { if (exitAction != null) exitAction.execute(e); }

    // Linear search through transitions for matching event
    public Transition findTransition(Event event) {
        for (Transition t : transitions) {
            if (t.getEventName().equals(event.getName())) {
                if (t.getGuard() == null || t.getGuard().evaluate(event)) return t;
            }
        }
        return null;
    }

    public String getName() { return name; }
    @Override public String toString() { return name; }
}

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/StateMachine.java — O(1) transition lookup via HashMap<TransitionKey, List<Transition>>
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StateMachine {
    private final String name;
    // WHY HashMap: O(1) transition lookup by (state, event) composite key.
    // Naive approach uses if-else or linear scan through all transitions in a state = O(n).
    private final Map<TransitionKey, List<Transition>> transitionTable; // HashMap = O(1) lookup by composite key
    private final Map<String, Action> entryActions;  // HashMap = O(1) entry action lookup by state name
    private final Map<String, Action> exitActions;   // HashMap = O(1) exit action lookup by state name
    private String currentState;                     // private = only fire() changes state
    private final List<String> history = new ArrayList<>(); // tracks visited states in order

    StateMachine(String name, String initialState,
                 Map<TransitionKey, List<Transition>> transitionTable,
                 Map<String, Action> entryActions,
                 Map<String, Action> exitActions) {
        this.name = name;
        this.currentState = initialState;
        this.transitionTable = transitionTable;
        this.entryActions = entryActions;
        this.exitActions = exitActions;
        history.add(initialState);
    }

    public boolean fire(Event event) {
        TransitionKey key = new TransitionKey(currentState, event.getName());
        List<Transition> candidates = transitionTable.get(key);

        if (candidates == null || candidates.isEmpty()) {
            System.out.println("  [" + name + "] No transition for '" + event.getName() + "' in " + currentState);
            return false;
        }

        // WHY list of candidates: Multiple transitions for same (state, event)
        // with different guards — first passing guard wins (priority by order).
        for (Transition t : candidates) {
            if (t.isAllowed(event)) {
                String oldState = currentState;

                // Exit action
                Action exitAction = exitActions.get(currentState);
                if (exitAction != null) exitAction.execute(event);

                // Transition action
                t.executeAction(event);

                // State change
                currentState = t.getTargetState();

                // Entry action
                Action entryAction = entryActions.get(currentState);
                if (entryAction != null) entryAction.execute(event);

                history.add(currentState);
                System.out.println("  [" + name + "] " + oldState + " --[" + event.getName() + "]--> " + currentState);
                return true;
            }
        }

        System.out.println("  [" + name + "] All guards failed for '" + event.getName() + "' in " + currentState);
        return false;
    }

    public boolean fire(String eventName) { return fire(new Event(eventName)); }
    public String getCurrentState() { return currentState; }
    public List<String> getHistory() { return history; }
}

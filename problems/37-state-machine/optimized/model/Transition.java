/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Transition.java — Transition definition with target, guard, and action
public class Transition {
    private final String targetState;      // final = destination state fixed at creation
    private final Guard guard;             // final = optional condition; null means always pass
    private final Action action;           // final = optional side-effect on transition

    public Transition(String targetState, Guard guard, Action action) {
        this.targetState = targetState;
        this.guard = guard;
        this.action = action;
    }

    public String getTargetState() { return targetState; }
    public Guard getGuard() { return guard; }
    public Action getAction() { return action; }

    public boolean isAllowed(Event event) {
        return guard == null || guard.evaluate(event);
    }

    public void executeAction(Event event) {
        if (action != null) action.execute(event);
    }
}

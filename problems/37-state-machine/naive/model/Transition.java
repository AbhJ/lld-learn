/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Transition.java — Defines a valid state transition with optional guard and action
public class Transition {
    private final State source;            // final = source state is fixed at creation
    private final State target;            // final = target state is fixed at creation
    private final String eventName;        // final = triggering event name is immutable
    private final Guard guard;             // final = optional condition; null means always allowed
    private final Action action;           // final = optional side-effect during transition

    public Transition(State source, State target, String eventName, Guard guard, Action action) {
        this.source = source;
        this.target = target;
        this.eventName = eventName;
        this.guard = guard;
        this.action = action;
    }

    public State getTarget() { return target; }
    public String getEventName() { return eventName; }
    public Guard getGuard() { return guard; }
    public void executeAction(Event e) { if (action != null) action.execute(e); }
}

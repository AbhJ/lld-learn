/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// state/OrderState.java — O(1) state transition validation using EnumMap
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public enum OrderState { // enum = fixed set of order lifecycle states; type-safe
    CREATED, PAID, SHIPPED, DELIVERED, RETURNED;

    // EnumMap<State, Set<State>> gives O(1) transition validation
    // instead of per-state boolean methods that must be updated for each new state
    private static final EnumMap<OrderState, Set<OrderState>> VALID_TRANSITIONS = new EnumMap<>(OrderState.class); // EnumMap = O(1) lookup keyed by enum; array-backed

    static {
        VALID_TRANSITIONS.put(CREATED, EnumSet.of(PAID));
        VALID_TRANSITIONS.put(PAID, EnumSet.of(SHIPPED));
        VALID_TRANSITIONS.put(SHIPPED, EnumSet.of(DELIVERED));
        VALID_TRANSITIONS.put(DELIVERED, EnumSet.of(RETURNED));
        VALID_TRANSITIONS.put(RETURNED, EnumSet.noneOf(OrderState.class));
    }

    // WHY: O(1) lookup — EnumSet.contains is a single bit-check
    public boolean canTransitionTo(OrderState target) {
        Set<OrderState> allowed = VALID_TRANSITIONS.get(this);
        return allowed != null && allowed.contains(target);
    }

    public Set<OrderState> getAllowedTransitions() {
        return VALID_TRANSITIONS.getOrDefault(this, EnumSet.noneOf(OrderState.class));
    }

    // WHY: True event sourcing — derive current state by replaying every transition event in order.
    // Starts at CREATED (the implicit initial state when an Order is constructed) and walks each
    // event's toState, ignoring action-only events that have no toState (e.g. RETURN_INITIATED).
    // Each transition is validated; an illegal transition in the log signals corruption.
    public static OrderState rebuildFromEvents(List<OrderHistory.OrderEvent> events) {
        OrderState current = CREATED;
        for (OrderHistory.OrderEvent e : events) {
            OrderState next = e.getToState();
            if (next == null) continue; // action-only event (no transition)
            if (e.getFromState() == null) {
                // Initial CREATED event has no fromState; just confirm we start at CREATED
                if (next != CREATED) throw new IllegalStateException("Corrupt log: first event toState " + next);
                current = next;
                continue;
            }
            if (current != e.getFromState() || !current.canTransitionTo(next)) {
                throw new IllegalStateException("Corrupt log: cannot replay " + current + " -> " + next);
            }
            current = next;
        }
        return current;
    }
}

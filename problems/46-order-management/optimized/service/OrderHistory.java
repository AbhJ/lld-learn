/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/OrderHistory.java — Event-sourced order history for complete audit trail
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderHistory {
    // Event sourcing — every state change is an immutable event
    // enabling complete replay of order lifecycle
    private List<OrderEvent> events; // ArrayList = ordered event log; append-only for replay

    public OrderHistory() { this.events = new ArrayList<>(); }

    // WHY: Expose immutable view of the event log so OrderState.rebuildFromEvents() can replay it
    public List<OrderEvent> getEvents() { return java.util.Collections.unmodifiableList(events); }

    public void recordEvent(OrderState fromState, OrderState toState, String description) {
        events.add(new OrderEvent(fromState, toState, description, LocalDateTime.now()));
    }

    public void recordEvent(String action, String description) {
        events.add(new OrderEvent(null, null, action + ": " + description, LocalDateTime.now()));
    }

    public void printTimeline() {
        for (OrderEvent event : events) {
            System.out.println("  " + event);
        }
    }

    // WHY: Can reconstruct state at any point by replaying events
    public OrderState getStateAtIndex(int index) {
        if (index >= events.size()) return null;
        return events.get(index).getToState();
    }

    public static class OrderEvent { // static = no reference to outer class; standalone event record
        private OrderState fromState;   // from-state for transition replay
        private OrderState toState;     // to-state for transition replay
        private String description;     // human-readable description of what happened
        private LocalDateTime timestamp; // when this event occurred

        public OrderEvent(OrderState fromState, OrderState toState, String description, LocalDateTime timestamp) {
            this.fromState = fromState; this.toState = toState;
            this.description = description; this.timestamp = timestamp;
        }

        public OrderState getFromState() { return fromState; }
        public OrderState getToState() { return toState; }

        @Override public String toString() {
            if (fromState != null && toState != null) {
                return fromState + " -> " + toState + ": " + description;
            }
            return description;
        }
    }
}

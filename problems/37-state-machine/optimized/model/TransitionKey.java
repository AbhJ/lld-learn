/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/TransitionKey.java — Composite key (state + event) for O(1) transition table lookup
import java.util.Objects;

public class TransitionKey {
    private final String state;            // final = state part of key is immutable
    private final String event;            // final = event part of key is immutable

    public TransitionKey(String state, String event) {
        this.state = state;
        this.event = event;
    }

    // WHY composite key: HashMap<(State,Event), List<Transition>> gives O(1)
    // lookup vs. naive O(n) linear scan through all transitions in a state.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransitionKey)) return false;
        TransitionKey k = (TransitionKey) o;
        return state.equals(k.state) && event.equals(k.event);
    }

    @Override
    public int hashCode() { return Objects.hash(state, event); }

    @Override
    public String toString() { return "(" + state + "," + event + ")"; }
}

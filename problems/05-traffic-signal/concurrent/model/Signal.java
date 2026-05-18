/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Signal.java — Traffic signal with AtomicReference for thread-safe state transitions

package model;

import java.util.concurrent.atomic.AtomicReference;

public class Signal {
    public enum SignalState {         // enum = fixed states; only valid transitions allowed
        RED, YELLOW, GREEN
    }

    private final String name;        // final = immutable identity; safe to read from any thread
    private final AtomicReference<SignalState> state; // AtomicReference = CAS-based transitions prevent corrupt state

    public Signal(String name, SignalState initialState) {
        this.name = name;
        this.state = new AtomicReference<>(initialState);
    }

    /**
     * CAS-based state transition. Only succeeds if current state matches expected.
     * Prevents corruption when timer and emergency override race.
     */
    public boolean tryTransition(SignalState expected, SignalState newState) {
        return state.compareAndSet(expected, newState);
    }

    public void forceState(SignalState newState) {
        state.set(newState);
    }

    public SignalState getState() { return state.get(); }
    public String getName() { return name; }

    @Override
    public String toString() {
        return name + "=" + state.get();
    }
}

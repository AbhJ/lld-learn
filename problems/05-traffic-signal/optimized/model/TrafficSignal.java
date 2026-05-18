/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/TrafficSignal.java — Single traffic signal with observer notification
//
// Acts as the *Context* in the State pattern: holds a `SignalStateBehavior` and
// replaces it on every transition. The enum `SignalState` is kept as a lightweight
// value for observers (so they keep receiving an enum and don't need to depend on
// the state classes), and the matching enum is derived via behavior.asEnum().

import java.util.ArrayList;
import java.util.List;

class TrafficSignal {
    private String id;                // private = signal identity
    private String direction;         // private = which road direction this signal controls
    private SignalStateBehavior stateBehavior; // State pattern context: current state object
    private List<SignalObserver> observers;    // private = observer list managed internally

    public TrafficSignal(String id, String direction) {
        this.id = id;
        this.direction = direction;
        this.stateBehavior = new RedState();   // signals start RED for safety
        this.observers = new ArrayList<>();
    }

    public void addObserver(SignalObserver observer) {
        observers.add(observer);
    }

    /** Set state by enum — kept for backward compatibility with the Intersection mediator. */
    public void setState(SignalState newState) {
        SignalStateBehavior next;
        switch (newState) {
            case RED:    next = new RedState(); break;
            case YELLOW: next = new YellowState(); break;
            case GREEN:  next = new GreenState(); break;
            default:     throw new IllegalStateException("Unknown state: " + newState);
        }
        applyTransition(next);
    }

    /** Set state directly via the State pattern object. */
    public void setStateBehavior(SignalStateBehavior next) {
        applyTransition(next);
    }

    /** Advance using the State pattern: ask the current state for the next one. */
    public void advance() {
        applyTransition(stateBehavior.tick());
    }

    private void applyTransition(SignalStateBehavior next) {
        SignalState oldState = stateBehavior.asEnum();
        this.stateBehavior = next;
        SignalState newState = stateBehavior.asEnum();
        if (oldState != newState) {
            notifyObservers(oldState, newState);
        }
    }

    private void notifyObservers(SignalState oldState, SignalState newState) {
        for (SignalObserver observer : observers) {
            observer.onSignalChange(id, oldState, newState);
        }
    }

    /** Compatibility accessor: returns the enum value matching the current state object. */
    public SignalState getState() { return stateBehavior.asEnum(); }
    public SignalStateBehavior getStateBehavior() { return stateBehavior; }
    public String getId() { return id; }
    public String getDirection() { return direction; }

    @Override
    public String toString() {
        return direction + "=" + stateBehavior.name();
    }
}

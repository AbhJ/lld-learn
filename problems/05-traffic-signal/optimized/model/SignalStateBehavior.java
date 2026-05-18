/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/SignalStateBehavior.java — State pattern: each signal phase is its own class
// with its own tick() transition logic and instruction text.
//
// Why this is the State pattern (and not just an enum-as-state):
//   - Each state is a separate class (RedState, YellowState, GreenState).
//   - Transitions are encoded inside the state's tick() method, not in a switch.
//   - The TrafficSignal context holds a reference to a SignalStateBehavior and
//     replaces it when the state advances — classic GoF State.

interface SignalStateBehavior {
    /** Advance one tick; return the next state (may be `this` if not yet transitioning). */
    SignalStateBehavior tick();

    /** Human-readable state name, e.g. "RED" / "YELLOW" / "GREEN". */
    String name();

    /** Driver-facing instruction for this phase. */
    String instruction();

    /** The matching SignalState enum value — used by observers and pedestrian signals. */
    SignalState asEnum();
}

/** Vehicles must stop. Transitions to GREEN on tick. */
class RedState implements SignalStateBehavior {
    @Override public SignalStateBehavior tick() { return new GreenState(); }
    @Override public String name() { return "RED"; }
    @Override public String instruction() { return "STOP"; }
    @Override public SignalState asEnum() { return SignalState.RED; }
}

/** Caution; transition imminent. Transitions to RED on tick. */
class YellowState implements SignalStateBehavior {
    @Override public SignalStateBehavior tick() { return new RedState(); }
    @Override public String name() { return "YELLOW"; }
    @Override public String instruction() { return "CAUTION"; }
    @Override public SignalState asEnum() { return SignalState.YELLOW; }
}

/** Vehicles may proceed. Transitions to YELLOW on tick. */
class GreenState implements SignalStateBehavior {
    @Override public SignalStateBehavior tick() { return new YellowState(); }
    @Override public String name() { return "GREEN"; }
    @Override public String instruction() { return "GO"; }
    @Override public SignalState asEnum() { return SignalState.GREEN; }
}

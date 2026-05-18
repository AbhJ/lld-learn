/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/SignalObserver.java — Observer pattern interface for signal state changes
// DESIGN PATTERN: Observer
//
// WHO IMPLEMENTS THIS? → SignalDisplay (in SignalDisplay.java)
// WHO CALLS IT? → TrafficSignal calls notifyObservers() on state change
// WHY? → Decouples "signal changed" from "update display/log event".
//         New observers (e.g., analytics, pedestrian crossing) can be added without touching TrafficSignal.

interface SignalObserver {             // interface = contract; any observer MUST handle signal changes
    void onSignalChange(String signalId, SignalState oldState, SignalState newState);
}

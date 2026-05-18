/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/ElevatorObserver.java — Observer pattern interface for elevator events
// DESIGN PATTERN: Observer
//
// WHO IMPLEMENTS THIS? → Display (in Display.java)
// WHO CALLS IT? → Elevator calls notifyFloorArrival/notifyStateChange/notifyDoorOpen
// WHY? → Decouples "elevator moved" from "show something on screen".

interface ElevatorObserver {           // interface = contract; any observer MUST define these 3 methods
    void onFloorArrival(int elevatorId, int floor, ElevatorState state);
    void onStateChange(int elevatorId, ElevatorState oldState, ElevatorState newState);
    void onDoorOpen(int elevatorId, int floor);
}

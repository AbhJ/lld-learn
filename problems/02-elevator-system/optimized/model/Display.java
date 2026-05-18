/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Display.java — Concrete observer that prints elevator events to the console
//
// This class IMPLEMENTS ElevatorObserver (defined in ElevatorObserver.java).

class Display implements ElevatorObserver { // implements = Display fulfills the observer contract
    private String displayName;       // private = only Display manages its own name
    private boolean verbose;          // private = controls whether to print

    public Display(String displayName, boolean verbose) {
        this.displayName = displayName;
        this.verbose = verbose;
    }

    public Display(String displayName) {
        this(displayName, true);
    }

    @Override
    public void onFloorArrival(int elevatorId, int floor, ElevatorState state) {
        if (verbose) {
            System.out.println("  [" + displayName + "] Elevator " + elevatorId + " at Floor " + floor + " (" + state + ")");
        }
    }

    @Override
    public void onStateChange(int elevatorId, ElevatorState oldState, ElevatorState newState) {
        if (verbose) {
            System.out.println("  [" + displayName + "] Elevator " + elevatorId + ": " + oldState + " -> " + newState);
        }
    }

    @Override
    public void onDoorOpen(int elevatorId, int floor) {
        if (verbose) {
            System.out.println("  [" + displayName + "] Elevator " + elevatorId + " doors open at Floor " + floor);
        }
    }
}

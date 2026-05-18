/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Elevator.java — Single elevator car managing stops, movement, direction, and observers
//
// KEY ALGORITHM: SCAN (elevator algorithm)
// - Collects all requested floors and serves them in sorted order
// - upStops: floors to visit while going UP (sorted ascending via TreeSet)
// - downStops: floors to visit while going DOWN (sorted descending via TreeSet)
// - When all up stops are served, switches to down direction (and vice versa)
// - Like a disk arm sweep — serves requests in one pass without zigzagging

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

class Elevator {
    private int id;                   // private = identity; only readable via getId()
    private int currentFloor;         // private = only step() changes this; tracks physical position
    private ElevatorState state;      // private = IDLE/MOVING_UP/MOVING_DOWN/MAINTENANCE; drives all decisions
    private TreeSet<Integer> upStops; // TreeSet = sorted set; iterate low→high for upward sweep
    private TreeSet<Integer> downStops; // TreeSet = sorted set; iterate high→low for downward sweep
    private int minFloor;             // private = lowest possible floor (bounds check)
    private int maxFloor;             // private = highest possible floor (bounds check)
    private List<ElevatorObserver> observers; // private = observer list; notified on floor arrival/state change

    public Elevator(int id, int minFloor, int maxFloor) {
        this.id = id;
        this.currentFloor = minFloor;
        this.state = ElevatorState.IDLE;
        this.upStops = new TreeSet<>();
        this.downStops = new TreeSet<>();
        this.minFloor = minFloor;
        this.maxFloor = maxFloor;
        this.observers = new ArrayList<>();
    }

    public void addObserver(ElevatorObserver observer) {
        observers.add(observer);
    }

    private void notifyFloorArrival() {
        for (ElevatorObserver o : observers) {
            o.onFloorArrival(id, currentFloor, state);
        }
    }

    private void notifyStateChange(ElevatorState oldState, ElevatorState newState) {
        for (ElevatorObserver o : observers) {
            o.onStateChange(id, oldState, newState);
        }
    }

    private void notifyDoorOpen() {
        for (ElevatorObserver o : observers) {
            o.onDoorOpen(id, currentFloor);
        }
    }

    /**
     * Adds a floor to the appropriate stop list based on SCAN algorithm.
     *
     * Decision logic:
     * - floor ABOVE current → must go UP to reach it → add to upStops
     * - floor BELOW current → must go DOWN to reach it → add to downStops
     * - floor == current → we're already here! → just open doors immediately
     */
    public void addStop(int floor) {
        if (state == ElevatorState.MAINTENANCE) return; // can't accept stops during maintenance

        if (floor > currentFloor) {
            // Floor is above us → add to upward sweep list
            upStops.add(floor);
        } else if (floor < currentFloor) {
            // Floor is below us → add to downward sweep list
            downStops.add(floor);
        } else {
            // floor == currentFloor → already here, just open doors
            notifyDoorOpen();
        }
    }

    /**
     * Convenience: adds both the pickup floor and destination floor as stops.
     */
    public void addRequest(Request request) {
        addStop(request.getSourceFloor());
        addStop(request.getDestinationFloor());
    }

    /**
     * step() = ONE tick of time. Called repeatedly by the system to simulate movement.
     *
     * Each call does ONE of:
     * - If IDLE: pick a direction (up if there are upStops, else down) and start moving
     * - If MOVING_UP: move one floor up, check if anyone wants to get off, switch if done
     * - If MOVING_DOWN: move one floor down, check if anyone wants to get off, switch if done
     *
     * Think of it like a game loop — each tick, the elevator moves one floor.
     */
    public void step() {
        if (state == ElevatorState.MAINTENANCE) return; // out of service

        // --- IDLE: decide which direction to start moving ---
        if (state == ElevatorState.IDLE) {
            if (!upStops.isEmpty()) {
                setState(ElevatorState.MOVING_UP);   // people waiting above → go up
            } else if (!downStops.isEmpty()) {
                setState(ElevatorState.MOVING_DOWN); // people waiting below → go down
            }
            // else: no stops anywhere → stay idle
            return;
        }

        // --- MOVING UP: advance one floor, check for stops ---
        if (state == ElevatorState.MOVING_UP) {
            currentFloor++;                          // physically move up one floor
            notifyFloorArrival();                    // tell observers we arrived at a new floor

            if (upStops.contains(currentFloor)) {   // someone pressed this floor
                upStops.remove(currentFloor);        // served! remove from queue
                notifyDoorOpen();                    // open doors for passengers
            }

            if (upStops.isEmpty()) {                 // no more floors to visit going up
                if (!downStops.isEmpty()) {
                    setState(ElevatorState.MOVING_DOWN); // switch to downward sweep
                } else {
                    setState(ElevatorState.IDLE);    // nothing left → rest
                }
            }
        }
        // --- MOVING DOWN: advance one floor down, check for stops ---
        else if (state == ElevatorState.MOVING_DOWN) {
            currentFloor--;                          // physically move down one floor
            notifyFloorArrival();

            if (downStops.contains(currentFloor)) {  // someone pressed this floor
                downStops.remove(currentFloor);      // served!
                notifyDoorOpen();
            }

            if (downStops.isEmpty()) {               // no more floors to visit going down
                if (!upStops.isEmpty()) {
                    setState(ElevatorState.MOVING_UP); // switch to upward sweep
                } else {
                    setState(ElevatorState.IDLE);
                }
            }
        }
    }

    private void setState(ElevatorState newState) {
        ElevatorState oldState = this.state;
        this.state = newState;
        notifyStateChange(oldState, newState);       // tell observers about direction change
    }

    public void setMaintenance(boolean maintenance) {
        if (maintenance) {
            setState(ElevatorState.MAINTENANCE);
            upStops.clear();                         // cancel all pending stops
            downStops.clear();
        } else {
            setState(ElevatorState.IDLE);            // return to service
        }
    }

    public int distanceTo(int floor) {
        return Math.abs(currentFloor - floor);
    }

    public boolean hasStops() {
        return !upStops.isEmpty() || !downStops.isEmpty();
    }

    public int getId() { return id; }
    public int getCurrentFloor() { return currentFloor; }
    public ElevatorState getState() { return state; }
    public int getPendingStops() { return upStops.size() + downStops.size(); }

    public String getStatus() {
        return "Elevator " + id + ": Floor " + currentFloor + " | " + state +
               " | Pending: " + getPendingStops();
    }
}

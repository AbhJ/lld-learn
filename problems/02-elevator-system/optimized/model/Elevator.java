/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Elevator.java — Elevator with pre-computed dispatch score for O(1) scheduling decisions
//
// KEY ALGORITHM: SCAN (elevator algorithm) + dispatch scoring
// - upStops/downStops: same sweep logic as naive version
// - scoreDirty: a "dirty flag" optimization — instead of recomputing the dispatch score
//   every time someone asks "which elevator should serve this request?", we cache the score
//   and only recompute when something changes (floor moved, stop added, state changed).
//   This makes the scheduler's per-request cost O(1) amortized instead of O(n) every time.

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

class Elevator {
    private int id;                   // private = encapsulated identity
    private int currentFloor;         // private = only step() modifies this; tracks physical position
    private ElevatorState state;      // private = IDLE/MOVING_UP/MOVING_DOWN/MAINTENANCE
    private TreeSet<Integer> upStops; // TreeSet = sorted set; O(log n) insert, O(1) first()/last()
    private TreeSet<Integer> downStops; // TreeSet = sorted descending sweep
    private int minFloor;             // private = lowest possible floor
    private int maxFloor;             // private = highest possible floor
    private List<ElevatorObserver> observers; // private = observer list

    // --- Dispatch score caching (dirty flag pattern) ---
    // The scheduler asks "how suitable is this elevator for request X?"
    // Computing the answer involves distance + direction + pending stops.
    // Instead of recomputing every time, we mark it "dirty" when state changes,
    // and only recompute on next query.
    private int cachedScore;          // last computed dispatch score
    private boolean scoreDirty;       // true = score is stale, needs recomputation

    public Elevator(int id, int minFloor, int maxFloor) {
        this.id = id;
        this.currentFloor = minFloor;
        this.state = ElevatorState.IDLE;
        this.upStops = new TreeSet<>();
        this.downStops = new TreeSet<>();
        this.minFloor = minFloor;
        this.maxFloor = maxFloor;
        this.observers = new ArrayList<>();
        this.scoreDirty = true;       // starts dirty; first query will compute fresh
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
        if (state == ElevatorState.MAINTENANCE) return;

        if (floor > currentFloor) {
            upStops.add(floor);       // above us → upward sweep will serve it
        } else if (floor < currentFloor) {
            downStops.add(floor);     // below us → downward sweep will serve it
        } else {
            notifyDoorOpen();         // already here → open doors now
        }
        scoreDirty = true;            // new stop added → cached dispatch score is stale
    }

    public void addRequest(Request request) {
        addStop(request.getSourceFloor());
        addStop(request.getDestinationFloor());
    }

    /**
     * step() = ONE tick of time. Called repeatedly to simulate movement.
     *
     * Each call:
     * - IDLE → pick direction and start
     * - MOVING_UP → move one floor up, open doors if someone wants off, switch if done
     * - MOVING_DOWN → move one floor down, same logic
     */
    public void step() {
        if (state == ElevatorState.MAINTENANCE) return;

        // IDLE: pick a direction based on pending stops
        if (state == ElevatorState.IDLE) {
            if (!upStops.isEmpty()) {
                setState(ElevatorState.MOVING_UP);
            } else if (!downStops.isEmpty()) {
                setState(ElevatorState.MOVING_DOWN);
            }
            return;
        }

        // MOVING UP: advance one floor, serve stop if present, switch when done
        if (state == ElevatorState.MOVING_UP) {
            currentFloor++;
            scoreDirty = true;        // position changed → score is stale
            notifyFloorArrival();
            if (upStops.contains(currentFloor)) {
                upStops.remove(currentFloor);
                notifyDoorOpen();     // doors open → passengers exit/enter
            }
            if (upStops.isEmpty()) {  // upward sweep complete
                if (!downStops.isEmpty()) {
                    setState(ElevatorState.MOVING_DOWN); // reverse direction
                } else {
                    setState(ElevatorState.IDLE);        // nothing left
                }
            }
        }
        // MOVING DOWN: same logic, opposite direction
        else if (state == ElevatorState.MOVING_DOWN) {
            currentFloor--;
            scoreDirty = true;
            notifyFloorArrival();
            if (downStops.contains(currentFloor)) {
                downStops.remove(currentFloor);
                notifyDoorOpen();
            }
            if (downStops.isEmpty()) {
                if (!upStops.isEmpty()) {
                    setState(ElevatorState.MOVING_UP);
                } else {
                    setState(ElevatorState.IDLE);
                }
            }
        }
    }

    private void setState(ElevatorState newState) {
        ElevatorState oldState = this.state;
        this.state = newState;
        this.scoreDirty = true;       // state changed → score is stale
        notifyStateChange(oldState, newState);
    }

    public void setMaintenance(boolean maintenance) {
        if (maintenance) {
            setState(ElevatorState.MAINTENANCE);
            upStops.clear();
            downStops.clear();
        } else {
            setState(ElevatorState.IDLE);
        }
    }

    /**
     * Computes how "good" this elevator is for serving a given request.
     * Lower score = better choice. Used by the scheduler to pick the best elevator.
     *
     * Scoring logic:
     * - IDLE: pure distance (just go there directly)
     * - Same direction AND on the way: just distance (will pass by naturally)
     * - Same direction BUT already passed: must complete current sweep + come back
     * - Opposite direction: distance + penalty (more pending stops = worse)
     *
     * WHERE IS THIS USED? → ElevatorSystem.dispatch() calls this on each elevator,
     * picks the one with the lowest score. This is the "scheduling strategy" result.
     */
    public int computeDispatchScore(Request request) {
        if (state == ElevatorState.MAINTENANCE) return Integer.MAX_VALUE; // never pick a broken elevator

        int srcFloor = request.getSourceFloor();
        int distance = Math.abs(currentFloor - srcFloor);

        // IDLE: pure distance — just go there directly
        if (state == ElevatorState.IDLE) {
            return distance;
        }

        // Going UP and request is UP and we haven't passed the floor yet → on the way!
        if (state == ElevatorState.MOVING_UP && request.getDirection() == Direction.UP
                && currentFloor <= srcFloor) {
            return distance;
        }
        // Going DOWN and request is DOWN and we haven't passed the floor yet → on the way!
        if (state == ElevatorState.MOVING_DOWN && request.getDirection() == Direction.DOWN
                && currentFloor >= srcFloor) {
            return distance;
        }

        // Going UP but floor is BELOW us → must finish going up, then come back down
        if (state == ElevatorState.MOVING_UP && currentFloor > srcFloor) {
            int topStop = upStops.isEmpty() ? currentFloor : upStops.last();
            return (topStop - currentFloor) + (topStop - srcFloor); // go to top + come back
        }
        // Going DOWN but floor is ABOVE us → must finish going down, then come back up
        if (state == ElevatorState.MOVING_DOWN && currentFloor < srcFloor) {
            int bottomStop = downStops.isEmpty() ? currentFloor : downStops.first();
            return (currentFloor - bottomStop) + (srcFloor - bottomStop); // go to bottom + come back
        }

        // Opposite direction or other edge case: penalize based on how busy this elevator is
        return distance + getPendingStops() * 2;
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

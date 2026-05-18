/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/SchedulingStrategy.java — Strategy pattern interface for elevator dispatch algorithms
// DESIGN PATTERN: Strategy
//
// WHO IMPLEMENTS THIS? → SCANStrategy, LOOKStrategy (below in this file)
// WHO CALLS IT? → ElevatorSystem.handleRequest() calls strategy.selectElevator()
// WHY? → Different algorithms for "which elevator should serve this request?"
//         Swap strategies at runtime without changing ElevatorSystem code.

import java.util.List;

interface SchedulingStrategy {        // interface = contract; any scheduling algorithm MUST define these
    Elevator selectElevator(List<Elevator> elevators, Request request);
    String getName();
}

/**
 * SCAN Strategy: picks the elevator with the lowest "cost" to reach the request.
 *
 * Scoring:
 * - IDLE elevator → cost = pure distance to source floor
 * - Moving TOWARD the request (same direction, hasn't passed) → cost = distance
 * - Moving AWAY or opposite direction → cost = distance + 10 (penalty)
 *
 * Picks elevator with the lowest score.
 */
class SCANStrategy implements SchedulingStrategy { // implements = fulfills the strategy contract
    @Override
    public Elevator selectElevator(List<Elevator> elevators, Request request) {
        Elevator best = null;
        int bestScore = Integer.MAX_VALUE;

        for (Elevator elevator : elevators) {
            if (elevator.getState() == ElevatorState.MAINTENANCE) continue; // skip broken elevators

            int distance = elevator.distanceTo(request.getSourceFloor());
            int score = distance;

            // IDLE: just go there → pure distance
            if (elevator.getState() == ElevatorState.IDLE) {
                score = distance;
            }
            // Going UP, request is UP, and we haven't passed it yet → on the way!
            else if (elevator.getState() == ElevatorState.MOVING_UP && request.getDirection() == Direction.UP
                    && elevator.getCurrentFloor() <= request.getSourceFloor()) {
                score = distance;
            }
            // Going DOWN, request is DOWN, and we haven't passed it yet → on the way!
            else if (elevator.getState() == ElevatorState.MOVING_DOWN && request.getDirection() == Direction.DOWN
                    && elevator.getCurrentFloor() >= request.getSourceFloor()) {
                score = distance;
            }
            // Otherwise: must reverse or detour → penalty
            else {
                score = distance + 10;
            }

            if (score < bestScore) {
                bestScore = score;
                best = elevator;
            }
        }
        return best;
    }

    @Override
    public String getName() { return "SCAN"; }
}

/**
 * LOOK Strategy: Like SCAN but also factors in elevator LOAD (pending stops).
 * A busy elevator gets a higher penalty, spreading requests more evenly.
 *
 * Scoring:
 * - IDLE → pure distance
 * - Same direction and on the way → distance + pendingStops (load factor)
 * - Otherwise → distance + 20 + pendingStops (heavy penalty)
 */
class LOOKStrategy implements SchedulingStrategy { // implements = fulfills the strategy contract
    @Override
    public Elevator selectElevator(List<Elevator> elevators, Request request) {
        Elevator best = null;
        int bestScore = Integer.MAX_VALUE;

        for (Elevator elevator : elevators) {
            if (elevator.getState() == ElevatorState.MAINTENANCE) continue;

            int distance = elevator.distanceTo(request.getSourceFloor());
            int score;

            if (elevator.getState() == ElevatorState.IDLE) {
                score = distance;
            } else if ((elevator.getState() == ElevatorState.MOVING_UP && request.getDirection() == Direction.UP
                    && elevator.getCurrentFloor() <= request.getSourceFloor()) ||
                    (elevator.getState() == ElevatorState.MOVING_DOWN && request.getDirection() == Direction.DOWN
                    && elevator.getCurrentFloor() >= request.getSourceFloor())) {
                score = distance + elevator.getPendingStops(); // on the way but account for load
            } else {
                score = distance + 20 + elevator.getPendingStops(); // heavy penalty
            }

            if (score < bestScore) {
                bestScore = score;
                best = elevator;
            }
        }
        return best;
    }

    @Override
    public String getName() { return "LOOK"; }
}

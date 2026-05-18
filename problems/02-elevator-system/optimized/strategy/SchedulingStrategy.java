/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/SchedulingStrategy.java — Optimized scheduling with effective-distance scoring
// DESIGN PATTERN: Strategy

import java.util.List;
import java.util.PriorityQueue;
import java.util.Comparator;

interface SchedulingStrategy {        // interface = contract; any scheduling algorithm MUST define these
    Elevator selectElevator(List<Elevator> elevators, Request request);
    String getName();
}

/**
 * Optimized SCAN: Uses Elevator.computeDispatchScore() which accounts for
 * effective travel distance (direction changes, sweep completion) rather than
 * naive linear distance. O(n) over elevators but with much better scoring.
 */
class SCANStrategy implements SchedulingStrategy { // implements = fulfills the strategy contract
    @Override
    public Elevator selectElevator(List<Elevator> elevators, Request request) {
        Elevator best = null;
        int bestScore = Integer.MAX_VALUE;

        for (Elevator elevator : elevators) {
            int score = elevator.computeDispatchScore(request);
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
 * Optimized LOOK: Uses a PriorityQueue to rank elevators by composite score
 * (effective distance + load factor). The PQ approach enables easy extension
 * to select top-K candidates for multi-criteria decisions.
 */
class LOOKStrategy implements SchedulingStrategy { // implements = fulfills the strategy contract
    @Override
    public Elevator selectElevator(List<Elevator> elevators, Request request) {
        // PriorityQueue ranks elevators by dispatch score — best candidate at head
        PriorityQueue<Elevator> candidates = new PriorityQueue<>(
            Comparator.comparingInt(e -> e.computeDispatchScore(request))
        );

        for (Elevator elevator : elevators) {
            if (elevator.getState() != ElevatorState.MAINTENANCE) {
                candidates.offer(elevator);
            }
        }

        return candidates.poll(); // Best elevator or null if all in maintenance
    }

    @Override
    public String getName() { return "LOOK"; }
}

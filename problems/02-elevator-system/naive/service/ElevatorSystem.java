/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/ElevatorSystem.java — Orchestrates multiple elevators, dispatches requests via strategy
// DESIGN PATTERN: Facade
//
// This is the FACADE: Main.java talks to ElevatorSystem, not individual elevators.
// ElevatorSystem decides WHICH elevator handles a request (via SchedulingStrategy)
// and advances time (via step()).

import java.util.ArrayList;
import java.util.List;

class ElevatorSystem {
    private List<Elevator> elevators; // private = the pool of elevator cars
    private SchedulingStrategy strategy; // private = swappable algorithm (Strategy pattern)
    private int totalFloors;          // private = building config

    public ElevatorSystem(int numElevators, int totalFloors) {
        this.totalFloors = totalFloors;
        this.elevators = new ArrayList<>();
        this.strategy = new SCANStrategy(); // default scheduling algorithm
        for (int i = 1; i <= numElevators; i++) {
            elevators.add(new Elevator(i, 0, totalFloors));
        }
    }

    public void setStrategy(SchedulingStrategy strategy) {
        this.strategy = strategy;    // swap scheduling algorithm at runtime
    }

    public void addObserverToAll(ElevatorObserver observer) {
        for (Elevator elevator : elevators) {
            elevator.addObserver(observer); // every elevator notifies this observer
        }
    }

    /**
     * Dispatches a request to the best elevator (chosen by the scheduling strategy).
     * The strategy scores all elevators and picks the one with the lowest cost.
     */
    public Elevator handleRequest(Request request) {
        Elevator selected = strategy.selectElevator(elevators, request);
        if (selected != null) {
            selected.addRequest(request); // adds source + destination floors to its stop list
        }
        return selected;
    }

    /**
     * step() = advance time by ONE tick. ALL elevators move one floor simultaneously.
     *
     * This is the simulation clock. In a real system this would be event-driven,
     * but for LLD interviews we use discrete ticks to make behavior deterministic and testable.
     *
     * Usage in Main.java:
     *   system.handleRequest(request);  // assign request to an elevator
     *   system.stepN(10);               // simulate 10 ticks of time passing
     *   // Now elevators have moved, picked up passengers, delivered them
     */
    public void step() {
        for (Elevator elevator : elevators) {
            elevator.step(); // each elevator moves one floor (see Elevator.step() for details)
        }
    }

    /** Convenience: run N ticks of simulation */
    public void stepN(int n) {
        for (int i = 0; i < n; i++) {
            step();
        }
    }

    public Elevator getElevator(int index) {
        return elevators.get(index);
    }

    public void setMaintenance(int elevatorIndex, boolean maintenance) {
        elevators.get(elevatorIndex).setMaintenance(maintenance);
    }

    public String getStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("Strategy: ").append(strategy.getName()).append("\n");
        for (Elevator elevator : elevators) {
            sb.append("  ").append(elevator.getStatus()).append("\n");
        }
        return sb.toString().trim();
    }

    public boolean allIdle() {
        for (Elevator e : elevators) {
            if (e.hasStops()) return false;
        }
        return true;
    }
}

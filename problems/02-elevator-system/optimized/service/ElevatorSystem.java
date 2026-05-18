/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/ElevatorSystem.java — Orchestrates multiple elevators, dispatches via strategy
// DESIGN PATTERN: Facade
//
// FACADE: Main.java talks only to this class.
// Delegates scheduling to SchedulingStrategy, movement to Elevator.step().

import java.util.ArrayList;
import java.util.List;

class ElevatorSystem {
    private List<Elevator> elevators; // private = the pool of elevator cars
    private SchedulingStrategy strategy; // private = swappable scheduling algorithm
    private int totalFloors;          // private = building config

    public ElevatorSystem(int numElevators, int totalFloors) {
        this.totalFloors = totalFloors;
        this.elevators = new ArrayList<>();
        this.strategy = new SCANStrategy();
        for (int i = 1; i <= numElevators; i++) {
            elevators.add(new Elevator(i, 0, totalFloors));
        }
    }

    public void setStrategy(SchedulingStrategy strategy) {
        this.strategy = strategy;
    }

    public void addObserverToAll(ElevatorObserver observer) {
        for (Elevator elevator : elevators) {
            elevator.addObserver(observer);
        }
    }

    /**
     * Dispatches a request to the best elevator (chosen by strategy).
     * Strategy uses Elevator.computeDispatchScore() for smart scoring.
     */
    public Elevator handleRequest(Request request) {
        Elevator selected = strategy.selectElevator(elevators, request);
        if (selected != null) {
            selected.addRequest(request);
        }
        return selected;
    }

    /**
     * step() = advance simulation by ONE tick. All elevators move simultaneously.
     * See Elevator.step() for what happens inside each elevator per tick.
     */
    public void step() {
        for (Elevator elevator : elevators) {
            elevator.step();
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

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/Intersection.java — Coordinates multiple signals with hardcoded tick-based timing

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Intersection {
    private String name;              // private = intersection identity
    private Map<String, TrafficSignal> signals; // private = direction-to-signal mapping
    private List<PedestrianSignal> pedestrianSignals; // private = coordinated pedestrian signals
    private Timer timer;              // private = phase timing logic
    private String currentGreenDirection; // private = which direction currently has green
    private String[] directionOrder;  // private = round-robin direction sequence
    private int currentDirectionIndex; // private = position in the direction cycle
    private EmergencyOverride emergencyOverride; // private = emergency preemption state
    private int greenDuration;        // private = how many ticks green lasts
    private int yellowDuration;       // private = how many ticks yellow lasts
    private int tickCount;            // private = ticks elapsed in current phase
    private SignalState currentPhase; // private = GREEN or YELLOW within active direction

    public Intersection(String name, int greenDuration, int yellowDuration) {
        this.name = name;
        this.signals = new HashMap<>();
        this.pedestrianSignals = new ArrayList<>();
        this.timer = new Timer(greenDuration, yellowDuration);
        this.emergencyOverride = new EmergencyOverride();
        this.greenDuration = greenDuration;
        this.yellowDuration = yellowDuration;
        this.tickCount = 0;
        this.currentDirectionIndex = 0;
    }

    public void addSignal(String direction) {
        TrafficSignal signal = new TrafficSignal(direction, direction);
        signals.put(direction, signal);
    }

    public void addPedestrianSignal(PedestrianSignal pedSignal, String trafficDirection) {
        pedestrianSignals.add(pedSignal);
        TrafficSignal associated = signals.get(trafficDirection);
        if (associated != null) {
            associated.addObserver(pedSignal);
        }
    }

    public void initialize(String[] directionOrder) {
        this.directionOrder = directionOrder;
        this.currentDirectionIndex = 0;
        this.currentGreenDirection = directionOrder[0];
        this.currentPhase = SignalState.GREEN;
        this.tickCount = 0;

        for (Map.Entry<String, TrafficSignal> entry : signals.entrySet()) {
            if (entry.getKey().equals(currentGreenDirection)) {
                entry.getValue().setState(SignalState.GREEN);
            } else {
                entry.getValue().setState(SignalState.RED);
            }
        }
    }

    public void tick() {
        if (emergencyOverride.isActive()) return;

        tickCount++;
        if (currentPhase == SignalState.GREEN && tickCount >= greenDuration) {
            currentPhase = SignalState.YELLOW;
            signals.get(currentGreenDirection).setState(SignalState.YELLOW);
            tickCount = 0;
        } else if (currentPhase == SignalState.YELLOW && tickCount >= yellowDuration) {
            signals.get(currentGreenDirection).setState(SignalState.RED);
            currentDirectionIndex = (currentDirectionIndex + 1) % directionOrder.length;
            currentGreenDirection = directionOrder[currentDirectionIndex];
            signals.get(currentGreenDirection).setState(SignalState.GREEN);
            currentPhase = SignalState.GREEN;
            tickCount = 0;
        }
    }

    public void emergencyOverride(String direction) {
        emergencyOverride.activate(direction);
        for (TrafficSignal signal : signals.values()) {
            signal.setState(SignalState.RED);
        }
        signals.get(direction).setState(SignalState.GREEN);
        currentGreenDirection = direction;
        currentPhase = SignalState.GREEN;
        tickCount = 0;
    }

    public void clearEmergency() {
        emergencyOverride.deactivate();
        for (int i = 0; i < directionOrder.length; i++) {
            if (directionOrder[i].equals(currentGreenDirection)) {
                currentDirectionIndex = i;
                break;
            }
        }
        tickCount = 0;
    }

    public TrafficSignal getSignal(String direction) { return signals.get(direction); }
    public List<PedestrianSignal> getPedestrianSignals() { return pedestrianSignals; }

    public String getStatus() {
        StringBuilder sb = new StringBuilder();
        for (String dir : directionOrder) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(signals.get(dir));
        }
        return sb.toString();
    }
}

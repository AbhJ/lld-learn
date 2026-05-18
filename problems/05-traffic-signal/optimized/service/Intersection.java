/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/Intersection.java — Configurable timing with per-direction durations and dynamic adjustment

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Optimized: Each direction has configurable green/yellow duration.
 * Supports dynamic timing adjustment (e.g., longer green for busier roads).
 * Uses ScheduledExecutorService for real-time mode (disabled in test/tick mode).
 */
class Intersection {
    private String name;              // private = intersection identity
    private Map<String, TrafficSignal> signals; // HashMap = O(1) direction-to-signal lookup
    private List<PedestrianSignal> pedestrianSignals; // private = coordinated ped signals
    private Timer timer;              // private = phase timing logic
    private String currentGreenDirection; // private = which direction currently has green
    private String[] directionOrder;  // private = round-robin direction sequence
    private int currentDirectionIndex; // private = position in the direction cycle
    private EmergencyOverride emergencyOverride; // private = emergency preemption state
    // Per-direction configurable durations
    private Map<String, Integer> greenDurations; // HashMap = O(1) lookup of per-direction green time
    private Map<String, Integer> yellowDurations; // HashMap = O(1) lookup of per-direction yellow time
    private int defaultGreenDuration; // private = fallback when no per-direction config
    private int defaultYellowDuration; // private = fallback when no per-direction config
    private int tickCount;            // private = ticks elapsed in current phase
    private SignalState currentPhase; // private = GREEN or YELLOW within active direction
    // Optional real-time scheduler
    private ScheduledExecutorService scheduler; // ScheduledExecutorService = auto-ticks at fixed interval

    public Intersection(String name, int greenDuration, int yellowDuration) {
        this.name = name;
        this.signals = new HashMap<>();
        this.pedestrianSignals = new ArrayList<>();
        this.timer = new Timer(greenDuration, yellowDuration);
        this.emergencyOverride = new EmergencyOverride();
        this.defaultGreenDuration = greenDuration;
        this.defaultYellowDuration = yellowDuration;
        this.greenDurations = new HashMap<>();
        this.yellowDurations = new HashMap<>();
        this.tickCount = 0;
        this.currentDirectionIndex = 0;
    }

    public void addSignal(String direction) {
        TrafficSignal signal = new TrafficSignal(direction, direction);
        signals.put(direction, signal);
        greenDurations.put(direction, defaultGreenDuration);
        yellowDurations.put(direction, defaultYellowDuration);
    }

    /**
     * Dynamically adjust timing for a direction (e.g., based on traffic sensors).
     */
    public void setDirectionTiming(String direction, int green, int yellow) {
        greenDurations.put(direction, green);
        yellowDurations.put(direction, yellow);
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

    /**
     * Start real-time mode: signals change automatically based on timing.
     * For production use; tests use manual tick() calls.
     */
    public void startRealTime(long tickIntervalMs) {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::tick, tickIntervalMs, tickIntervalMs, TimeUnit.MILLISECONDS);
    }

    public void stopRealTime() {
        if (scheduler != null) {
            scheduler.shutdown();
            scheduler = null;
        }
    }

    public void tick() {
        if (emergencyOverride.isActive()) return;

        int currentGreen = greenDurations.getOrDefault(currentGreenDirection, defaultGreenDuration);
        int currentYellow = yellowDurations.getOrDefault(currentGreenDirection, defaultYellowDuration);

        tickCount++;
        if (currentPhase == SignalState.GREEN && tickCount >= currentGreen) {
            currentPhase = SignalState.YELLOW;
            signals.get(currentGreenDirection).setState(SignalState.YELLOW);
            tickCount = 0;
        } else if (currentPhase == SignalState.YELLOW && tickCount >= currentYellow) {
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

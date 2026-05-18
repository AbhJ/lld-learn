/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/TrafficController.java — Intersection controller with ReentrantLock for multi-signal coordination

package service;

import model.Signal;
import model.Signal.SignalState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicInteger;

public class TrafficController {
    private final Signal northSouth;  // final = reference never changes; safe publication
    private final Signal eastWest;    // final = reference never changes; safe publication
    private final ReentrantLock intersectionLock; // ReentrantLock = ensures only one thread mutates both signals at once
    private final List<String> stateLog; // synchronizedList = thread-safe append from timer+emergency threads
    private final AtomicInteger timerTransitions; // AtomicInteger = lock-free counter for stats
    private final AtomicInteger emergencyOverrides; // AtomicInteger = lock-free counter for stats
    private volatile boolean invalidStateDetected; // volatile = written by one thread, readable by all immediately

    public TrafficController() {
        this.northSouth = new Signal("NorthSouth", SignalState.GREEN);
        this.eastWest = new Signal("EastWest", SignalState.RED);
        this.intersectionLock = new ReentrantLock();
        this.stateLog = Collections.synchronizedList(new ArrayList<>());
        this.timerTransitions = new AtomicInteger(0);
        this.emergencyOverrides = new AtomicInteger(0);
        this.invalidStateDetected = false;
    }

    /**
     * Timer-based signal cycle. Acquires intersection lock to ensure
     * no two greens exist simultaneously.
     */
    public void timerCycle() {
        intersectionLock.lock();
        try {
            // Current: NS=GREEN, EW=RED -> transition to NS=RED, EW=GREEN
            SignalState nsState = northSouth.getState();
            SignalState ewState = eastWest.getState();

            if (nsState == SignalState.GREEN && ewState == SignalState.RED) {
                northSouth.tryTransition(SignalState.GREEN, SignalState.YELLOW);
                northSouth.tryTransition(SignalState.YELLOW, SignalState.RED);
                eastWest.tryTransition(SignalState.RED, SignalState.GREEN);
            } else if (nsState == SignalState.RED && ewState == SignalState.GREEN) {
                eastWest.tryTransition(SignalState.GREEN, SignalState.YELLOW);
                eastWest.tryTransition(SignalState.YELLOW, SignalState.RED);
                northSouth.tryTransition(SignalState.RED, SignalState.GREEN);
            }
            timerTransitions.incrementAndGet();
            logState("TIMER");
            validateState();
        } finally {
            intersectionLock.unlock();
        }
    }

    /**
     * Emergency override — forces a specific signal to GREEN.
     * Must acquire the intersection lock to avoid two greens.
     */
    public void emergencyOverride(String direction) {
        intersectionLock.lock();
        try {
            if ("NS".equals(direction)) {
                eastWest.forceState(SignalState.RED);
                northSouth.forceState(SignalState.GREEN);
            } else {
                northSouth.forceState(SignalState.RED);
                eastWest.forceState(SignalState.GREEN);
            }
            emergencyOverrides.incrementAndGet();
            logState("EMERGENCY(" + direction + ")");
            validateState();
        } finally {
            intersectionLock.unlock();
        }
    }

    private void validateState() {
        SignalState ns = northSouth.getState();
        SignalState ew = eastWest.getState();
        if (ns == SignalState.GREEN && ew == SignalState.GREEN) {
            invalidStateDetected = true;
            stateLog.add("!!! INVALID: Both signals GREEN !!!");
        }
    }

    private void logState(String trigger) {
        stateLog.add(trigger + " -> [" + northSouth + ", " + eastWest + "]");
    }

    public Signal getNorthSouth() { return northSouth; }
    public Signal getEastWest() { return eastWest; }
    public List<String> getStateLog() { return stateLog; }
    public int getTimerTransitions() { return timerTransitions.get(); }
    public int getEmergencyOverrides() { return emergencyOverrides.get(); }
    public boolean isInvalidStateDetected() { return invalidStateDetected; }
}

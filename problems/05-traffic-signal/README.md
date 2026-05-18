# Traffic Signal System


## Problem Statement
Design a traffic signal control system for a road intersection. The system manages multiple traffic signals at an intersection, coordinating their states to ensure safe traffic flow. Each signal cycles through RED, YELLOW, and GREEN states with configurable timing.

The system supports emergency vehicle override (immediately turning relevant signals green), pedestrian crossing signals that coordinate with vehicle signals, and a mediator pattern to ensure conflicting signals are never both green simultaneously.

## Requirements

### Functional Requirements
- Manage traffic signals with RED, YELLOW, GREEN states
- Configurable timing for each state (green duration, yellow duration)
- Coordinate signals at intersection (conflicting directions never both green)
- Emergency override: priority signal goes green immediately
- Pedestrian crossing signals synchronized with vehicle signals
- Timer-based automatic state transitions
- Observer notification on signal changes

### Non-functional Requirements
- Safety: conflicting signals never simultaneously green
- Extensible: support new intersection layouts
- Observable: notify displays and crossing signals of changes
- Deterministic simulation for testing

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| State | SignalStateBehavior → RedState / YellowState / GreenState | GoF State pattern — each signal phase is its own class with its own `tick()` transition logic. `TrafficSignal` is the context that swaps in a new state object on each transition |
| Observer | Signal change notification | Notify pedestrian signals and displays |
| Mediator | Intersection | Coordinate conflicting signals safely |

## Folder Structure
```
05-traffic-signal/
├── naive/          <- Start here. Hardcoded timing, simple cycle.
│   ├── model/      -> Data classes (SignalState, TrafficSignal, PedestrianSignal, Timer)
│   ├── service/    -> Business logic (Intersection, EmergencyOverride)
│   └── Main.java   -> Entry point — run this
└── optimized/      <- Production-grade. Per-direction configurable timing, ScheduledExecutor.
    ├── model/
    ├── service/
    └── Main.java
```

### How to Run
```bash
# Run the naive (simple) version:
cd naive
mkdir -p out
javac -d out model/*.java service/*.java Main.java
java -cp out Main

# Run the optimized version:
cd optimized
mkdir -p out
javac -d out model/*.java service/*.java Main.java
java -cp out Main
```

### Naive vs Optimized — What Changes?
| Operation | Naive | Optimized |
|-----------|-------|-----------|
| Timing control | Hardcoded per-cycle durations | Per-direction configurable green/yellow maps |
| Cycle execution | Manual step() calls | ScheduledExecutorService for real-time mode |
| Direction timing | Same duration for all directions | setDirectionTiming() for dynamic adjustment |
| Extensibility | Change code to adjust timing | Runtime-configurable without restart |

---

## Class Diagram (Text)
```
Intersection (Mediator)
├── TrafficSignal[] (one per direction: N, S, E, W)
│   └── SignalState (RED, YELLOW, GREEN)
├── PedestrianSignal[] (crosswalks)
├── Timer (manages timing for transitions)
└── EmergencyOverride (priority control)
```

## Key Design Decisions
- Mediator ensures safety: only one direction green at a time
- Timer uses tick-based simulation for deterministic testing
- Emergency override immediately forces target direction green, others red
- Pedestrian signals derive state from vehicle signals in their direction

## Interview Tips
- Start with safety: conflicting signals must never be green simultaneously
- Draw state transition diagram: GREEN -> YELLOW -> RED -> GREEN
- Discuss timing: how long for each state, all-red clearance interval
- Talk about emergency: what happens to in-progress transitions?
- Consider extensibility: T-intersections, left-turn arrows

---

## Concurrency Version

**Race condition:** Timer thread changing signal while emergency override thread tries to force green — state corruption where two directions are simultaneously green.

```bash
cd concurrent
mkdir -p out
find . -name '*.java' | xargs javac -d out
java -cp out Main
```

### Key Concurrency Techniques
| Technique | Where | Why |
|-----------|-------|-----|
| AtomicReference\<SignalState\> | Signal.state | CAS-based state transitions prevent partial updates |
| ReentrantLock | TrafficController.intersectionLock | Coordinates multi-signal transitions — ensures no two greens at same intersection |
| Invariant validation | TrafficController.validateState() | Detects any invalid state (two greens) after every transition |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

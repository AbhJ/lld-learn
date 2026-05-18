# Elevator System


## Problem Statement
Design an elevator system for a building with multiple floors and multiple elevators. The system should efficiently handle floor requests from passengers, optimize elevator movement using different scheduling algorithms, and provide real-time status updates.

Each elevator can be in one of several states: IDLE, MOVING_UP, MOVING_DOWN, or MAINTENANCE. The system controller receives requests (source floor, destination floor) and dispatches the most appropriate elevator based on the configured scheduling strategy.

The system should support different scheduling algorithms (SCAN, LOOK), handle concurrent requests, and provide observer-based notifications for floor arrivals and state changes.

## Requirements

### Functional Requirements
- Support multiple elevators in a building
- Handle floor requests with source and destination
- Dispatch optimal elevator based on scheduling strategy
- Track elevator state (IDLE, MOVING_UP, MOVING_DOWN, MAINTENANCE)
- Support SCAN and LOOK scheduling algorithms
- Display real-time elevator status
- Handle elevator maintenance mode

### Non-functional Requirements
- Efficient elevator dispatching
- Extensible scheduling strategies
- Observable state changes
- Simulation-friendly (step-based movement)

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| State | ElevatorState | Manage elevator behavior based on state |
| Strategy | SchedulingStrategy | Swap scheduling algorithms at runtime |
| Observer | Display | Notify displays when elevator arrives at floor |

## Folder Structure
```
02-elevator-system/
├── naive/          <- Start here. Simple synchronized scheduling.
│   ├── model/      -> Data classes (Elevator, ElevatorState, Request, Display)
│   ├── service/    -> Business logic (ElevatorSystem)
│   ├── strategy/   -> Swappable algorithms (SCAN, LOOK scheduling)
│   └── Main.java   -> Entry point — run this
└── optimized/      <- Production-grade. PriorityQueue dispatch, effective travel distance.
    ├── model/
    ├── service/
    ├── strategy/
    └── Main.java
```

### How to Run
```bash
# Run the naive (simple) version:
cd naive
mkdir -p out
javac -d out model/*.java strategy/*.java service/*.java Main.java
java -cp out Main

# Run the optimized version:
cd optimized
mkdir -p out
javac -d out model/*.java strategy/*.java service/*.java Main.java
java -cp out Main
```

### Naive vs Optimized — What Changes?
| Operation | Naive | Optimized |
|-----------|-------|-----------|
| Dispatch scoring | Linear distance + fixed penalty | Effective travel distance accounting for sweep completion |
| Stop management | TreeSet with basic ordering | PriorityQueue with direction-aware ordering |
| Strategy selection | SCAN iterates all elevators linearly | LOOK uses computeDispatchScore() |
| Direction reversal | Checks if stops exist above/below | Smart reversal with minimum future travel |

---

## Class Diagram (Text)
```
ElevatorSystem (Controller)
├── Elevator[] (each has state, current floor, request queue)
│   └── ElevatorState (IDLE, MOVING_UP, MOVING_DOWN, MAINTENANCE)
├── SchedulingStrategy (interface)
│   ├── SCANStrategy
│   └── LOOKStrategy
└── Display (Observer)

Request → sourceFloor, destinationFloor, direction
```

## Key Design Decisions
- Step-based simulation rather than real-time for testability
- Each elevator maintains its own sorted request queue
- SCAN: elevator goes to top/bottom before reversing; LOOK: reverses when no more requests in current direction
- Nearest elevator with matching direction gets priority

## Interview Tips
- Clarify: number of elevators, floors, peak traffic patterns
- Discuss scheduling trade-offs: SCAN vs LOOK vs FCFS
- Talk about fairness: preventing starvation of far-floor requests
- Consider edge cases: simultaneous requests, elevator at capacity
- Mention how this scales: elevator banks for high-rise buildings

---

## Concurrency Version

**Race condition:** Two passengers pressing buttons at the same time, elevator assigned to both — one gets skipped. Or two elevators accepting the same request (double-assignment).

```bash
cd concurrent
mkdir -p out
find . -name '*.java' | xargs javac -d out
java -cp out Main
```

### Key Concurrency Techniques
| Technique | Where | Why |
|-----------|-------|-----|
| ConcurrentLinkedQueue | ElevatorController.requestQueue | Lock-free thread-safe request submission from multiple passengers |
| AtomicReference\<Elevator\> | Per-request assignment lock | CAS ensures only one elevator claims each request — prevents double-assignment |
| CountDownLatch | Main.java | Ensures all 20 passenger threads start simultaneously to maximize contention |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

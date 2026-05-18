# State Machine


## Problem Statement
Design a generic, configurable finite state machine. A user defines states, transitions (current state + event -> next state), guards, and entry/exit actions through a builder, then fires events to drive the machine.

Use cases include order lifecycles and traffic lights. Invalid transitions are rejected without changing state.

## Requirements

### Functional Requirements
- Define states and an initial state
- Register transitions keyed by (state, event)
- Optional guard predicate per transition
- Optional action on transition; entry/exit actions per state
- Fire events and record transition history
- Reject invalid events without state change

### Non-functional Requirements
- O(1) transition lookup (optimized via TransitionKey map)
- Build-time configuration; runtime is read-only
- Reusable across domains (orders, lights, etc.)

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| State Machine | StateMachine | Models stateful behavior driven by events |
| Builder | StateMachineBuilder | Fluent API for declaring states and transitions |
| Strategy | Guard, Action | Pluggable predicates and side effects on transitions |
| Command | Action | Encapsulates a transition side-effect as an object |

## Folder Structure

```
37-state-machine/
├── README.md
├── VARIATIONS.md
├── naive/
│   ├── model/        ← Event, State, Transition, Action
│   ├── service/      ← StateMachine, StateMachineBuilder
│   ├── strategy/     ← Guard
│   └── Main.java
└── optimized/
    ├── model/        ← Event, Action, Guard, TransitionKey, Transition
    ├── service/      ← StateMachine, StateMachineBuilder
    └── Main.java
```

## How to Run

```bash
# Naive
cd naive && mkdir -p out && javac -d out model/*.java strategy/*.java service/*.java Main.java && java -cp out Main

# Optimized
cd optimized && mkdir -p out && javac -d out model/*.java service/*.java Main.java && java -cp out Main
```

## Naive vs Optimized

| Aspect | Naive | Optimized |
|--------|-------|-----------|
| Transition lookup | Linear scan through State's transition list O(n) | `HashMap<(State,Event), List<Transition>>` O(1) |
| State storage | State objects with mutable transition list | Immutable transition table built at construction |
| Validation | Runtime errors on missing transitions | Builder validates all targets exist at build time |
| Guard evaluation | If-else through candidates | Ordered list with first-match-wins |
| Scalability | Degrades with many transitions per state | Constant-time regardless of transition count |
| Type safety | String-based events | Composite key with proper equals/hashCode |

---

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** Two events arriving simultaneously causing invalid state transition.

```bash
cd concurrent
mkdir -p out
find . -name '*.java' | xargs javac -d out
java -cp out Main
```

### Key Concurrency Techniques Used
| Technique | Where | Why |
|-----------|-------|-----|
| AtomicReference\<State\> | currentState | CAS ensures only valid transitions succeed |
| compareAndSet | transition() | Atomic check-and-update of state |
| canTransitionTo() | State enum | Validates transitions before CAS attempt |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

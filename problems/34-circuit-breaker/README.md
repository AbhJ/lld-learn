# Circuit Breaker


## Problem Statement
Design a circuit breaker that wraps calls to an unreliable service. When failures exceed a threshold, the breaker trips OPEN and fails fast. After a cool-down it moves to HALF_OPEN to test recovery, returning to CLOSED on a configurable number of successes.

The naive variant models each state as its own class. The optimized variant uses atomic state transitions with a sliding-window failure counter.

## Requirements

### Functional Requirements
- Wrap arbitrary service calls (functional interface)
- Three states: CLOSED, OPEN, HALF_OPEN
- Trip OPEN on consecutive (or windowed) failures
- Auto-transition OPEN -> HALF_OPEN after timeout
- Close again after N successful probes
- Expose metrics (success/failure counts, current state)

### Non-functional Requirements
- Fail-fast in OPEN state with no overhead
- Thread-safe state transitions
- Bounded memory (fixed-size sliding window in optimized)

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| State | CircuitState (Closed/Open/HalfOpen) | Each state defines its own execute behavior |
| Strategy | ServiceCall | Functional interface lets callers plug in any operation |
| Sliding Window | SlidingWindow (optimized) | Track recent failures over a fixed time bucket |

## Folder Structure

```
34-circuit-breaker/
├── README.md
├── VARIATIONS.md
├── naive/
│   ├── model/        ← CircuitBreakerConfig, ServiceCall, CircuitBreakerMetrics
│   ├── service/      ← CircuitBreaker
│   ├── state/        ← CircuitState, ClosedState, OpenState, HalfOpenState
│   └── Main.java
└── optimized/
    ├── model/        ← CircuitBreakerConfig, ServiceCall
    ├── service/      ← SlidingWindow, CircuitBreaker (atomic state transitions)
    └── Main.java
```

## How to Run

```bash
# Naive
cd naive && mkdir -p out && javac -d out model/*.java state/*.java service/*.java Main.java && java -cp out Main

# Optimized
cd optimized && mkdir -p out && javac -d out model/*.java service/*.java Main.java && java -cp out Main
```

## Naive vs Optimized

| Aspect | Naive | Optimized |
|--------|-------|-----------|
| Failure tracking | Simple consecutive failure counter | Sliding window ring buffer (rate-based) |
| Trip condition | N consecutive failures | Failure RATE exceeds threshold over window |
| State management | `synchronized` on entire execute | `AtomicReference` CAS for lock-free transitions |
| Half-open probes | Single trial | Configurable probe count with atomic tracking |
| Intermittent faults | Single success resets counter (too forgiving) | Window captures true failure rate |
| Concurrency | Blocking synchronized | Multiple threads can check state without blocking |

---

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** State transition during concurrent requests — one thread opens circuit while another is mid-request.

```bash
cd concurrent
mkdir -p out
javac -d out model/*.java service/*.java Main.java
java -cp out Main
```

### Key Concurrency Techniques Used
| Technique | Where | Why |
|-----------|-------|-----|
| AtomicReference<State> (CAS) | ConcurrentCircuitBreaker.state | Only one thread transitions CLOSED -> OPEN |
| AtomicInteger | failureCount, successCount | Lock-free metrics tracking |
| AtomicLong | lastFailureTime | Thread-safe timestamp for reset timeout |
| CAS state machine | CLOSED -> OPEN -> HALF_OPEN -> CLOSED | Each transition is atomic, no intermediate states |
| Supplier<T> pattern | execute() method | Generic request execution through the breaker |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

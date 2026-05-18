# Order Management


## Problem Statement
Design an order management system that takes an order through its lifecycle: created, paid, shipped, delivered, returned, refunded. Each step is gated by the current state, and shipping cost is computed by a configurable strategy.

The naive variant uses an OrderState interface per state. The optimized variant uses an EnumMap-based transition table and an event-sourced history.

## Requirements

### Functional Requirements
- Create an order with items
- Accept payment (transitions to PAID)
- Ship with a shipping strategy and tracking number
- Mark as delivered
- Initiate returns and process refunds
- Reject illegal transitions (e.g. ship before pay)
- Maintain a timeline/history

### Non-functional Requirements
- All transitions auditable
- Strategy plug-in for shipping rates

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| State | OrderState (Created, Paid, Shipped, Delivered) | Allowed actions depend on current order state |
| Strategy | ShippingStrategy (Standard, Express, Overnight) | Swappable shipping cost calculation |
| Event Sourcing | OrderHistory (optimized) | Reconstruct timeline from a log of state changes |
| Facade | OrderService | Unified API for creating and managing orders |

## Folder Structure

```
46-order-management/
├── README.md
├── VARIATIONS.md
├── naive/
│   ├── model/        ← OrderItem, Payment, Return, Refund
│   ├── service/      ← Order, OrderService, OrderHistory
│   ├── strategy/     ← ShippingStrategy (Standard, Express, Overnight)
│   ├── state/        ← OrderState (Created, Paid, Shipped, Delivered)
│   └── Main.java
└── optimized/
    ├── model/        ← OrderItem, Payment, Return, Refund
    ├── service/      ← Order, OrderService, OrderHistory (event-sourced)
    ├── strategy/     ← ShippingStrategy
    ├── state/        ← OrderState (EnumMap transition table)
    └── Main.java
```

## How to Run

```bash
# Naive
cd problems/46-order-management/naive
mkdir -p out && javac -d out model/*.java strategy/*.java state/*.java service/*.java Main.java && java -cp out Main

# Optimized
cd problems/46-order-management/optimized
mkdir -p out && javac -d out model/*.java strategy/*.java state/*.java service/*.java Main.java && java -cp out Main
```

## Naive vs Optimized

| Aspect | Naive | Optimized |
|--------|-------|-----------|
| Transition validation | Per-state boolean methods (canPay, canShip...) | EnumMap<State, Set<State>> for O(1) bit-check |
| Adding new states | Modify all state classes | Add one line to transition table |
| Order history | Simple list of strings | Event sourcing: typed events with state transitions |
| State reconstruction | Not possible | Replay events to reconstruct state at any point |

---

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** Payment confirmation and user cancellation arriving at same instant — order is both paid and cancelled.

```bash
cd concurrent
mkdir -p out
find . -name '*.java' | xargs javac -d out
java -cp out Main
```

### Key Concurrency Techniques Used
| Technique | Where | Why |
|-----------|-------|-----|
| AtomicReference<OrderState> | Order.state | CAS-based state machine — only one transition from CREATED wins |
| CAS transitions | Order.tryTransition() | Cancel only from CREATED, pay only from CREATED — mutually exclusive |
| Enum state machine | OrderState | Clear state space, impossible to reach contradictory states |
| CountDownLatch | Main (startLatch) | Pay + cancel threads fire simultaneously for true race condition |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

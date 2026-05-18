# Vending Machine


## Problem Statement
Design a vending machine that accepts coins, allows product selection, dispenses products, and returns change. The machine manages an inventory of products with different prices and tracks inserted money.

The vending machine operates as a state machine with distinct states: Idle (waiting for coins), HasMoney (coins inserted, waiting for selection), Dispensing (product being dispensed), and each state defines valid operations. The machine uses a greedy algorithm to calculate optimal change.

The system should handle edge cases like insufficient funds, out-of-stock products, and inability to make change.

## Requirements

### Functional Requirements
- Accept coins: PENNY (1c), NICKEL (5c), DIME (10c), QUARTER (25c)
- Display available products with prices
- Select product after inserting sufficient money
- Dispense product and return correct change
- Refund all inserted money on cancel
- Track product inventory
- Handle insufficient funds and out-of-stock cases

### Non-functional Requirements
- State machine ensures valid operation sequences
- Greedy change calculation
- Thread-safe coin insertion and product dispensing
- Extensible product catalog

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| State | IdleState, HasMoneyState, DispensingState | Enforce valid operation sequences per FSM state |
| Strategy | ChangeStrategy → GreedyChangeStrategy | Swappable change-making algorithm injected into the machine |
| Observer | VendingMachineObserver → LoggingObserver | Fan-out events for sales, low stock, and sold-out alerts |

## Folder Structure
```
04-vending-machine/
├── naive/          <- Start here. Synchronized state transitions.
│   ├── model/      -> Data classes (Coin, Product, Inventory)
│   ├── service/    -> Business logic (ChangeCalculator, VendingMachine)
│   ├── state/      -> State pattern (IdleState, HasMoneyState, DispensingState)
│   └── Main.java   -> Entry point — run this
└── optimized/      <- Production-grade. CAS operations, lock-free concurrency.
    ├── model/
    ├── service/
    ├── state/
    └── Main.java
```

### How to Run
```bash
# Run the naive (simple) version:
cd naive
mkdir -p out
javac -d out model/*.java state/*.java service/*.java Main.java
java -cp out Main

# Run the optimized version:
cd optimized
mkdir -p out
javac -d out model/*.java state/*.java service/*.java Main.java
java -cp out Main
```

### Naive vs Optimized — What Changes?
| Operation | Naive | Optimized |
|-----------|-------|-----------|
| Inventory tracking | HashMap with synchronized | ConcurrentHashMap + AtomicInteger |
| Coin insertion | synchronized methods | AtomicInteger CAS (lock-free) |
| Dispense | synchronized decrement | CAS loop with retry |
| Thread safety | Global object lock | Per-item lock-free operations |

---

## Class Diagram (Text)
```
VendingMachine (Context)
├── State (interface)
│   ├── IdleState (no money inserted)
│   ├── HasMoneyState (money inserted, awaiting selection)
│   └── DispensingState (dispensing product)
├── Inventory (product stock management)
├── Product (name, price, code)
├── Coin (enum: PENNY, NICKEL, DIME, QUARTER)
└── ChangeCalculator (greedy change algorithm)
```

## Key Design Decisions
- State pattern prevents invalid operations (e.g., selecting product without money)
- Coin enum with cent values for precise arithmetic (no floating point)
- Greedy change algorithm: largest coins first
- Inventory tracks quantity per product code

## Interview Tips
- Start with state machine diagram: transitions between states
- Use integer cents to avoid floating-point precision issues
- Discuss change-making: greedy works for standard coin denominations
- Handle edge cases: exact change, out of stock, cancel mid-transaction
- Consider concurrency: what if two people use the machine?

---

## Concurrency Version

**Race condition:** Two users inserting coins and selecting the same product with quantity=1 — both read stock > 0, both dispense, resulting in negative inventory.

```bash
cd concurrent
mkdir -p out
find . -name '*.java' | xargs javac -d out
java -cp out Main
```

### Key Concurrency Techniques
| Technique | Where | Why |
|-----------|-------|-----|
| AtomicInteger | Product.quantity | Lock-free atomic stock management |
| CAS loop | Product.tryPurchase() | compareAndSet in a loop — only one thread gets the last item |
| ConcurrentHashMap | VendingMachine.products | Thread-safe product lookup without global lock |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

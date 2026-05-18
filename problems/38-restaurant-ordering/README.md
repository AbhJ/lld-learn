# Restaurant Ordering System


## Problem Statement
Design a restaurant ordering system that manages tables, takes orders from a menu, queues orders for the kitchen, and bills customers. The naive variant uses one FIFO kitchen queue; the optimized variant routes orders by station and prioritizes VIP tables.

The system supports different billing strategies (dine-in with tip, takeout, etc.).

## Requirements

### Functional Requirements
- Manage menu items grouped by category
- Assign and free tables
- Place orders against a table
- Queue and process kitchen orders (FIFO or priority)
- Mark orders as served
- Bill orders with a configurable strategy

### Non-functional Requirements
- O(log n) priority pull for high-priority orders (optimized)
- Extensible billing without modifying core flow

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Strategy | BillingStrategy | Swappable billing rules (dine-in, takeout) |
| Queue / Priority Queue | KitchenQueue / PriorityKitchen | Order kitchen processing |
| Facade | Restaurant | Single entry point over menu, tables, kitchen, billing |

## Folder Structure

```
38-restaurant-ordering/
├── README.md
├── VARIATIONS.md
├── naive/
│   ├── model/        ← MenuItem, Order, Table
│   ├── service/      ← KitchenQueue (FIFO LinkedList), Restaurant
│   ├── strategy/     ← BillingStrategy
│   └── Main.java
└── optimized/
    ├── model/        ← MenuItem (with station), Order (with priority), Table (VIP)
    ├── service/      ← PriorityKitchen, Restaurant
    ├── strategy/     ← BillingStrategy
    └── Main.java
```

## How to Run

```bash
# Naive
cd naive && mkdir -p out && javac -d out model/*.java strategy/*.java service/*.java Main.java && java -cp out Main

# Optimized
cd optimized && mkdir -p out && javac -d out model/*.java strategy/*.java service/*.java Main.java && java -cp out Main
```

## Naive vs Optimized

| Aspect | Naive | Optimized |
|--------|-------|-----------|
| Kitchen queue | Single FIFO `LinkedList` | `PriorityQueue` (VIP/large orders first) |
| Order priority | All orders treated equally | VIP=10, large(4+)=5, normal=1 |
| Station routing | None — one queue | Items route to grill/pasta/cold/drinks stations |
| Parallel prep | Sequential processing | Station-based parallel preparation |
| Fairness | First-come-first-served | Priority-based with VIP fast-track |
| Throughput | Bottlenecked by single queue | Multiple stations work concurrently |

---

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** Kitchen picks up order while waiter is still adding items — partial order prepared.

```bash
cd concurrent
mkdir -p out
find . -name '*.java' | xargs javac -d out
java -cp out Main
```

### Key Concurrency Techniques Used
| Technique | Where | Why |
|-----------|-------|-----|
| ReentrantLock | Order.itemsLock | Waiter holds lock while building order |
| AtomicReference\<OrderState\> | Order.state | Atomic lifecycle transitions |
| ConcurrentLinkedQueue | Restaurant.orderQueue | Thread-safe order submission queue |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

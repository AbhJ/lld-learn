# Pizza Delivery


## Problem Statement
Design a pizza delivery ordering system. Customers configure a pizza (size, crust, toppings) through a builder, optionally add wrappers like extra cheese, then place orders that the shop processes.

The optimized variant caches pizza prices, validates builder inputs, and uses a ConcurrentHashMap for the order registry.

## Requirements

### Functional Requirements
- Build a pizza with size, crust, and toppings
- Decorate a pizza with extras (extra cheese, etc.)
- Create orders containing one or more pizzas
- Compute order total including extras
- Process orders through the shop

### Non-functional Requirements
- Builder rejects invalid configurations (optimized)
- Price caching avoids recomputation (optimized)
- Thread-safe order registry (optimized)

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Builder | PizzaBuilder | Fluent construction of pizzas with many options |
| Decorator | PizzaDecorator (ExtraCheese, etc.) | Layer optional add-ons that change price |
| Facade | PizzaShop | Single entry point for ordering and processing |

## Folder Structure

```
48-pizza-delivery/
├── README.md
├── VARIATIONS.md
├── naive/
│   ├── model/        ← Pizza, Order, Size, Crust, Topping
│   ├── service/      ← PizzaShop, PizzaBuilder, PizzaDecorator
│   └── Main.java
└── optimized/
    ├── model/        ← Pizza (cached price), Order (running total), Size, Crust, Topping
    ├── service/      ← PizzaShop (ConcurrentHashMap), PizzaBuilder (validated), PizzaDecorator
    └── Main.java
```

## How to Run

```bash
# Naive
cd problems/48-pizza-delivery/naive
mkdir -p out && javac -d out model/*.java service/*.java Main.java && java -cp out Main

# Optimized
cd problems/48-pizza-delivery/optimized
mkdir -p out && javac -d out model/*.java service/*.java Main.java && java -cp out Main
```

## Naive vs Optimized

| Aspect | Naive | Optimized |
|--------|-------|-----------|
| Price calculation | Recalculates from toppings list every time | Pre-computed cached price at build time |
| Order total | Recalculates O(n) on every call | Running total updated O(1) on addPizza |
| Order tracking | ArrayList (not thread-safe) | ConcurrentHashMap for concurrent access |
| Builder validation | No validation | Max toppings limit, required topping check |
| Topping set storage | Each pizza owns its own `List<Topping>` copy (heap grows linearly with pizza count even when toppings are identical) | **Topping interning**: `PizzaBuilder` keeps a `Map<Set<Topping>, Set<Topping>>` cache keyed by `EnumSet`; pizzas built with identical toppings share the SAME immutable `Set` instance — proven via `==` / `identityHashCode` in `Main` |
| Decorator pattern | Composable extras | Same, with documented composability |

---

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** Multiple orders from same address computed simultaneously — delivery tracking gets mixed up.

```bash
cd concurrent
mkdir -p out
find . -name '*.java' | xargs javac -d out
java -cp out Main
```

### Key Concurrency Techniques Used
| Technique | Where | Why |
|-----------|-------|-----|
| ConcurrentHashMap<orderId, PizzaOrder> | DeliveryTracker.orders | Thread-safe tracking map — no mixup between concurrent orders |
| AtomicReference<DeliveryState> | PizzaOrder.state | CAS lifecycle transitions prevent invalid state jumps |
| CAS state progression | PizzaOrder.advanceTo() | Each stage transition atomic — duplicate processors blocked |
| synchronized stateHistory | PizzaOrder.stateHistory | Consistent audit trail even under concurrent progression |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

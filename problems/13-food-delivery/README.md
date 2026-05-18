# Food Delivery System


## Problem Statement
Design a food delivery system similar to Zomato or DoorDash. The system allows customers to browse restaurants, view menus, place orders, and track delivery status. Restaurants can manage their menus and accept/reject orders.

Delivery agents are assigned to orders based on configurable strategies (nearest agent, least busy). Orders progress through a state machine from PLACED to DELIVERED, with real-time status notifications to customers. The system also supports restaurant and agent ratings.

The platform acts as a mediator between customers, restaurants, and delivery agents, coordinating the entire ordering and delivery workflow.

## Requirements
### Functional Requirements
- Register restaurants with menus
- Browse restaurants and menu items
- Place orders with multiple items
- Order state tracking (placed, confirmed, preparing, out for delivery, delivered, cancelled)
- Delivery agent assignment with strategy selection
- Rating system for restaurants and delivery agents
- Order status notifications

### Non-functional Requirements
- Real-time order tracking
- Fair delivery agent assignment
- Extensible for new delivery strategies
- Reliable state transitions

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Observer | Order status updates | Notify customers of state changes |
| Strategy | Delivery assignment | Pluggable agent selection algorithms |
| State | Order lifecycle | Clean state transitions with validation |
| Command | Order operations (`OrderCommand` -> `PlaceOrderCommand`/`CancelOrderCommand`/`AssignDriverCommand` run by `CommandInvoker`) | Encapsulate order actions with undo history |

## Folder Structure
```
13-food-delivery/
├── naive/
│   ├── model/      -> Customer, DeliveryAgent, MenuItem, Order, OrderState, Rating, Restaurant
│   ├── service/    -> FoodDeliverySystem
│   ├── strategy/   -> DeliveryStrategy (Nearest, LeastBusy)
│   └── Main.java
└── optimized/
    ├── model/
    ├── service/    -> FoodDeliverySystem
    ├── strategy/   -> SpatialGridStrategy, PriorityDispatchStrategy
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

### Naive vs Optimized
| Operation | Naive | Optimized |
|-----------|-------|-----------|
| Find nearest agent | O(n) scan all agents | O(1) grid cell lookup + ring expand |
| Agent index rebuild | N/A (no index) | O(a) where a = available agents |
| Dispatch with load balancing | Separate strategy O(n) | Composite score in PriorityQueue O(n log n) |
| Spatial locality | Not exploited | Grid cells group nearby agents |

---

## Class Diagram (Text)
```
FoodDeliverySystem (Facade)
 ├── Restaurant
 │    └── MenuItem
 ├── Order
 │    └── OrderState (enum)
 ├── Customer (Observer)
 ├── DeliveryAgent
 ├── DeliveryStrategy (Interface)
 │    ├── NearestAgentStrategy
 │    └── LeastBusyStrategy
 └── Rating
```

## How to Compile and Run
```bash
cd problems/13-food-delivery
mkdir -p out
javac -d out *.java
java -cp out Main
```

## Expected Output
```
=== Food Delivery System Demo ===
Order placed at Pizza Palace: 2x Margherita Pizza, 1x Garlic Bread
Order confirmed by restaurant.
Agent assigned: Mike (nearest to restaurant)
Order out for delivery.
Order delivered! Rate your experience.
Restaurant rating: 4.5/5
```

## Key Design Decisions
- State pattern ensures valid order transitions (cannot deliver before preparing)
- Strategy pattern allows runtime selection of agent assignment algorithm
- Observer decouples notification from order processing
- Facade simplifies the complex subsystem interactions

## Interview Tips
- Draw the order state machine first
- Explain how Strategy pattern enables A/B testing different assignment algorithms
- Discuss scalability: how would you handle thousands of concurrent orders?
- Mention eventual consistency for real-time tracking

---

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** Multiple orders trying to claim the same delivery agent simultaneously.

```bash
cd concurrent
mkdir -p out
find . -name '*.java' | xargs javac -d out
java -cp out Main
```

### Key Concurrency Techniques Used
| Technique | Where | Why |
|-----------|-------|-----|
| AtomicReference (CAS) | DeliveryAgent.tryAssign() | Only one order can claim an agent (null -> assigned) |
| CopyOnWriteArrayList | DispatchService.agents | Safe iteration over agent list during dispatch |
| ConcurrentLinkedQueue | DispatchService.pendingOrders | Thread-safe order queueing when no agents free |
| AtomicLong | Order ID generation | Unique order IDs without synchronization |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

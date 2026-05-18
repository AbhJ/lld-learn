# Ride Sharing System (Uber)


## Problem Statement
Design a ride-sharing system similar to Uber/Lyft. The system matches riders with nearby drivers, estimates fares, tracks trips in real-time, and handles payments. Surge pricing applies during high-demand periods.

Riders request rides by specifying pickup and dropoff locations. The system uses configurable matching strategies (nearest driver, highest rated) to find suitable drivers. Trips progress through states from requested to completed, with fare calculation happening at trip end.

The system supports multiple vehicle types, driver ratings, and dynamic pricing based on demand.

## Requirements
### Functional Requirements
- Rider can request a ride with pickup/dropoff locations
- Match rider with available driver based on strategy
- Fare estimation before ride confirmation
- Real-time trip state tracking
- Surge pricing during peak demand
- Rating system for drivers and riders
- Payment processing

### Non-functional Requirements
- Fast driver matching (low latency)
- Fair driver assignment
- Accurate fare calculation
- Concurrent ride handling

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Strategy | Driver matching, fare pricing | Different algorithms for matching and pricing |
| Observer | Trip status updates | Notify rider/driver of state changes |
| State | Trip lifecycle | Manage trip state transitions |
| Proxy | Payment processing | `PaymentProcessor` -> `PaymentProxy` -> `RealPaymentProcessor`: proxy adds pre-auth, audit logging, and a single retry around the real gateway call |

## Folder Structure
```
14-ride-sharing/
├── naive/
│   ├── model/      -> Driver, Rider, Location, Trip, TripState, Payment
│   ├── service/    -> RideService
│   ├── strategy/   -> MatchingStrategy (Nearest, HighestRated), PricingStrategy
│   └── Main.java
└── optimized/
    ├── model/
    ├── service/    -> RideService
    ├── strategy/   -> GeospatialGridStrategy (ConcurrentHashMap<GridCell, List<Driver>>)
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
| Driver matching | O(n) scan all drivers | O(1) grid cell + ring expansion |
| Thread safety | None (single-threaded) | ConcurrentHashMap for grid index |
| Index maintenance | N/A | Rebuilt per request (amortized) |
| Nearby search | Distance calc on every driver | Only drivers in nearby cells |

---

## Class Diagram (Text)
```
RideService (Controller)
 ├── Rider
 ├── Driver (with Location)
 ├── Trip
 │    └── TripState (enum)
 ├── Location (GPS)
 ├── MatchingStrategy (Interface)
 │    ├── NearestDriverStrategy
 │    └── HighestRatedStrategy
 ├── PricingStrategy (Interface)
 │    ├── BasePricing
 │    └── SurgePricing
 └── Payment
```

## How to Compile and Run
```bash
cd problems/14-ride-sharing
mkdir -p out
javac -d out *.java
java -cp out Main
```

## Expected Output
```
=== Ride Sharing System Demo ===
Ride requested by Alice from Downtown to Airport
Estimated fare: $25.50
Driver matched: Bob (4.8 stars, 2.3 km away)
Trip started. En route to destination.
Trip completed. Final fare: $27.30
Payment processed: $27.30 via Credit Card
```

## Key Design Decisions
- Strategy pattern allows A/B testing of matching algorithms
- Surge pricing is a decorator on base pricing strategy
- Trip state machine prevents invalid transitions
- Location uses simple Euclidean distance (Haversine for production)

## Interview Tips
- Focus on the matching algorithm and how it scales
- Discuss surge pricing: when to activate, how to calculate multiplier
- Explain driver availability management
- Talk about real-time tracking implementation choices
- Mention how to handle edge cases (driver cancellation, no drivers available)

---

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** Two riders requesting the same driver simultaneously.

```bash
cd concurrent
mkdir -p out
javac -d out model/*.java service/*.java Main.java
java -cp out Main
```

### Key Concurrency Techniques Used
| Technique | Where | Why |
|-----------|-------|-----|
| AtomicReference (CAS) | Driver.tryAssign() | Only one rider can claim a driver (AVAILABLE -> ASSIGNED) |
| ConcurrentLinkedQueue | ConcurrentMatchingService.waitQueue | Thread-safe rider queueing when no drivers available |
| CopyOnWriteArrayList | ConcurrentMatchingService.drivers | Safe iteration over driver list during matching |
| ConcurrentHashMap | activeTrips | Thread-safe trip storage and lookup |
| CAS driver lifecycle | Driver state machine | Each state transition is atomic, no double-assignment |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

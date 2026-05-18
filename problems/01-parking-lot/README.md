# Parking Lot System


## Problem Statement
Design a multi-level parking lot system that can accommodate different types of vehicles (motorcycles, cars, and trucks). The system should manage multiple entry and exit points, track parking duration, and calculate fees based on configurable pricing strategies.

The parking lot has multiple levels, each with a fixed number of spots of different sizes (SMALL for motorcycles, MEDIUM for cars, LARGE for trucks). When a vehicle arrives, the system assigns an appropriate spot based on vehicle type, issues a ticket, and upon exit, calculates the parking fee based on the duration and pricing strategy.

The system should notify observers when spot availability changes, support different pricing strategies (hourly, flat-rate), and ensure thread-safe operations at entry/exit points.

## Requirements

### Functional Requirements
- Support multiple vehicle types: Motorcycle, Car, Truck
- Multiple parking levels with configurable spots per level
- Assign appropriate spot size based on vehicle type
- Issue tickets on entry with timestamp
- Calculate fees on exit based on duration
- Support multiple pricing strategies (hourly, flat-rate, vehicle-type based)
- Track available spots per level and type
- Notify observers when availability changes

### Non-functional Requirements
- Thread-safe entry/exit operations
- Extensible pricing strategies
- O(1) spot lookup by ticket

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Singleton | ParkingLot (naive only) | Ensure single instance of parking lot system |
| Strategy | PricingStrategy | Allow swappable pricing algorithms |
| Factory | ParkingSpotFactory → DefaultParkingSpotFactory | Encapsulate ParkingSpot construction (spotId scheme, batching by size) so ParkingLevel doesn't `new ParkingSpot(...)` directly |
| Observer | Spot availability | Notify displays/systems when spots change |

## Folder Structure
```
01-parking-lot/
├── naive/          <- Start here. Simple, correct, but O(n) lookups.
│   ├── model/      -> Data classes (Vehicle, ParkingSpot, Ticket)
│   ├── service/    -> Business logic (ParkingLot, ParkingLevel, PaymentProcessor)
│   ├── strategy/   -> Swappable algorithms (PricingStrategy implementations)
│   └── Main.java   -> Entry point — run this
└── optimized/      <- Production-grade. O(1) lookups, lock-free concurrency.
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
| Find available spot | O(n) linear scan | O(1) queue poll |
| Get available count | O(n) count every time | O(1) atomic counter |
| Thread safety | synchronized methods | CAS + ConcurrentLinkedQueue |
| ParkingLot creation | Singleton (hard to test) | Regular class (injectable) |

---

## Class Diagram (Text)
```
ParkingLot
├── ParkingLevel[]
│   └── ParkingSpot[] (SMALL, MEDIUM, LARGE)
├── PricingStrategy (interface)
│   ├── HourlyPricing
│   ├── FlatRatePricing
│   └── VehicleTypePricing
├── ParkingObserver (interface)
│   └── DisplayBoard (concrete implementation)
└── PaymentProcessor

Vehicle (abstract)
├── Motorcycle
├── Car
└── Truck

Ticket -> Vehicle + ParkingSpot + entryTime
```

## Observer Pattern — How ParkingObserver Works

The Observer pattern decouples "something happened" from "who needs to know about it."

```
┌─────────────┐         notifies          ┌─────────────────┐
│  ParkingLot │ ─────────────────────────▶ │  ParkingObserver │ (interface)
│  (Subject)  │   "spot X is now free"     │                  │
└─────────────┘                            └────────┬─────────┘
                                                    │ implements
                                           ┌────────▼─────────┐
                                           │   DisplayBoard    │ (concrete class)
                                           │   prints status   │
                                           └──────────────────┘
```

**The flow:**
1. `ParkingObserver` is an **interface** — it defines WHAT observers must do (handle spot changes)
2. `DisplayBoard` is a **class that implements** the interface — it defines HOW to react (print to console)
3. `ParkingLot` keeps a list of observers and calls them whenever a spot is parked/unparked

```java
// 1. Interface = the contract
interface ParkingObserver {
    void onSpotAvailabilityChanged(ParkingSpot spot, boolean available);
}

// 2. Concrete class = fulfills the contract
class DisplayBoard implements ParkingObserver {
    @Override
    public void onSpotAvailabilityChanged(ParkingSpot spot, boolean available) {
        System.out.println(spot.getSpotId() + " is now " + (available ? "FREE" : "TAKEN"));
    }
}

// 3. Usage in Main.java
lot.addObserver(new DisplayBoard("Main Entrance"));  // register it
// Now whenever a car parks/unparks, DisplayBoard.onSpotAvailabilityChanged() is called automatically
```

**Why not just print directly inside ParkingLot?** Because tomorrow you might want to send an SMS, update a mobile app, or log to a database — you just create a new class like `SMSNotifier implements ParkingObserver` without touching ParkingLot at all. Open/Closed Principle.

## Key Design Decisions
- Vehicle-to-spot mapping: Motorcycle->SMALL, Car->MEDIUM, Truck->LARGE (no fitting larger vehicles in smaller spots for simplicity)
- Ticket stores entry time; fee calculated at exit based on duration
- PricingStrategy is injected into PaymentProcessor, allowing runtime strategy changes
- ParkingLevel handles its own spot allocation (Factory-like logic internally)

## Interview Tips
- Start with requirements clarification: vehicle types, pricing model, concurrency needs
- Draw class diagram first showing relationships
- Discuss trade-offs: Should a car fit in a LARGE spot? (flexibility vs. utilization)
- Mention concurrency: synchronized entry/exit or lock-per-spot
- Talk about extensibility: new vehicle types, new pricing strategies
- Consider database persistence for production systems

---

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** Two vehicles trying to park in the last available spot simultaneously.

```bash
cd concurrent
mkdir -p out
javac -d out model/*.java service/*.java Main.java RaceConditionDemo.java
java -cp out Main
java -cp out RaceConditionDemo    # Shows the bug + fix
```

### Key Concurrency Techniques Used
| Technique | Where | Why |
|-----------|-------|-----|
| AtomicReference (CAS) | ParkingSpot.park() | Lock-free single-spot assignment — only one thread wins |
| ConcurrentLinkedQueue | ParkingLevel | Thread-safe O(1) spot distribution without locks |
| AtomicInteger | ParkingLevel counters | Lock-free available spot counting |
| ConcurrentHashMap | ParkingLot.activeTickets | Thread-safe ticket storage |
| CopyOnWriteArrayList | ParkingLot.observers | Safe iteration during notification |
| CountDownLatch | Main/RaceConditionDemo | Synchronize thread start for maximum contention |

---

## Why CAS Instead of Synchronized?

### 1. The Race Condition: Check-Then-Act

The core problem is that **checking** if a spot is free and **claiming** it are two separate steps. Any gap between them is a bug:

```java
// BROKEN — race window between check and act
if (spot.getVehicle() == null) {   // Thread A checks: empty!
    // <-- Thread B also sees empty, sneaks in here
    spot.setVehicle(myCar);        // Both threads "succeed" — double-parked!
}
```

CAS eliminates this by making check+act **a single CPU instruction** (`LOCK CMPXCHG` on x86):

```java
// CORRECT — one atomic hardware operation
boolean parked = spot.compareAndSet(null, myCar);
// If null → set to myCar, return true  (I win)
// If not null → do nothing, return false (someone else won)
// There is NO window between the check and the act.
```

### 2. Performance Under Contention

| Scenario | CAS (AtomicReference) | synchronized |
|----------|----------------------|--------------|
| Uncontended (1 thread) | ~10 ns | ~10-20 ns |
| Moderate (2-4 threads) | ~20-80 ns | **1,000-10,000 ns** |
| Heavy (16+ threads) | Degrades (spinning) | Threads sleep (better for very long waits) |

Why the gap at moderate contention? `synchronized` escalates to a **heavyweight lock**: the JVM calls into the OS kernel (`futex`), context-switches the losing thread off the CPU (~2,000-10,000 ns), and later wakes it back up. CAS just retries immediately in user space — no kernel, no sleep, no context switch.

**Parking lot reality:** A spot claim is ~10ns of work. Paying 50,000ns of lock overhead for 10ns of work is a 5000x tax.

### 3. Per-Spot Parallelism

With CAS, each `ParkingSpot` has its own independent `AtomicReference`. Two cars parking in **different** spots have **zero interference**:

```java
// CAS: spots are independent — maximum parallelism
spots[5].compareAndSet(null, carA);   // Thread A — touches only spot 5
spots[12].compareAndSet(null, carB);  // Thread B — touches only spot 12
// These two operations run in TRUE parallel. No shared lock.
```

With a single `synchronized` block, all threads serialize through one lock — even when they want completely unrelated spots:

```java
// synchronized: ONE lock for everything — artificial bottleneck
synchronized(parkingLevel) {         // Thread B waits here...
    spots[5].setVehicle(carA);       // ...even though it wants spot 12
}
```

### 4. When to Use Which — Decision Guide

```
Is the operation a single pointer/value swap?
  YES → Use CAS (AtomicReference, AtomicInteger)

Do you need to wait/block until a condition is true?
  YES → Use synchronized + wait/notify (or Lock + Condition)

Must multiple fields update atomically together?
  YES → Use synchronized (or explicit Lock)

Is contention extreme (100+ threads on one variable)?
  YES → Use LongAdder (striped CAS) or synchronized
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

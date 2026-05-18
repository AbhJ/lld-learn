# Car Rental System


## Problem Statement
Design a car rental system that allows customers to browse available vehicles, make reservations, and handle billing. The system supports different vehicle types (Car, SUV, Van, Truck) with type-specific pricing strategies.

Customers can search for vehicles by type and location, make reservations using a builder pattern for flexible booking configuration, add optional insurance, and receive detailed bills. The system tracks vehicle availability and notifies interested customers when vehicles become available.

The pricing varies based on duration (daily, weekly) and vehicle type. Insurance options can be added to any reservation.

## Requirements
### Functional Requirements
- Register and manage different vehicle types
- Search available vehicles by type, location, and date range
- Create reservations with pickup/dropoff locations
- Multiple pricing strategies (daily, weekly, weekend)
- Optional insurance add-ons
- Generate itemized bills
- Track reservation status

### Non-functional Requirements
- Prevent double-booking of vehicles
- Extensible pricing model
- Clean separation of concerns

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Strategy | Pricing per vehicle/duration | Different pricing algorithms without modifying vehicle classes |
| Factory | Vehicle creation | Centralized vehicle instantiation |
| Observer | Availability notification | Notify customers when vehicles become available |
| Builder | Reservation creation | Complex object with many optional parameters |

## Folder Structure
```
12-car-rental-system/
├── naive/
│   ├── model/      -> Vehicle, Customer, Location, Insurance, Reservation, Bill
│   ├── service/    -> RentalSystem
│   ├── strategy/   -> PricingStrategy (Daily, Weekly, Weekend)
│   └── Main.java
└── optimized/
    ├── model/
    ├── service/    -> RentalSystem with indexed HashMap<Type, PriorityQueue>
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

### Naive vs Optimized
| Operation | Naive | Optimized |
|-----------|-------|-----------|
| Search by type | O(n) linear scan all vehicles | O(1) HashMap type lookup |
| Find cheapest | O(n) scan + compare | O(1) PriorityQueue.peek() |
| Search by type+location | O(n) filter both | O(1) nested HashMap lookup |
| Re-index after return | N/A (re-scans) | O(log n) PriorityQueue.offer() |

---

## Class Diagram (Text)
```
RentalSystem (Controller)
 ├── Vehicle (Abstract)
 │    ├── Car
 │    ├── SUV
 │    ├── Van
 │    └── Truck
 ├── Customer
 ├── Reservation (Builder)
 │    └── Insurance
 ├── Bill
 ├── PricingStrategy (Interface)
 │    ├── DailyPricing
 │    ├── WeeklyPricing
 │    └── WeekendPricing
 └── Location
```

## How to Compile and Run
```bash
cd problems/12-car-rental-system
mkdir -p out
javac -d out *.java
java -cp out Main
```

## Expected Output
```
=== Car Rental System Demo ===
Vehicles registered. Searching for available SUVs...
Found: Toyota RAV4 (SUV) - $75.00/day
Reservation created for Alice: Toyota RAV4, 5 days
Bill total: $375.00 + Insurance: $50.00 = $425.00
Vehicle returned. Notifying waitlisted customers...
```

## Key Design Decisions
- Builder pattern for Reservation allows flexible configuration
- Strategy pattern makes pricing extensible without modifying core classes
- Observer pattern decouples availability tracking from notification logic
- Factory centralizes vehicle creation logic

## Interview Tips
- Emphasize how Strategy pattern handles varying pricing rules
- Show the Builder pattern for complex reservation objects
- Discuss how to handle concurrent reservations (double-booking prevention)
- Mention how the system could be extended with loyalty programs, fleet management

---

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** Two customers reserving the last available car of a type simultaneously.

```bash
cd concurrent
mkdir -p out
find . -name '*.java' | xargs javac -d out
java -cp out Main
```

### Key Concurrency Techniques Used
| Technique | Where | Why |
|-----------|-------|-----|
| ConcurrentHashMap | RentalService.fleet | Thread-safe mapping of VehicleType to available queue |
| ConcurrentLinkedQueue.poll() | RentalService.reserve() | Atomic dequeue — only one customer gets each car |
| computeIfAbsent | Fleet initialization | Thread-safe lazy queue creation per vehicle type |
| AtomicLong | Reservation ID generation | Unique IDs without synchronization |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

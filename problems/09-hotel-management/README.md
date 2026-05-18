# Hotel Management System


## Problem Statement
Design a hotel booking system that manages room inventory, guest check-in/check-out, billing, and room service. The hotel has different room types (Single, Double, Suite) with varying prices based on seasonal and weekend pricing strategies.

Rooms transition through states: AVAILABLE, BOOKED, OCCUPIED, MAINTENANCE. The system handles reservations, generates bills including room charges and room service orders, and uses factory pattern for room creation.

## Requirements

### Functional Requirements
- Multiple room types: Single, Double, Suite with base prices
- Room state management: AVAILABLE -> BOOKED -> OCCUPIED -> AVAILABLE
- Guest registration and booking management
- Check-in and check-out operations
- Bill generation with room charges and room service
- Seasonal and weekend pricing strategies
- Room service orders during stay
- Maintenance mode for rooms

### Non-functional Requirements
- Strategy pattern for dynamic pricing
- State pattern for room lifecycle
- Factory pattern for room creation
- Observer pattern for booking confirmations

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Strategy | PricingStrategy | Seasonal and weekend pricing without code changes |
| State | RoomState | Manage room lifecycle transitions |
| Observer | Booking confirmation | Notify guests of booking status |
| Factory | Room creation | Create different room types uniformly |

## Folder Structure
```
09-hotel-management/
├── naive/          <- Start here. Linear scan for room availability.
│   ├── model/      -> Data classes (Room, Guest, Booking, Bill, RoomFactory)
│   ├── service/    -> Business logic (RoomService, Hotel)
│   ├── strategy/   -> Swappable algorithms (PricingStrategy)
│   └── Main.java   -> Entry point — run this
└── optimized/      <- Production-grade. O(1) room lookup by type.
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
| Find available room | O(n) linear scan of all rooms | O(1) poll from availableByType set |
| Room type filtering | Iterate + filter per request | Map<String, Set<Room>> pre-indexed |
| Index maintenance | None | Auto-update on book/checkout/maintenance |
| Search by type | Scan all rooms every time | Direct set lookup by room type key |

---

## Class Diagram (Text)
```
Hotel (Facade)
├── Room (abstract)
│   ├── SingleRoom (base price $100)
│   ├── DoubleRoom (base price $150)
│   └── Suite (base price $300)
├── RoomState (AVAILABLE, BOOKED, OCCUPIED, MAINTENANCE)
├── Guest (name, contact)
├── Booking (guest, room, dates)
├── Bill (room charges + room service)
├── PricingStrategy (interface)
│   ├── SeasonalPricing
│   └── WeekendPricing
└── RoomService (food/service orders)
```

## Key Design Decisions
- Room state transitions are validated (can't check into MAINTENANCE room)
- PricingStrategy applied per night (weekend nights get surcharge)
- Bill aggregates room charges + room service items
- Booking holds dates; actual stay tracked on check-in/check-out

## Interview Tips
- Start with entities: Room, Guest, Booking, Bill
- Discuss state machine: valid transitions, who triggers them
- Talk about pricing: composite strategies, time-based pricing
- Consider concurrency: two guests trying to book same room
- Extensions: loyalty programs, cancellation policies, overbooking

---

## Concurrency Version

**Race condition:** Two guests booking the last available room for the same date — both see room available, both succeed, resulting in double-booking.

```bash
cd concurrent
mkdir -p out
find . -name '*.java' | xargs javac -d out
java -cp out Main
```

### Key Concurrency Techniques
| Technique | Where | Why |
|-----------|-------|-----|
| ReentrantLock per room | Room.bookingLock | Fine-grained locking — guests can book different rooms concurrently |
| ConcurrentHashMap | HotelService.bookingsByDate | Thread-safe date-indexed booking storage without global lock |
| Atomic tryBook() | Room.tryBook() | Lock + check-then-act pattern ensures only one guest books each room |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

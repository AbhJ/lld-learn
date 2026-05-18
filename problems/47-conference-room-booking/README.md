# Conference Room Booking


## Problem Statement
Design a conference room booking system that finds a room satisfying capacity and amenity requirements for a given time slot, books it (rejecting conflicts), and supports recurring bookings.

The naive variant scans all rooms and checks each for conflicts. The optimized variant uses a TreeMap of bookings per room (for O(log n) conflict checks) and a capacity-indexed TreeMap for room search.

## Requirements

### Functional Requirements
- Register rooms with capacity, floor, and amenities
- Find a room matching capacity + amenity needs for a time slot
- Book a room (organizer, meeting type, title)
- Reject conflicting bookings
- Recurring bookings (daily, weekly) with occurrence count
- Cancel a booking by ID

### Non-functional Requirements
- O(log n) conflict detection per room (optimized)
- Smallest-fitting-room heuristic to maximize availability

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Strategy | RoomFinder (SmallestFitting, capacity-indexed) | Swappable room selection algorithm |
| Facade | BookingSystem | Unified API over rooms, conflicts, recurring bookings |
| Composite | `BookingComponent` interface implemented by `Booking` (leaf) and `RecurringBooking` (composite) | Callers (e.g. `BookingSystem.cancelBooking`) treat a single booking and a whole series uniformly via `getId()`, `getTimeSlots()`, `cancel()` |
| Repository | BookingSystem room/booking maps | Central lookup for rooms and bookings |

## Folder Structure

```
47-conference-room-booking/
├── README.md
├── VARIATIONS.md
├── naive/
│   ├── model/        ← Room, Booking, User, TimeSlot, MeetingType, RecurringBooking
│   ├── service/      ← BookingSystem, ConflictDetector
│   ├── strategy/     ← RoomFinder (SmallestFitting)
│   └── Main.java
└── optimized/
    ├── model/        ← Room, Booking, User, TimeSlot, MeetingType, RecurringBooking
    ├── service/      ← BookingSystem, ConflictDetector (TreeMap per room)
    ├── strategy/     ← RoomFinder (capacity-indexed TreeMap)
    └── Main.java
```

## How to Run

```bash
# Naive
cd problems/47-conference-room-booking/naive
mkdir -p out && javac -d out model/*.java strategy/*.java service/*.java Main.java && java -cp out Main

# Optimized
cd problems/47-conference-room-booking/optimized
mkdir -p out && javac -d out model/*.java strategy/*.java service/*.java Main.java && java -cp out Main
```

## Naive vs Optimized

| Aspect | Naive | Optimized |
|--------|-------|-----------|
| Conflict check | O(n*m) scan all rooms x all bookings | O(log n) TreeMap per room with floor/ceiling |
| Room finding | O(rooms) linear scan | TreeMap<capacity> tailMap for O(log n) start |
| Cancel booking | Linear search | TreeMap removal O(log n) |
| Scalability | Degrades with booking count | Scales logarithmically per room |

---

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** Two meetings booked in same room at overlapping times.

```bash
cd concurrent
mkdir -p out
find . -name '*.java' | xargs javac -d out
java -cp out Main
```

### Key Concurrency Techniques Used
| Technique | Where | Why |
|-----------|-------|-----|
| ConcurrentSkipListMap<TimeSlot, Booking> | RoomBookingService (per room) | Sorted concurrent map for efficient overlap queries |
| ReentrantLock per room | RoomBookingService.roomLocks | Atomic conflict-check-then-book — no TOCTOU gap |
| Per-room isolation | Separate lock + map per room | Different rooms don't contend — high parallelism |
| Comparable TimeSlot | TimeSlot.compareTo() | Natural ordering enables efficient range queries in skip list |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

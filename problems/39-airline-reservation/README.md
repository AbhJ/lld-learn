# Airline Reservation System


## Problem Statement
Design an airline reservation system that manages flights and seats across multiple cabin classes (economy, business, first), supports search by route, and handles booking, check-in, and cancellation.

The naive variant scans seat lists linearly. The optimized variant represents seat availability as BitSets per class for O(1) availability checks.

## Requirements

### Functional Requirements
- Register flights with seat counts per class
- Search flights by origin and destination
- Book a passenger into a specific class
- Cancel a booking and free the seat
- Check in a confirmed passenger
- Reject booking when no seats remain

### Non-functional Requirements
- O(1) seat availability lookup (optimized)
- Bounded memory per flight (BitSet vs full seat objects)
- Stable booking IDs

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Facade | AirlineSystem | Unified API over flights, seats, bookings |
| Repository | AirlineSystem flight registry | Central lookup for flights by ID/route |
| Bit Set | SeatMap (optimized) | Compact, fast seat availability tracking |

## Folder Structure

```
39-airline-reservation/
├── README.md
├── VARIATIONS.md
├── naive/
│   ├── model/        ← Seat, Flight, Passenger, Booking
│   ├── service/      ← AirlineSystem (linear seat search)
│   └── Main.java
└── optimized/
    ├── model/        ← Passenger, Booking
    ├── service/      ← SeatMap (BitSet), Flight, AirlineSystem
    └── Main.java
```

## How to Run

```bash
# Naive
cd naive && mkdir -p out && javac -d out model/*.java service/*.java Main.java && java -cp out Main

# Optimized
cd optimized && mkdir -p out && javac -d out model/*.java service/*.java Main.java && java -cp out Main
```

## Naive vs Optimized

| Aspect | Naive | Optimized |
|--------|-------|-----------|
| Seat storage | `List<Seat>` objects (heavy) | `BitSet` — 1 bit per seat (64x less memory) |
| Find available | O(n) linear scan through Seat list | `BitSet.nextClearBit()` — O(n/64) word scan |
| Book/release | O(n) search + boolean flip | O(1) `BitSet.set()`/`clear()` by index |
| Memory (200 seats) | 200 Seat objects with fields | 4 longs (32 bytes) |
| Class lookup | Scan all seats, filter by class | Pre-computed class boundaries for range scan |
| Availability count | O(n) loop | `BitSet` range scan with cardinality |

---

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** Multiple passengers selecting same seat on same flight simultaneously.

```bash
cd concurrent
mkdir -p out
find . -name '*.java' | xargs javac -d out
java -cp out Main
```

### Key Concurrency Techniques Used
| Technique | Where | Why |
|-----------|-------|-----|
| AtomicReferenceArray | FlightSeatMap.seats | One AtomicRef per seat for independent CAS |
| compareAndSet(null, passenger) | bookSeat() | Atomic claim — only first passenger wins |
| Lock-free scan | bookAnySeat() | CAS on each empty seat until one succeeds |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

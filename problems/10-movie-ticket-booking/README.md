# Movie Ticket Booking System


## Problem Statement
Design a movie ticket booking system (similar to BookMyShow) that allows users to browse movies, select shows, choose seats, and make bookings. The system handles concurrent booking scenarios using seat locks to prevent double-booking.

The system supports different seat types (Regular, Premium, VIP) with different pricing, multiple screens with different seat configurations, and show timings. When a user starts selecting seats, a temporary lock is placed to prevent other users from booking the same seats during the selection window.

## Requirements

### Functional Requirements
- Browse available movies and their show timings
- View seat availability for a show
- Select seats (multiple types: Regular, Premium, VIP)
- Temporary seat lock during selection (expires after timeout)
- Book selected seats with payment
- Cancel booking
- Handle concurrent booking attempts gracefully
- Different pricing per seat type

### Non-functional Requirements
- Thread-safe seat locking for concurrent bookings
- Strategy pattern for seat pricing
- Factory pattern for seat type creation
- Observer pattern for booking confirmations
- Lock timeout to prevent indefinite holds

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Strategy | Pricing per seat type | Different pricing for Regular/Premium/VIP |
| Observer | Booking notification | Confirm booking to user |
| Factory | Seat creation | Create different seat types uniformly |
| Lock | SeatLock | Temporary hold during seat selection |

## Folder Structure
```
10-movie-ticket-booking/
├── naive/          <- Start here. Synchronized blocks for seat management.
│   ├── model/      -> Data classes (Movie, Seat, Screen, Show, Payment, Booking)
│   ├── service/    -> Business logic (SeatLockManager, BookingSystem)
│   └── Main.java   -> Entry point — run this
└── optimized/      <- Production-grade. ReentrantLock per show + ConcurrentHashMap.
    ├── model/
    ├── service/
    └── Main.java
```

### How to Run
```bash
# Run the naive (simple) version:
cd naive
mkdir -p out
javac -d out model/*.java service/*.java Main.java
java -cp out Main

# Run the optimized version:
cd optimized
mkdir -p out
javac -d out model/*.java service/*.java Main.java
java -cp out Main
```

### Naive vs Optimized — What Changes?
| Operation | Naive | Optimized |
|-----------|-------|-----------|
| Seat status store | HashMap + synchronized | ConcurrentHashMap (lock-free reads) |
| Seat locking | Global synchronized lock manager | Per-show ReentrantLock + ConcurrentHashMap |
| Booking confirmation | synchronized on Show object | ReentrantLock.tryBookSeats() per show |
| Available count | O(n) count each time | AtomicInteger maintained on each status change |
| Show lookup | Linear scan of list | ConcurrentHashMap<showId, Show> O(1) |
| Counters | static int with reset | AtomicInteger (thread-safe) |

---

## Class Diagram (Text)
```
BookingSystem (Controller)
├── Movie (title, genre, duration)
├── Show (movie, screen, timing)
├── Screen (name, seats layout)
├── Seat (abstract)
│   ├── RegularSeat
│   ├── PremiumSeat
│   └── VIPSeat
├── Booking (show, seats, user, payment status)
├── Payment (amount, status)
└── SeatLock (seat, user, expiry time)
```

## Key Design Decisions
- SeatLock has expiry time; expired locks are automatically released
- Seats are locked per-show (same physical seat can be available for different shows)
- Pricing is per seat type (Regular < Premium < VIP)
- Payment is processed before finalizing booking
- Thread-safe operations on seat status using synchronized blocks (naive) or ReentrantLock (optimized)

## Interview Tips
- Start with entity identification: Movie, Show, Screen, Seat, Booking
- Discuss concurrency: how to handle two users selecting the same seat
- Talk about seat lock expiry: prevent indefinite holds
- Consider scalability: database-level locks in production
- Mention payment failure handling: release locks, rollback
- Extension: discount codes, food combos, multiple cinema chains

---

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** Two users booking the same seat at the same time.

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
| AtomicReference (CAS) | Seat.tryLock() | Only one user can transition AVAILABLE -> LOCKED |
| ReentrantLock per Show | BookingService | Serialize bookings within same show, parallel across shows |
| ConcurrentHashMap | BookingService.shows | Thread-safe show/booking storage |
| Rollback pattern | BookingService.bookSeats() | Release locked seats if any seat in batch fails |
| CountDownLatch | Main | All 50 users start simultaneously for maximum race pressure |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

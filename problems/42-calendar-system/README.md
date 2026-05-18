# Calendar System


## Problem Statement
Design a calendar system where users own calendars and create events with start/end times, organizers, invitees, reminders, and recurrence rules. The system detects scheduling conflicts and expands recurring events into concrete occurrences.

The naive variant scans all events for conflicts. The optimized variant indexes events in a TreeMap keyed by start time for log-time conflict detection.

## Requirements

### Functional Requirements
- Create per-user calendars
- Add events with title, time range, organizer, reminders
- Recurring events (daily, weekly, monthly)
- Detect time conflicts within a calendar
- Send invitations to other users
- Builder API for event construction

### Non-functional Requirements
- O(log n) conflict detection (optimized)
- Recurrence expansion is on-demand, not stored

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Builder | EventBuilder | Fluent construction of events with many optional fields |
| Strategy | RecurrenceRule (Daily, Weekly, Monthly) | Pluggable recurrence expansion |
| Facade | CalendarService | Unified API over calendars, events, conflicts |
| Repository | Calendar | Owns and indexes a user's events |

## Folder Structure

```
42-calendar-system/
├── README.md
├── VARIATIONS.md
├── naive/
│   ├── model/        ← TimeSlot, Event, Invitation, Reminder
│   ├── service/      ← CalendarService, Calendar, ConflictDetector, EventBuilder
│   ├── strategy/     ← RecurrenceRule (Daily, Weekly, Monthly)
│   └── Main.java
└── optimized/
    ├── model/        ← TimeSlot (Comparable), Event, Invitation, Reminder
    ├── service/      ← CalendarService, Calendar (TreeMap), ConflictDetector (TreeMap)
    ├── strategy/     ← RecurrenceRule
    └── Main.java
```

## How to Run

```bash
# Naive
cd problems/42-calendar-system/naive
mkdir -p out && javac -d out model/*.java strategy/*.java service/*.java Main.java && java -cp out Main

# Optimized
cd problems/42-calendar-system/optimized
mkdir -p out && javac -d out model/*.java strategy/*.java service/*.java Main.java && java -cp out Main
```

## Naive vs Optimized

| Aspect | Naive | Optimized |
|--------|-------|-----------|
| Conflict detection | O(n) linear scan of all events | O(log n) via TreeMap floor/ceiling lookup |
| Find all conflicts | O(n^2) pairwise comparison | O(n) adjacent-pair check in sorted order |
| Events for date | O(n) filter all events | O(log n) range query via TreeMap.subMap |
| Event indexing | Unsorted ArrayList | TreeMap keyed by start time |

---

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** Two people booking overlapping time slots in same room/calendar simultaneously.

```bash
cd concurrent
mkdir -p out
find . -name '*.java' | xargs javac -d out
java -cp out Main
```

### Key Concurrency Techniques Used
| Technique | Where | Why |
|-----------|-------|-----|
| ReentrantLock | CalendarService.lock | Atomic overlap-check-then-book — no gap between check and insert |
| TreeMap<StartTime, Event> | CalendarService.events | Efficient range queries for overlap detection under lock |
| CountDownLatch | Main (startLatch) | All 10 booking threads fire simultaneously for maximum contention |
| Immutable Event | Event class | Thread-safe by construction — created before lock, inserted atomically |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

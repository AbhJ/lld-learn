# Event Bus


## Problem Statement
Design an in-process event bus where publishers post typed events and subscribers register handlers for the event types they care about. Handlers can be filtered and prioritized; events with no matching subscriber land in a dead-letter queue.

The naive variant dispatches synchronously over an ArrayList. The optimized variant uses a CopyOnWriteArrayList per type and dispatches asynchronously via CompletableFuture.

## Requirements

### Functional Requirements
- Subscribe handlers to specific event types (including a base Event type)
- Publish events to all matching handlers
- Handler priority ordering
- Per-subscription filter predicate
- Dead-letter queue for events with no handler
- Asynchronous dispatch (optimized)

### Non-functional Requirements
- Decoupled publishers and subscribers
- Safe iteration during concurrent modification (optimized)
- A slow handler should not block other handlers (optimized)

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Observer / Pub-Sub | EventBus + EventHandler | Subscribers react to published events without coupling |
| Strategy | EventFilter | Per-subscription predicate decides which events are delivered |
| Dead Letter Channel | DeadLetterQueue | Capture undeliverable events for later inspection |

## Folder Structure

```
33-event-bus/
├── README.md
├── VARIATIONS.md
├── naive/
│   ├── model/        ← Event, UserEvent, SystemEvent, Subscription, EventHandler
│   ├── service/      ← EventBus, DeadLetterQueue
│   ├── strategy/     ← EventFilter
│   └── Main.java
└── optimized/
    ├── model/        ← Event, UserEvent, SystemEvent, EventHandler
    ├── service/      ← EventBus (CopyOnWriteArrayList + CompletableFuture)
    └── Main.java
```

## How to Run

```bash
# Naive
cd naive && mkdir -p out && javac -d out model/*.java strategy/*.java service/*.java Main.java && java -cp out Main

# Optimized
cd optimized && mkdir -p out && javac -d out model/*.java service/*.java Main.java && java -cp out Main
```

## Naive vs Optimized

| Aspect | Naive | Optimized |
|--------|-------|-----------|
| Handler storage | Single `ArrayList` scanned for every publish | `CopyOnWriteArrayList` per event type (O(1) type lookup) |
| Dispatch | Synchronous on publisher thread | Async via `CompletableFuture` + thread pool |
| Type safety | Raw `EventHandler` interface | Generic `EventHandler<T extends Event>` |
| Subscribe concurrency | Not thread-safe | `ConcurrentHashMap` + `CopyOnWriteArrayList` |
| Slow handler impact | Blocks publisher and other handlers | Isolated — each runs independently on executor |
| Complexity | Simple, single-threaded model | More complex but production-ready |

---

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** Event published while handler is being registered — handler misses the event or gets partial event.

```bash
cd concurrent
mkdir -p out
find . -name '*.java' | xargs javac -d out
java -cp out Main
```

### Key Concurrency Techniques Used
| Technique | Where | Why |
|-----------|-------|-----|
| CopyOnWriteArrayList | Handler list per topic | Snapshot iteration — no ConcurrentModificationException |
| ConcurrentHashMap | Topic-to-handlers map | Thread-safe topic registry |
| computeIfAbsent | subscribe() | Atomic handler list initialization |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

# Producer-Consumer


## Problem Statement
Design a bounded buffer that synchronizes multiple producer threads handing off items to multiple consumer threads. Producers block when the buffer is full; consumers block when it is empty.

The naive variant uses a synchronized buffer with wait/notify. The optimized variant uses a lock-free ring buffer and supports batch produce/consume to amortize coordination overhead.

## Requirements

### Functional Requirements
- Bounded buffer with configurable capacity
- Multiple concurrent producers and consumers
- Block producers on full, consumers on empty
- Coordinator to start and await completion of worker threads
- Batch put/take operations (optimized)

### Non-functional Requirements
- Thread-safe under contention
- No busy-waiting; blocked threads consume no CPU
- Bounded memory regardless of producer rate

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Producer-Consumer | BoundedBuffer / LockFreeRingBuffer | Core decoupling pattern between producers and consumers |
| Monitor | BoundedBuffer (synchronized + wait/notify) | Coordinate access via condition variables |
| Coordinator | Coordinator | Owns lifecycle of producer/consumer threads |

## Folder Structure

```
32-producer-consumer/
├── README.md
├── VARIATIONS.md
├── naive/
│   ├── model/        ← Item
│   ├── service/      ← BoundedBuffer, Producer, Consumer, Coordinator
│   └── Main.java
└── optimized/
    ├── model/        ← Item
    ├── service/      ← LockFreeRingBuffer, BatchProducer, BatchConsumer
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
| Synchronization | `synchronized` + `wait()`/`notifyAll()` | CAS-based lock-free ring buffer |
| Buffer | Array with single monitor lock | Power-of-2 ring buffer with atomic head/tail |
| Throughput | Limited by monitor contention and context switches | Batch produce/consume amortizes CAS cost |
| Backpressure | Blocking `wait()` (thread parks) | Spin-yield (lower latency at cost of CPU) |
| False sharing | N/A (single lock) | Separate atomic head/tail avoid cache-line contention |
| Complexity | Simple, correct, easy to debug | Higher complexity but orders-of-magnitude faster |

---

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** Classic — producer writes to full buffer, consumer reads from empty buffer.

```bash
cd concurrent
mkdir -p out
javac -d out model/*.java service/*.java Main.java
java -cp out Main
```

### Key Concurrency Techniques Used
| Technique | Where | Why |
|-----------|-------|-----|
| ReentrantLock + Condition | BoundedBuffer | await/signal for blocking when full/empty |
| ConcurrentLinkedQueue + CAS | LockFreeBuffer | Lock-free bounded buffer with backpressure |
| Condition.await/signal | BoundedBuffer.put/take | Efficient blocking — no busy-wait |
| AtomicInteger size bound | LockFreeBuffer.offer() | CAS-based capacity enforcement without locks |
| ConcurrentHashMap.newKeySet() | Main (verification) | Detect duplicates across consumer threads |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

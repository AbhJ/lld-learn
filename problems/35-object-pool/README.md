# Object Pool


## Problem Statement
Design a generic object pool that lends out reusable, expensive-to-create objects (e.g. database connections) and reclaims them on return. The pool enforces a max size, validates objects, and evicts idle entries.

The naive variant uses a synchronized ArrayList. The optimized variant uses a ConcurrentLinkedDeque, a Semaphore for capacity control, and a background evictor.

## Requirements

### Functional Requirements
- Borrow and return pooled objects
- Configurable min/max size and idle timeout
- Pluggable factory for object creation/destruction
- Validation strategy on borrow/return
- Evict idle objects past their timeout
- Throw or block on exhaustion

### Non-functional Requirements
- Thread-safe borrow/return
- Bounded resource consumption
- O(1) borrow when objects are available (optimized)

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Object Pool | ObjectPool | Reuse expensive instances rather than recreating |
| Factory | ObjectFactory | Encapsulates how pooled objects are constructed/destroyed |
| Strategy | ValidationStrategy | Swappable validation logic for pooled objects |
| Generics | ObjectPool\<T\> | Pool any type without code duplication |

## Folder Structure

```
35-object-pool/
├── README.md
├── VARIATIONS.md
├── naive/
│   ├── model/        ← PoolConfig, PooledObject, DatabaseConnection
│   ├── service/      ← ObjectPool (synchronized ArrayList)
│   ├── strategy/     ← ObjectFactory, ValidationStrategy
│   └── Main.java
└── optimized/
    ├── model/        ← PoolConfig, PooledObject, DatabaseConnection
    ├── service/      ← ObjectPool (ConcurrentLinkedDeque + Semaphore + evictor)
    ├── strategy/     ← ObjectFactory
    └── Main.java
```

## How to Run

```bash
# Naive
cd naive && mkdir -p out && javac -d out model/*.java strategy/*.java service/*.java Main.java && java -cp out Main

# Optimized
cd optimized && mkdir -p out && javac -d out model/*.java strategy/*.java service/*.java Main.java && java -cp out Main
```

## Naive vs Optimized

| Aspect | Naive | Optimized |
|--------|-------|-----------|
| Idle storage | `synchronized ArrayList` | `ConcurrentLinkedDeque` (lock-free LIFO) |
| Bounding | Exception on exhaustion | `Semaphore` blocks callers (natural backpressure) |
| Access pattern | LIFO via remove(size-1) under lock | Lock-free push/pop for cache locality |
| Eviction | Manual `evict()` call | Background daemon thread on interval |
| Concurrency | Single monitor blocks all threads | Concurrent borrow/return without global lock |
| Cache locality | Random reuse order | LIFO ensures recently-used objects stay warm |

---

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** Pool exhaustion — more threads borrow than pool capacity, last thread gets null/corrupted object.

```bash
cd concurrent
mkdir -p out
find . -name '*.java' | xargs javac -d out
java -cp out Main
```

### Key Concurrency Techniques Used
| Technique | Where | Why |
|-----------|-------|-----|
| Semaphore | ObjectPool capacity | Bounds concurrent borrows to pool size |
| ConcurrentLinkedDeque | Idle object storage | Lock-free borrow/return operations |
| AtomicBoolean | PooledObject.inUse | CAS prevents double-borrow of same object |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

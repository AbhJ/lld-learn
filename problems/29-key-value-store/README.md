# Key-Value Store


## Problem Statement
Design an in-memory key-value store similar to **Redis** or **Memcached**. The store exposes a simple API — `set(key, value)`, `get(key)`, `delete(key)` — and stores arbitrary string values keyed by string keys. Optionally, each entry may carry a **TTL (time-to-live)**: after the TTL expires, `get` should return null and the entry should be removed from memory.

The store must support **persistence** so data survives restarts. Two strategies are common: (1) **in-memory only** — data lost on restart, fastest; (2) **append-only log (AOF)** — every mutating operation is appended to disk as a `Command` object so it can be replayed at startup to reconstruct state. Long-running stores must compact the log periodically, otherwise it grows without bound.

In production, many clients read and write concurrently. The store must guarantee that **read-modify-write** sequences (e.g., `incr` — read, increment, write back) are atomic even under contention; otherwise updates can be silently lost. **Observers** (loggers, cache-invalidation hooks, replication followers) should be notified on `set`, `delete`, and TTL expiration without blocking the hot path.

## Requirements

### Functional Requirements
- `set(key, value)` and `set(key, value, ttl)`
- `get(key)` — returns null on miss or expired
- `delete(key)`
- TTL-based expiration (lazy on access + active background sweep)
- Persistence strategies: in-memory and append-only log
- Replay log on startup to restore state
- Observers notified on set / delete / expire events
- Atomic operations: `compute`, `compareAndSet`, `incr`

### Non-functional Requirements
- O(1) get/set via ConcurrentHashMap (lock-free reads)
- Background expiration via DelayQueue — O(log n) insert, no full-scan
- Atomic read-modify-write via ConcurrentHashMap.compute (no lost updates)
- Append-only log with background compaction to bound disk usage
- Observers run asynchronously — never block writers
- Extensible persistence via Strategy pattern

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Command | `Command` objects (SET / DELETE) | Replayable operations for persistence |
| Observer | `KVStoreObserver` + `LoggingKVObserver`; `KVStore.addObserver` | Notify watchers on `onSet` / `onDelete` / `onExpire` |
| Strategy | `PersistenceStrategy` + `InMemoryPersistence` / `AppendOnlyLogPersistence`; injected into `KVStore` | Pluggable persistence backends without changing store logic |

---

## Folder Structure
```
29-key-value-store/
├── README.md
├── VARIATIONS.md
├── naive/
│   ├── model/        ← Entry (key-value with TTL)
│   ├── service/      ← KVStore + KVStoreObserver/LoggingKVObserver + PersistenceStrategy/InMemory/AppendOnlyLog
│   ├── command/      ← Command (SET/DELETE for logging)
│   └── Main.java
└── optimized/
    ├── model/        ← Entry
    ├── service/      ← KVStore (ConcurrentHashMap + DelayQueue + compaction) + Observer + PersistenceStrategy
    ├── command/      ← Command
    └── Main.java
```

### How to Run
```bash
# Naive
cd problems/29-key-value-store/naive
mkdir -p out && javac -d out model/*.java command/*.java service/*.java Main.java && java -cp out Main

# Optimized
cd problems/29-key-value-store/optimized
mkdir -p out && javac -d out model/*.java command/*.java service/*.java Main.java && java -cp out Main
```

### Naive vs Optimized
| Operation | Naive | Optimized |
|-----------|-------|-----------|
| Get/Set | O(1) but not thread-safe | O(1) ConcurrentHashMap (lock-free) |
| TTL expiration | Full scan or lazy on access | DelayQueue (background thread, O(log n) add) |
| Persistence | Simple log (grows unbounded) | Append-only log + background compaction |
| Concurrency | Single-threaded only | Full concurrent access |

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** Read-modify-write on same key from multiple threads — lost updates.

```bash
cd concurrent
mkdir -p out
find . -name '*.java' | xargs javac -d out
java -cp out Main
```

### Key Concurrency Techniques Used
| Technique | Where | Why |
|-----------|-------|-----|
| ConcurrentHashMap.compute | increment() | Atomic read-modify-write — no lost updates |
| AtomicReference | Per-value CAS | Optimistic locking for compareAndSet |
| VersionedValue (immutable) | Value wrapper | Version tracking for conflict detection |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

# Connection Pool


## Problem Statement
Design a generic **connection pool** that manages a fixed-size set of expensive-to-create resources — typically database connections, but the pattern applies to HTTP clients, gRPC channels, or any object whose construction cost dwarfs its per-use cost. Threads `borrow` a connection from the pool, use it for a query, and `return` it; the same connection is then handed to the next waiting thread instead of being recreated.

The pool has a **maximum size** (e.g., 20). When all connections are in use and another thread requests one, that thread should **block** until a connection is returned, but only up to a configured **timeout** — indefinite blocking would deadlock the application. Returned connections should be **health-checked** (is the TCP connection still alive? has the DB closed it?) and replaced if broken, so callers never receive a zombie connection.

In production, hundreds of threads contend for the pool. A naive `synchronized` pool serializes all borrow/return operations and becomes the bottleneck. The pool must allow concurrent borrows, prevent the same connection from being handed to two threads simultaneously, and guarantee that connections are returned even if the borrowing thread throws an exception (try-finally).

## Requirements

### Functional Requirements
- `borrow()` — get a connection, blocking if none available (with timeout)
- `release(conn)` — return a connection to the pool
- Bounded maximum pool size — never create more than `maxSize` connections
- Initial size — pre-create `initialSize` connections at startup
- Health check on borrow / return — replace broken connections
- Background health-check thread — proactively detect dead idle connections
- Stats: total, idle, in-use, borrows, returns, timeouts
- Reject borrows after `close()` — graceful shutdown

### Non-functional Requirements
- Lock-free borrow/return via BlockingQueue (no global synchronized block)
- Fair Semaphore bounds concurrent connections — kernel-level wait, not busy-wait
- AtomicBoolean prevents the same connection being acquired twice (CAS)
- Bounded wait time on borrow — never blocks indefinitely
- Try-finally pattern guarantees release on exception
- Background ScheduledExecutor for periodic health checks (no caller-side polling)

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Object Pool | ConnectionPool | Reuse expensive resources |
| Factory | ConnectionFactory | Decouple creation from pool logic |
| Proxy | ConnectionWrapper | Auto-return on close() |

---

## Folder Structure
```
30-connection-pool/
├── README.md
├── VARIATIONS.md
├── naive/
│   ├── model/        ← Connection, PoolConfig, PoolStats
│   ├── service/      ← ConnectionPool (synchronized ArrayList)
│   └── Main.java
└── optimized/
    ├── model/        ← Connection, PoolConfig, PoolStats
    ├── service/      ← ConnectionPool (BlockingQueue + Semaphore + health check)
    └── Main.java
```

### How to Run
```bash
# Naive
cd problems/30-connection-pool/naive
mkdir -p out && javac -d out model/*.java service/*.java Main.java && java -cp out Main

# Optimized
cd problems/30-connection-pool/optimized
mkdir -p out && javac -d out model/*.java service/*.java Main.java && java -cp out Main
```

### Naive vs Optimized
| Operation | Naive | Optimized |
|-----------|-------|-----------|
| Borrow | synchronized (blocks all threads) | BlockingQueue.poll (lock-free) |
| Size limiting | Manual count in synchronized block | Semaphore (kernel-level, fair) |
| Return | synchronized notify | BlockingQueue.offer (non-blocking) |
| Health check | Manual (caller must run) | Background ScheduledExecutor thread |
| Wait for connection | busy-wait with sleep(100) | Semaphore.tryAcquire with timeout |

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** Pool exhaustion — more threads request connections than pool size.

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
| Semaphore (fair) | ConcurrentConnectionPool | Bounds max concurrent connections, fair ordering |
| BlockingQueue | ConcurrentConnectionPool.idleConnections | Threads block efficiently waiting for idle connection |
| AtomicBoolean (CAS) | Connection.acquire() | Prevents double-acquisition of same connection |
| try-finally pattern | RaceConditionDemo.FixedWorker | Guarantees connection return even on exception |
| Timeout with tryAcquire | ConcurrentConnectionPool.acquire() | Prevents indefinite blocking on exhausted pool |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

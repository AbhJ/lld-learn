# Thread Pool


## Problem Statement
Design a thread pool that accepts tasks from clients and executes them on a managed set of worker threads. The pool maintains a bounded task queue, supports a configurable core/max worker count, and applies a rejection policy when the queue is full.

The pool should track basic statistics (submitted, completed, rejected) and shut down cleanly. The optimized variant uses per-worker work-stealing deques to reduce contention.

## Requirements

### Functional Requirements
- Submit tasks for asynchronous execution
- Configure core size, max size, and queue capacity
- Pluggable rejection policies (Abort, CallerRuns, Discard)
- Track pool statistics (submitted, completed, rejected, active)
- Graceful and immediate shutdown
- Work-stealing across workers (optimized)

### Non-functional Requirements
- Thread-safe submission and execution
- Bounded memory via fixed queue capacity
- Low contention under high submission rates (optimized)

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Strategy | RejectionPolicy (Abort, CallerRuns, Discard) | Swappable behavior when queue is full |
| Producer-Consumer | TaskQueue + Worker | Decouples task submission from execution |
| Command | Task interface | Encapsulates a unit of work as an object |
| Worker Thread | Worker / StealingWorker | Long-lived threads pull work from a queue |

## Folder Structure

```
31-thread-pool/
├── README.md
├── VARIATIONS.md
├── naive/
│   ├── model/        ← PoolConfig, ThreadPoolStats, Task, CompletionCallback
│   ├── service/      ← TaskQueue, Worker, ThreadPool
│   ├── strategy/     ← RejectionPolicy (Abort, CallerRuns, Discard)
│   └── Main.java
└── optimized/
    ├── model/        ← Task, PoolConfig, PoolStats
    ├── service/      ← WorkStealingDeque, StealingWorker, WorkStealingPool
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
| Queue | Single shared `LinkedList` with `synchronized` | Per-worker `WorkStealingDeque` (lock-free) |
| Contention | All workers compete for one lock | Owner has exclusive bottom; thieves CAS top |
| Submission | `synchronized offer()` blocks under load | Round-robin push to worker deques, no lock |
| Load balance | None — fast workers idle while queue empty | Idle workers steal from busy peers |
| Throughput | Bottlenecked by single monitor | Scales linearly with core count |
| Complexity | Simple, easy to reason about | More complex but production-grade (like ForkJoinPool) |

---

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** Task submission during shutdown, rejected execution handling.

```bash
cd concurrent
mkdir -p out
javac -d out model/*.java service/*.java Main.java
java -cp out Main
```

### Key Concurrency Techniques Used
| Technique | Where | Why |
|-----------|-------|-----|
| volatile shutdown flag | ConcurrentThreadPool | Visibility of shutdown state across all worker threads |
| CountDownLatch | ConcurrentThreadPool.terminationLatch | Wait for all workers to finish gracefully |
| LinkedBlockingQueue | ConcurrentThreadPool.taskQueue | Thread-safe bounded task submission |
| RejectionPolicy (Strategy) | AbortPolicy, CallerRunsPolicy, DiscardPolicy | Configurable behavior for rejected tasks |
| Daemon threads | Worker threads | Don't prevent JVM exit if main thread finishes |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

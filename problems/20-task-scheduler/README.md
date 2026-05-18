# Task Scheduler


## Problem Statement
Design a task scheduler that supports priority-based execution, delayed tasks, recurring tasks, and dependency resolution between tasks. The scheduler manages a pool of workers that execute tasks based on priority and readiness.

Tasks can be one-shot, delayed (execute after a specified delay), or recurring (execute at regular intervals). Tasks may depend on other tasks, forming a DAG (Directed Acyclic Graph). A task with unmet dependencies cannot execute until all dependencies complete successfully.

The scheduler uses a priority queue to determine execution order and notifies observers when tasks complete.

## Requirements
### Functional Requirements
- Submit tasks with different priorities
- Delayed task execution
- Recurring task execution
- Task dependency resolution (DAG)
- Task cancellation
- Execution result tracking
- Worker pool management

### Non-functional Requirements
- Priority-based scheduling
- No circular dependency acceptance
- Thread-safe task submission
- Observable task completion

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Strategy | `SchedulingStrategy` (`PrioritySchedulingStrategy`, `FifoSchedulingStrategy`) injected into `Scheduler` | Pluggable policy for picking the next ready task; default is priority-based, swap to FIFO for fairness demos |
| Command | `Task` subclasses (`SimpleTask`, `RecurringTask`, `DelayedTask`) executed by `Scheduler`/`Worker` | Encapsulate task logic as objects so the scheduler can queue, cancel, and dispatch them uniformly |
| Observer | `TaskCompletionListener` (`LoggingTaskListener`); `Scheduler.addListener` fires `onTaskCompleted` / `onTaskFailed` | Decouple completion/failure notifications from the scheduler; listeners can log, alert, or feed metrics |
| Priority Queue | `Scheduler` ready queue ordering (naive `PriorityQueue`, optimized `PriorityBlockingQueue`) | Efficient priority-based retrieval when the priority strategy is in use |

## Folder Structure
```
20-task-scheduler/
├── naive/
│   ├── model/      -> Task, TaskPriority, TaskResult, TaskDependency, DelayedTask, RecurringTask
│   ├── service/    -> Scheduler, Worker
│   └── Main.java
└── optimized/
    ├── model/      -> Task, TaskPriority, TaskResult, DAG (topological sort)
    ├── service/    -> Scheduler (PriorityBlockingQueue, event-driven)
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

### Naive vs Optimized
| Operation | Naive | Optimized |
|-----------|-------|-----------|
| Ready check | Poll all pending tasks O(n) | Event-driven (DAG signals unblocked) |
| Dependency resolution | Manual Set tracking | DAG with topological sort |
| Cycle detection | BFS per add O(V+E) | BFS per add O(V+E) (same) |
| Task ordering | PriorityQueue | PriorityBlockingQueue (concurrent) |

---

## Class Diagram (Text)
```
Scheduler (Main)
 ├── Task (Command)
 │    ├── RecurringTask
 │    └── DelayedTask
 ├── TaskPriority (Enum)
 ├── TaskDependency (DAG)
 ├── Worker (Executor)
 └── TaskResult
```

## How to Compile and Run
```bash
cd problems/20-task-scheduler
mkdir -p out
javac -d out *.java
java -cp out Main
```

## Expected Output
```
=== Task Scheduler Demo ===
Task "ProcessPayment" [HIGH] submitted.
Task "SendEmail" [LOW] depends on "ProcessPayment".
Worker-1 executing: ProcessPayment... COMPLETED
Dependency met. Scheduling: SendEmail
Worker-2 executing: SendEmail... COMPLETED
Recurring task "HealthCheck" executed (run 1/5)
```

## Key Design Decisions
- Priority Queue ensures highest priority tasks execute first
- Dependency graph prevents scheduling until all prerequisites complete
- Recurring tasks reschedule themselves after each execution
- Workers pull from a shared priority queue

## Interview Tips
- Explain how you prevent circular dependencies (topological sort / DFS cycle detection)
- Discuss priority inversion and solutions
- Talk about worker pool sizing strategies
- Mention how delayed/recurring tasks work with the priority queue
- Discuss fault tolerance: what happens when a task fails?

---

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** Two workers picking up the same task, task dependency resolution under concurrency.

```bash
cd concurrent
mkdir -p out
javac -d out model/*.java service/*.java Main.java
java -cp out Main
```

### Key Concurrency Techniques Used
| Technique | Where | Why |
|-----------|-------|-----|
| AtomicReference (CAS) | Task.tryPickUp() | Only one worker can transition READY -> RUNNING |
| CountDownLatch per task | DependencyResolver | Threads wait for dependency completion, wake atomically |
| CopyOnWriteArrayList | ConcurrentScheduler.allTasks | Safe iteration while workers modify task states |
| CAS state machine | Task (WAITING -> READY -> RUNNING -> COMPLETED) | Lock-free state transitions prevent double-pickup |
| Dependency graph | DependencyResolver | Ensures topological ordering respected under concurrency |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

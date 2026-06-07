# LLD Interview Playbook

A 45–60-minute Low-Level Design interview is **not** a competitive-programming question. The interviewer is grading three things: (1) can you turn a vague requirement into a clean object model, (2) do you make sensible trade-offs out loud, and (3) is the code you produce extensible? You can ace LeetCode and still fail this round if you start typing too early.

This page mirrors the [interview tips section](../README.md) in the repo README — keep it open in a tab while you practice with the problems on the left.

---

## The 7-Step Framework — Use This for Every Problem

| # | Step | Time | What You Do | What You Say Out Loud |
|---|------|------|------------|----------------------|
| 1 | **Clarify scope** | 3–5 min | Ask 3–5 questions to nail down what's in / out of scope. Don't assume. | *"How many vehicle types? Multi-level? Pricing fixed or pluggable? Concurrent entries?"* |
| 2 | **List functional requirements** | 2 min | Write 4–6 bullets of *what the system does*. Read them back. | *"Park, unpark, calculate fee, find available spot, notify on full."* |
| 3 | **List non-functional requirements** | 2 min | Scale, concurrency, latency, extensibility. Even one bullet shows seniority. | *"Should support concurrent entries; pricing must be swappable without modifying core."* |
| 4 | **Identify entities (nouns)** | 5 min | Underline nouns in the requirements. Those are your classes. | *"Vehicle, ParkingSpot, Ticket, Level, ParkingLot, PricingStrategy."* |
| 5 | **Identify operations (verbs)** | 3 min | Verbs become methods on the right entity. | *"`park` lives on ParkingLot, not Vehicle. `calculatePrice` lives on PricingStrategy."* |
| 6 | **Sketch the API + classes** | 10 min | Method signatures first. Fields follow. Patterns emerge naturally — don't force them. | *"I'll use Strategy here because pricing is pluggable. Singleton on ParkingLot."* |
| 7 | **Code the core flow** | 15–20 min | Implement Main + the happy path. Stub edge cases as comments. | *"I'll skip the persistence layer — let me know if you want it."* |

---

## What Interviewers Actually Score

| Signal | What Earns Points | What Loses Points |
|--------|------------------|------------------|
| **Clarification** | Asks 3–5 sharp questions before coding | Starts coding immediately |
| **Vocabulary** | Says "Strategy", "Open/Closed", "race condition", "linearizability" | Generic "I'll just use a HashMap" without justification |
| **Trade-offs** | "I picked HashMap over TreeMap because lookups are 10x more frequent than range queries" | Silent choices |
| **Extensibility** | Adds an interface where future variation is likely (pricing, notifications, matching) | Hard-codes everything as `if/else` chains |
| **Concurrency awareness** | Spots the race condition unprompted, even if they don't fix it | "Yeah, threads, sure" hand-wave |
| **Code quality** | Small classes, single responsibility, meaningful names | 200-line god classes named `Manager` |
| **Honesty** | "I'm not sure — I'd reach for X but verify with a benchmark" | Bluffing |

---

## Cheat Sheet — Pattern by Symptom

When you hear this in the question, reach for this pattern:

| Requirement signal | Pattern | Where to study it on this site |
|-------------------|---------|-------------------------------|
| "Pluggable rules / algorithms" | **Strategy** | `01-parking-lot`, `23-splitwise` |
| "Different behavior in different states" | **State** | `02-elevator-system`, `04-vending-machine`, `11-atm-machine` |
| "Notify subscribers / listeners" | **Observer** | `19-pub-sub-system`, `24-online-auction` |
| "Undo / redo / replay" | **Command** + **Memento** | `43-document-editor`, `22-spreadsheet` |
| "Tree of things treated uniformly" | **Composite** | `21-file-system` |
| "One global instance" | **Singleton** | Use sparingly — only for true singletons (logger, config) |
| "Wrap an object to add behavior" | **Decorator** | `16-notification-system`, `18-cache-system` |
| "Caller shouldn't know which subclass to make" | **Factory** | `12-car-rental-system` |
| "Build a complex object step by step" | **Builder** | `48-pizza-delivery` |
| "Coordinate many objects without N×N coupling" | **Mediator** | `41-chat-application`, `25-stock-exchange` |
| "Pass request through a series of handlers" | **Chain of Responsibility** | `26-payment-gateway`, `17-logging-framework` |
| "Reuse expensive objects" | **Object Pool** | `30-connection-pool`, `31-thread-pool` |
| "Wrap an incompatible interface" | **Adapter** | Third-party SDK integrations |
| "Fixed skeleton, swappable steps" | **Template Method** | `06-snake-and-ladder`, `50-card-game-blackjack` |

---

## Top 10 Mistakes to Avoid

1. **Coding before clarifying.** The single most common mistake. Even 60 seconds of clarification saves you 10 minutes of rework.
2. **God classes named `Manager`, `Helper`, `Util`.** A class with 15 unrelated methods is a violation of SRP and a sign you didn't decompose.
3. **Inheritance for code reuse.** Use composition. Inherit only when there is genuine *is-a* substitutability (Liskov).
4. **Premature abstraction.** Don't add a `Strategy` for one algorithm "just in case." Add it when the requirement says "different rules" or the interviewer hints at extension.
5. **Ignoring concurrency entirely.** If the interviewer mentions "many users", "real-time", or "high traffic", they want to hear *something* about thread safety — at minimum, name the data structure.
6. **Silent decisions.** Saying nothing while you type makes you look like you're guessing. Narrate: *"I'm picking a `ConcurrentHashMap` here because…"*
7. **Forgetting the question's verbs.** Every requirement verb (park, unpark, calculate, notify) must map to a method. If it doesn't, you missed a feature.
8. **Premature optimization.** "I'd use a Bloom filter" before you've drawn a single class is a red flag, not a green one. Get the model right first.
9. **No `Main` / no demo.** Always end with a runnable `main()` that exercises the happy path. It proves your design *works*, not just that it compiles.
10. **Defensive null-checks everywhere.** Use `Optional`, immutable objects, and trust internal contracts. Validate at the boundary, not in every method.

---

## When to Use a DTO vs. a Rich Object

This is the distinction between **procedural** and **object-oriented** thinking. In LLD interviews, you need both — and the ability to articulate *why* you chose one over the other.

A **DTO (Data Transfer Object)** is a plain struct — public fields (or getters/setters), no business logic. It *carries* data. A **rich object** *owns* data and *protects* it with behavior.

### Use a DTO (procedural style) when:

| Signal | Example | Reason |
|--------|---------|--------|
| **Pure data carrier between layers** | `ParkingTicketDTO` returned by an API | No logic needed — just serialization |
| **No invariant can be violated** | `Coordinate(int x, int y)` | All combinations are valid |
| **External system boundary** | `PaymentResponse` from a gateway | You don't control the shape, just map it |
| **Read model / view projection** | `SpotAvailabilityView` | Flat, denormalized for display — no behavior |
| **Immutable value with no operations** | `Money(int amount, Currency currency)` | It's a measurement, not an actor |

### Use a rich object (OOP style) when:

| Signal | Example | Reason |
|--------|---------|--------|
| **Illegal states are possible** | `Ticket` without entry time | Constructor rejects bad input — a DTO can't |
| **Behavior guards state transitions** | `ParkingSpot.park(vehicle)` | Must check: is spot free? Does size match? |
| **Polymorphism is needed** | `PricingStrategy` → Hourly, FlatRate | You need `strategy.calculate(duration)` to dispatch |
| **Encapsulation hides complexity** | `Elevator.moveToFloor(n)` | Internal queue management shouldn't leak to callers |
| **State machine with rules** | `Order` → PLACED → PAID → SHIPPED | Transitions have preconditions: can't ship before paying |
| **It's a noun the interviewer named** | "The system issues a Ticket" | Named domain concepts = classes |

### Side by side:

```java
// DTO — just carries data, no logic. Procedural code operates ON it.
class TicketDTO {
    private final String id;
    private final String vehicleId;
    private final String spotId;
    private final LocalDateTime entryTime;

    TicketDTO(String id, String vehicleId, String spotId, LocalDateTime entryTime) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.spotId = spotId;
        this.entryTime = entryTime;
    }

    // Only getters. No business logic. No validation beyond null-checks.
    String getId() { return id; }
    String getVehicleId() { return vehicleId; }
    String getSpotId() { return spotId; }
    LocalDateTime getEntryTime() { return entryTime; }
}

// Service (procedural) operates on the DTO externally:
class FeeCalculator {
    int calculate(TicketDTO ticket, LocalDateTime exitTime) {
        long hours = Duration.between(ticket.getEntryTime(), exitTime).toHours();
        return (int) hours * RATE_PER_HOUR;
    }
}
```

```java
// Rich object — data + behavior together. Logic lives WHERE the data is.
class Ticket {
    private final Vehicle vehicle;
    private final ParkingSpot spot;
    private final LocalDateTime entryTime;

    Ticket(Vehicle vehicle, ParkingSpot spot) {
        if (vehicle == null || spot == null) throw new IllegalArgumentException();
        this.vehicle = vehicle;
        this.spot = spot;
        this.entryTime = LocalDateTime.now();
    }

    int calculateFee(LocalDateTime exitTime, PricingStrategy strategy) {
        return strategy.calculate(this.entryTime, exitTime, this.vehicle.getType());
    }
}
```

### When it's a judgement call:

The grey zone is entities that *could* be either. Decision rule: **if external code needs to check conditions before acting on the data, that logic belongs inside the object.**

```java
// Smell: caller checks before acting → logic is in the wrong place
if (spot.getVehicle() == null && spot.getSize().fits(car)) {
    spot.setVehicle(car);  // what if another thread does this simultaneously?
}

// Fix: object owns the decision
boolean parked = spot.tryPark(car);  // atomic check + act inside
```

---

## When to Use `record` in Java

Java `record` (Java 14+) is syntactic sugar for an immutable DTO. It auto-generates the constructor, getters, `equals()`, `hashCode()`, and `toString()`. Use it when you'd otherwise write a boilerplate-heavy class with only final fields and getters.

### What a record gives you:

```java
// This one line:
record Coordinate(int x, int y) {}

// Is equivalent to writing all of this:
class Coordinate {
    private final int x;
    private final int y;

    Coordinate(int x, int y) { this.x = x; this.y = y; }

    int x() { return x; }       // Note: no "get" prefix
    int y() { return y; }

    @Override public boolean equals(Object o) { ... }
    @Override public int hashCode() { ... }
    @Override public String toString() { return "Coordinate[x=" + x + ", y=" + y + "]"; }
}
```

### Use `record` when:

| Scenario | Example |
|----------|---------|
| Immutable value objects | `record Money(int amount, Currency currency) {}` |
| API response/request DTOs | `record ParkingResponse(String ticketId, String spotId) {}` |
| Method return tuples | `record SearchResult(List<Spot> spots, int totalCount) {}` |
| Map keys that need proper equals/hashCode | `record SpotKey(int level, int position) {}` |
| Event payloads | `record SpotFreedEvent(String spotId, LocalDateTime time) {}` |

### Do NOT use `record` when:

| Scenario | Why | Use instead |
|----------|-----|-------------|
| Mutable state | Records are final — fields can't change | Regular class |
| Business logic inside | Records are data carriers, not actors | Rich object |
| Inheritance needed | Records can't extend other classes | Abstract class |
| Lazy initialization | All fields must be set in constructor | Regular class with factory |

### What to say in an interview:

> *"I'll use a record here because it's a pure data carrier — immutable, no logic, and I get equals/hashCode for free which matters since I'm using it as a Map key."*

> *"I won't use a record for Ticket because it has behavior (calculateFee) and I might need to extend it later. Records are for value objects, not domain entities."*

### Compact constructor (validation in records):

```java
record Money(int amount, Currency currency) {
    // Compact constructor — validates without repeating field assignments
    Money {
        if (amount < 0) throw new IllegalArgumentException("Amount cannot be negative");
        if (currency == null) throw new IllegalArgumentException("Currency required");
    }
}
```

This is the one case where a record *can* enforce invariants — but it still shouldn't have business logic methods. If you find yourself adding 3+ methods to a record, upgrade it to a class.

### What to say in an interview:

> *"I'm keeping this as a DTO because it's just a data bag crossing the API boundary — no logic, no invariants. The service layer operates on it."*

> *"I'm making this a class with private fields because there are illegal states — a Ticket without an entry time is nonsensical. The constructor enforces that invariant."*

> *"The rule of thumb: if I find myself writing `if (object.getX() && object.getY()) then doSomething(object)` — that logic should move inside the object as a method."*

---

## Phrases That Make You Sound Senior

Memorize a few of these — they're force multipliers in any LLD round:

- *"I'd start with the simplest design that satisfies the requirement, then extract a Strategy if the rules need to vary."*
- *"This is a read-heavy workload, so I'll bias toward a structure with O(1) lookups and accept O(log n) writes."*
- *"To keep this thread-safe without a global lock, I'd reach for a `ConcurrentHashMap` and CAS on the value."*
- *"That violates the Open/Closed principle — let me extract an interface here."*
- *"I'd add an interface even though there's only one implementation today, because the requirement explicitly says 'pluggable'."*
- *"There's a race condition between checking and inserting — I'd use `putIfAbsent` to make it atomic."*
- *"I'm trading memory for latency here. If the interviewer cares about footprint, I'd switch to lazy evaluation."*
- *"That's an instance of the Composite pattern — files and folders are both `FileSystemEntry`."*
- *"I'd push that responsibility into a separate `NotificationService` — it's not the parking lot's job."*
- *"For the MVP I'd skip persistence; if asked, I'd add a `Repository` interface and inject it."*

---

## Time Budget for a 45-Minute Round

| Phase | Time | Output |
|-------|------|--------|
| Clarify + requirements | 5 min | A bulleted list both you and the interviewer agree on |
| Entities + class diagram | 10 min | A whiteboard / shared doc with classes, fields, methods |
| Core implementation | 20 min | Working `Main` + 2–3 core classes |
| Walkthrough + edge cases | 5 min | "Here's how I'd handle X… and Y…" |
| Buffer | 5 min | For follow-ups: "How would you scale this?" |

> **Pro tip:** If you're at minute 20 and still drawing the class diagram, that's *good* — it means you're not coding spaghetti. If you're at minute 20 and 200 lines deep with no design done, that's bad.

---

## Recommended Practice Order

1. **Warm-up (Easy):** `28-url-shortener`, `06-snake-and-ladder`, `07-tic-tac-toe` — small surface, lets you focus on the framework above.
2. **Bread-and-butter (Medium):** `01-parking-lot`, `11-atm-machine`, `18-cache-system`, `27-rate-limiter`, `23-splitwise` — these come up in 70% of interviews.
3. **Concurrency-flavored (Medium-Hard):** `30-connection-pool`, `32-producer-consumer`, `31-thread-pool`, `24-online-auction`, `26-payment-gateway` — the differentiator at senior+.
4. **Heavy domains (Hard):** `02-elevator-system`, `08-chess-game`, `25-stock-exchange`, `22-spreadsheet`, `43-document-editor` — practice these only after you can handle the framework with smaller problems.

For each one: do `naive/` first to get the design right, then `optimized/` to learn the production tricks, then `concurrent/` to learn the thread-safety patterns. Read `VARIATIONS.md` last — it's your interviewer's likely follow-up.

---

## Java Concurrency Primitives — Quick Reference

Every `concurrent/` folder in this repo uses these primitives. Master them and you'll nail the "how would you make this thread-safe?" follow-up.

### AtomicInteger / AtomicLong

**What it does:** A thread-safe integer/long with lock-free CAS (compare-and-set) operations.

```java
AtomicInteger counter = new AtomicInteger(0);

counter.incrementAndGet();        // ++x atomically, returns new value
counter.decrementAndGet();        // --x atomically
counter.getAndIncrement();        // x++ atomically, returns OLD value
counter.compareAndSet(5, 6);      // if current==5, set to 6, return true
counter.get();                    // read current value
```

**When to use:** Counters (available spots, ticket IDs), rate limiters, monotonic sequence generators.

**Problems that use it:** `27-rate-limiter` (token count), `28-url-shortener` (counter-based IDs), `25-stock-exchange` (partial fill quantity), `30-connection-pool` (pool size tracking).

---

### AtomicReference\<T\>

**What it does:** A thread-safe reference pointer. Lets you atomically swap what an object points to.

```java
AtomicReference<Bid> highest = new AtomicReference<>(null);

// CAS loop — the core lock-free pattern
Bid current;
do {
    current = highest.get();
    if (newBid.amount <= current.amount) return false;
} while (!highest.compareAndSet(current, newBid));
```

**When to use:** "Current highest bid", "current state", any single-writer-or-CAS-loop scenario.

**Problems that use it:** `24-online-auction` (highest bid), `25-stock-exchange` (order status transitions), `34-circuit-breaker` (state reference).

---

### ConcurrentHashMap

**What it does:** A thread-safe HashMap with segment-level locking (not a global lock). Reads never block.

```java
ConcurrentHashMap<String, Ticket> map = new ConcurrentHashMap<>();

map.put(key, value);                           // thread-safe put
map.get(key);                                  // lock-free read
map.putIfAbsent(key, value);                   // atomic "insert if missing"
map.computeIfAbsent(key, k -> new Thing(k));   // atomic "get or create"
map.compute(key, (k, v) -> v.update());        // atomic read-modify-write
```

**When to use:** Any shared key-value registry. The single most important concurrent collection.

**Problems that use it:** `28-url-shortener` (code→URL registry), `26-payment-gateway` (idempotency), `29-key-value-store` (main store), `23-splitwise` (balance registry), `27-rate-limiter` (per-client limiters).

---

### ConcurrentLinkedQueue

**What it does:** An unbounded, lock-free FIFO queue. `offer()` and `poll()` are O(1) and never block.

```java
ConcurrentLinkedQueue<ParkingSpot> freeSpots = new ConcurrentLinkedQueue<>();

freeSpots.offer(spot);       // add to tail — always succeeds
ParkingSpot s = freeSpots.poll();  // remove from head — null if empty
```

**When to use:** Free-slot pools, work queues where blocking isn't needed.

**Problems that use it:** `01-parking-lot` (available spot queue), `35-object-pool` (idle objects).

---

### BlockingQueue (LinkedBlockingQueue)

**What it does:** A bounded queue where `put()` blocks when full and `take()` blocks when empty.

```java
BlockingQueue<Task> queue = new LinkedBlockingQueue<>(100);

queue.put(task);        // blocks if full (backpressure)
Task t = queue.take();  // blocks if empty (waits for work)
```

**When to use:** Producer-consumer patterns. The queue handles ALL synchronization.

**Problems that use it:** `32-producer-consumer`, `31-thread-pool` (task queue), `20-task-scheduler`.

---

### Semaphore

**What it does:** A permit counter. `acquire()` blocks until a permit is available. `release()` returns one.

```java
Semaphore permits = new Semaphore(10, true);  // 10 permits, fair ordering

permits.tryAcquire(5, TimeUnit.SECONDS);  // wait up to 5s
try {
    // use the bounded resource
} finally {
    permits.release();
}
```

**When to use:** Limiting concurrent access to a fixed pool (connections, threads).

**Problems that use it:** `30-connection-pool` (max connections), `31-thread-pool` (worker limit).

---

### ReentrantReadWriteLock

**What it does:** Many concurrent readers OR one exclusive writer. Readers never block each other.

```java
ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

rwLock.readLock().lock();    // many readers allowed
rwLock.writeLock().lock();   // exclusive — blocks all readers & writers
```

**When to use:** Read-heavy workloads where writes are rare (caches, config, spreadsheet cells).

**Problems that use it:** `22-spreadsheet` (cell locks), `18-cache-system` (read-heavy cache).

---

### CopyOnWriteArrayList

**What it does:** Every write clones the entire array. Readers see a stable snapshot and never block.

```java
CopyOnWriteArrayList<Observer> listeners = new CopyOnWriteArrayList<>();
```

**When to use:** Observer/listener lists — many readers (notify loop), rare writers (subscribe/unsubscribe).

**Problems that use it:** `25-stock-exchange` (trade recording), `19-pub-sub-system` (subscriber list).

---

### CountDownLatch

**What it does:** A one-shot barrier. N threads `countDown()`, one coordinator `await()`s until count hits 0.

```java
CountDownLatch ready = new CountDownLatch(3);

// In each worker:
ready.countDown();   // "I'm ready"

// In coordinator:
ready.await();       // blocks until all 3 check in
```

**When to use:** "Start all threads at once" (load tests), "wait for N initializations to complete".

**Problems that use it:** `30-connection-pool` (concurrent borrow test), `31-thread-pool` (startup barrier).

---

### The CAS Loop Pattern

This is THE fundamental lock-free pattern. Memorize it:

```java
AtomicReference<State> state = new AtomicReference<>(CLOSED);

// Thread-safe state transition without locks:
State current;
do {
    current = state.get();
    if (!isValidTransition(current, newState)) return false;
} while (!state.compareAndSet(current, newState));
// If CAS fails, another thread changed it first — retry with fresh read
```

**When to say this in an interview:** *"I'd use a CAS loop here — it's optimistic locking without a lock. Under low contention it's one atomic instruction; under high contention threads retry but never block."*

---

## How to Practice With This Site

1. Pick a problem from the sidebar.
2. Cover the code with your hand. Read only the **Problem Statement** and **Requirements**.
3. Open a notepad and walk through steps 1–6 of the framework above. Write classes, methods, patterns.
4. Now uncover the `naive/` code. Compare your design to it.
5. Click **Edit** on `Main.java` and try implementing one variation from `VARIATIONS.md` yourself.
6. Click **Run** to compile and execute. Iterate until your tweak works.
7. Read `optimized/` and `concurrent/` to see what improvements production scale demands.
8. Mark the problem as completed when you can re-derive its design from scratch in under 30 minutes.

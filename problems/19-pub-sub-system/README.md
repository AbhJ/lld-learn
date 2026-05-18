# Pub-Sub Messaging System


## Problem Statement
Design a publish-subscribe messaging system where publishers send messages to topics and subscribers receive messages from topics they've subscribed to. The system supports content-based filtering, different delivery guarantees, and per-subscriber message queues.

The message broker acts as a mediator between publishers and subscribers. Subscribers can filter messages based on content criteria. The system supports at-most-once and at-least-once delivery semantics.

Each subscriber has its own message queue, allowing independent consumption rates. The broker handles message routing, filtering, and delivery tracking.

## Requirements
### Functional Requirements
- Create and manage topics
- Publish messages to topics
- Subscribe/unsubscribe from topics
- Content-based message filtering
- Per-subscriber message queues
- Configurable delivery guarantees (at-most-once, at-least-once)
- Message acknowledgment

### Non-functional Requirements
- Decoupled publishers and subscribers
- Message ordering within a topic
- Extensible filtering mechanism
- Reliable delivery for at-least-once semantics

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Observer | Core pub-sub mechanism | Notify subscribers of new messages |
| Mediator | Message broker | Centralized message routing |
| Factory | Message creation | Standardized message construction |

## Folder Structure
```
19-pub-sub-system/
├── naive/
│   ├── model/      -> Message, Topic, DeliveryGuarantee, Publisher
│   ├── service/    -> Subscriber, MessageQueue, MessageBroker
│   ├── strategy/   -> Filter (PayloadContains, Header, AllPass)
│   └── Main.java
└── optimized/
    ├── model/
    ├── service/    -> MessageBroker (per-topic thread pool, ack tracking)
    ├── strategy/
    └── Main.java
```

### How to Run
```bash
# Run the naive (simple) version:
cd naive
mkdir -p out
javac -d out model/*.java strategy/*.java service/*.java Main.java
java -cp out Main

# Run the optimized version:
cd optimized
mkdir -p out
javac -d out model/*.java strategy/*.java service/*.java Main.java
java -cp out Main
```

### Naive vs Optimized
| Operation | Naive | Optimized |
|-----------|-------|-----------|
| Message delivery | Synchronous single-thread | Per-topic thread pool (parallel topics) |
| Ordering guarantee | Implicit (single thread) | Explicit (single thread per topic) |
| Delivery semantics | Fire-and-forget | At-least-once with ack tracking |
| Unacked retry | Not supported | Retry with message list per subscriber |

---

## Class Diagram (Text)
```
MessageBroker (Mediator)
 ├── Topic
 ├── Publisher
 ├── Subscriber (Interface)
 │    └── ConcreteSubscriber
 ├── Message
 ├── Filter (content-based)
 ├── DeliveryGuarantee (enum)
 └── MessageQueue (per-subscriber)
```

## How to Compile and Run
```bash
cd problems/19-pub-sub-system
mkdir -p out
javac -d out *.java
java -cp out Main
```

## Expected Output
```
=== Pub-Sub System Demo ===
Topic "orders" created.
Subscriber "OrderProcessor" subscribed to "orders".
Publisher published: {orderId: 123, amount: 99.99}
OrderProcessor received: {orderId: 123, amount: 99.99}
Message acknowledged by OrderProcessor.
```

## Key Design Decisions
- Mediator pattern decouples publishers from subscribers
- Per-subscriber queues allow independent consumption
- Content-based filtering happens at the broker level
- At-least-once uses ack/retry mechanism

## Interview Tips
- Explain the difference between at-most-once and at-least-once delivery
- Discuss how to achieve exactly-once semantics (idempotency)
- Talk about message ordering guarantees and trade-offs
- Mention backpressure handling for slow subscribers
- Compare topic-based vs content-based routing

---

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** Publisher publishes while new subscriber is being added — subscriber misses the message or gets partial state.

```bash
cd concurrent
mkdir -p out
find . -name '*.java' | xargs javac -d out
java -cp out Main
```

### Key Concurrency Techniques Used
| Technique | Where | Why |
|-----------|-------|-----|
| CopyOnWriteArrayList | Topic.subscribers | Safe publish during subscriber add/remove (snapshot iteration) |
| AtomicLong | Message.sequenceNumber | Global ordering guarantee across concurrent publishers |
| ConcurrentHashMap | MessageBroker.topics | Thread-safe topic registry with computeIfAbsent |
| ConcurrentLinkedQueue | Topic.messageHistory | Thread-safe message history for verification |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

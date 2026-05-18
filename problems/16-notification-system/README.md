# Notification System


## Problem Statement
Design a multi-channel notification system that supports sending messages via email, SMS, and push notifications. The system handles different priority levels, message templates, rate limiting (throttling), and retry logic for failed deliveries.

Notifications are created through a factory based on the event type and routed to appropriate channels. A throttler prevents notification spam by limiting the rate of notifications per user. The retry decorator adds fault tolerance for transient delivery failures.

The system is extensible: new channels and notification types can be added without modifying existing code.

## Requirements
### Functional Requirements
- Send notifications via multiple channels (Email, SMS, Push)
- Support priority levels (LOW, MEDIUM, HIGH, CRITICAL)
- Template-based message formatting
- Rate limiting per user per channel
- Retry logic for failed deliveries
- Track notification delivery status

### Non-functional Requirements
- Extensible for new channels
- Configurable throttling rules
- Reliable delivery for critical notifications
- Audit trail of all notifications

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Observer | Event to notification mapping | Decouple event sources from notification logic |
| Strategy | Channel selection and delivery | Different delivery mechanisms per channel |
| Factory | Notification creation | Create appropriate notification type based on event |
| Decorator | Priority/retry enhancement | Add behavior without modifying core notification |

## Folder Structure
```
16-notification-system/
├── naive/
│   ├── model/      -> Notification, Priority, Template
│   ├── service/    -> NotificationFactory, NotificationService, RetryDecorator, Throttler
│   ├── strategy/   -> Channel (Email, SMS, Push)
│   └── Main.java
└── optimized/
    ├── model/
    ├── service/    -> AsyncNotificationService (BlockingQueue per channel), RateLimiter
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
| Send notification | Synchronous (blocks caller) | Async (non-blocking enqueue) |
| Channel isolation | Shared thread | Per-channel BlockingQueue + worker |
| Batch processing | Sequential | Drain + batch sort by priority |
| Rate limiting | Sliding window (List scan) | Token bucket (O(1) CAS) |

---

## Class Diagram (Text)
```
NotificationService (Main)
 ├── Notification
 ├── Channel (Interface)
 │    ├── EmailChannel
 │    ├── SMSChannel
 │    └── PushChannel
 ├── Template
 ├── Priority (Enum)
 ├── Throttler
 ├── NotificationFactory
 └── RetryDecorator
```

## How to Compile and Run
```bash
cd problems/16-notification-system
mkdir -p out
javac -d out *.java
java -cp out Main
```

## Expected Output
```
=== Notification System Demo ===
Sending EMAIL [HIGH] to user@example.com: "Your order has shipped!"
Sending PUSH [MEDIUM] to device_123: "New message from Alice"
Throttled: SMS rate limit exceeded for user U-1
Retry 1/3 failed for EMAIL. Retrying...
Retry 2/3 succeeded.
```

## Key Design Decisions
- Factory pattern centralizes notification creation logic
- Decorator pattern adds retry without modifying channel implementations
- Throttler uses sliding window for rate limiting
- Priority determines retry attempts and channel selection

## Interview Tips
- Discuss how to handle guaranteed delivery for critical notifications
- Explain throttling algorithms (token bucket, sliding window)
- Talk about async notification delivery and queuing
- Mention dead letter queues for persistently failing notifications
- Discuss how to add new channels without code changes (Open/Closed Principle)

---

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** Multiple events triggering notifications to same user — throttle bypassed, user gets flooded.

```bash
cd concurrent
mkdir -p out
find . -name '*.java' | xargs javac -d out
java -cp out Main
```

### Key Concurrency Techniques Used
| Technique | Where | Why |
|-----------|-------|-----|
| AtomicInteger (CAS) | Throttler.tryAcquire() | CAS loop ensures exact rate limit enforcement |
| ConcurrentHashMap | Throttler.userCounters | Per-user counters without global lock |
| computeIfAbsent | Counter initialization | Thread-safe lazy counter creation per user |
| ConcurrentLinkedQueue | NotificationService delivered/throttled | Thread-safe collection of results |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

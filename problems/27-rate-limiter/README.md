# Rate Limiter


## Problem Statement
Design a rate limiter that protects a service from being overwhelmed by limiting how many requests a given client (identified by user ID, IP address, or API key) can make in a fixed time window. For example: *"each client can make at most 100 requests per minute"*. When a client exceeds the limit, the limiter rejects the request (typically with HTTP 429), so the downstream service is shielded from overload.

The rate-limit decision must be made on every incoming request and must be **fast and accurate** — a slow rate limiter would itself become the bottleneck, and an inaccurate one either lets too much traffic through or blocks legitimate users. The system should support multiple algorithms — **sliding window**, **token bucket**, **fixed window**, **leaky bucket** — chosen per route or per client tier (e.g., free vs paid users get different limits).

In production, thousands of requests hit the limiter concurrently. A naive `synchronized` counter serializes all requests through a single lock and kills throughput. The limiter must scale with concurrency, isolate clients from each other (one heavy user shouldn't slow another), and use bounded memory regardless of request volume.

## Requirements

### Functional Requirements
- Configure per-client limits (e.g., 100 requests / 60 seconds)
- Decide allow / reject for each incoming request
- Support multiple algorithms: Sliding Window, Token Bucket, Fixed Window
- Per-client isolation — independent counters keyed by clientId
- Reset behavior at window boundaries (or token refill)
- Expose stats: current count, remaining quota, reset time
- Different limits per client tier (free / premium / enterprise)

### Non-functional Requirements
- O(1) allow/reject decision in optimized version (circular buffer / atomic counter)
- Lock-free per-client counters — no global lock; AtomicInteger CAS for token bucket
- Bounded memory per client (fixed-size circular buffer, not unbounded LinkedList)
- ConcurrentHashMap for client registry — clients don't block one another
- Extensible: new algorithms pluggable behind the RateLimiter interface

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Strategy | RateLimiter | Pluggable algorithms (sliding window, token bucket) |
| Factory | RateLimiterService | Creates limiter instances from config |

---

## Folder Structure
```
27-rate-limiter/
├── README.md
├── VARIATIONS.md
├── naive/
│   ├── model/        ← Request, RateLimitConfig
│   ├── service/      ← RateLimiterService
│   ├── strategy/     ← RateLimiter interface, SlidingWindowLimiter (synchronized)
│   └── Main.java
└── optimized/
    ├── model/        ← Same entities
    ├── service/      ← RateLimiterService
    ├── strategy/     ← RateLimiter, SlidingWindowLimiter (atomic circular buffer)
    └── Main.java
```

### How to Run
```bash
# Naive
cd problems/27-rate-limiter/naive
mkdir -p out && javac -d out model/*.java strategy/*.java service/*.java Main.java && java -cp out Main

# Optimized
cd problems/27-rate-limiter/optimized
mkdir -p out && javac -d out model/*.java strategy/*.java service/*.java Main.java && java -cp out Main
```

### Naive vs Optimized
| Operation | Naive | Optimized |
|-----------|-------|-----------|
| Rate check | O(expired) LinkedList scan | O(1) circular buffer check |
| Concurrency | Global synchronized lock | AtomicInteger per bucket (lock-free) |
| Client isolation | Shared lock blocks all clients | ConcurrentHashMap — independent |
| Memory | Unbounded LinkedList per client | Fixed 10-slot circular buffer |

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** Multiple requests checking rate limit simultaneously — all pass before counter updates, exceeding limit.

```bash
cd concurrent
mkdir -p out
find . -name '*.java' | xargs javac -d out
java -cp out Main
```

### Key Concurrency Techniques Used
| Technique | Where | Why |
|-----------|-------|-----|
| AtomicInteger | TokenBucketRateLimiter.tokens | Lock-free token management |
| compareAndSet loop | tryAcquire() | Atomic decrement — exactly N requests pass |
| CAS-based refill | refill() | Safe concurrent token replenishment |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

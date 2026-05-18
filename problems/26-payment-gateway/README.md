# Payment Gateway


## Problem Statement
Design a payment gateway that sits between a merchant's checkout flow and one or more payment providers (Stripe, PayPal, Razorpay, etc.). When the merchant submits a payment request — `{amount, currency, customer, payment method, idempotency key}` — the gateway runs a series of **validation checks** (amount > 0, supported currency, fraud screening, customer KYC), routes the request to the appropriate provider, and returns a `Transaction` record with a final status: `SUCCESS`, `FAILED`, or `PENDING`.

A critical requirement is **idempotency**: if the same logical payment is submitted twice (e.g., the user double-clicks "Pay", or the network retries) with the same idempotency key, the customer must only be charged once. The gateway must detect duplicates atomically — a check-then-insert pattern is unsafe under concurrency and would charge twice.

Validation rules vary by merchant and should be **composable** — adding a new rule (e.g., a velocity check) should not require touching existing rules. Provider integrations should also be **pluggable** so adding a new provider is a single class.

## Requirements

### Functional Requirements
- Accept payment requests with amount, currency, payment method, idempotency key
- Run a chain of validation rules (amount, currency, fraud, KYC) — short-circuit on first failure
- Route to a payment provider strategy (Stripe, PayPal, etc.)
- Return a Transaction record with final status (SUCCESS / FAILED / PENDING)
- Enforce idempotency by key — same key returns the original Transaction, no double-charge
- Support refunds and status queries by transaction ID
- Log every payment attempt for audit

### Non-functional Requirements
- Atomic duplicate detection via ConcurrentHashMap.putIfAbsent (no TOCTOU race)
- Parallel validation in optimized version (CompletableFuture) — latency = max, not sum
- Immutable Transaction objects for safe sharing across threads
- Extensible: new validation rules and new payment providers added without modifying existing code
- Thread-safe under high concurrent payment volume

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Strategy | PaymentProcessor | Pluggable payment providers |
| Chain of Responsibility | ValidationChain | Composable validation rules |
| Facade | PaymentGateway | Unified payment API |

---

## Folder Structure
```
26-payment-gateway/
├── README.md
├── VARIATIONS.md
├── naive/
│   ├── model/        ← PaymentRequest, Transaction, TransactionStatus
│   ├── service/      ← ValidationChain, PaymentGateway
│   ├── strategy/     ← PaymentProcessor (Stripe, PayPal)
│   └── Main.java
└── optimized/
    ├── model/        ← Same entities
    ├── service/      ← Parallel validation, ConcurrentHashMap idempotency
    ├── strategy/     ← Same processors
    └── Main.java
```

### How to Run
```bash
# Naive
cd problems/26-payment-gateway/naive
mkdir -p out && javac -d out model/*.java strategy/*.java service/*.java Main.java && java -cp out Main

# Optimized
cd problems/26-payment-gateway/optimized
mkdir -p out && javac -d out model/*.java strategy/*.java service/*.java Main.java && java -cp out Main
```

### Naive vs Optimized
| Operation | Naive | Optimized |
|-----------|-------|-----------|
| Validation | Sequential (sum of times) | Parallel via CompletableFuture (max of times) |
| Idempotency check | HashSet (not thread-safe) | ConcurrentHashMap.putIfAbsent (atomic) |
| Concurrent payments | Unsafe — race conditions | Thread-safe throughout |
| Duplicate detection | Check-then-add (TOCTOU) | Single atomic putIfAbsent |

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** Double-submit — user clicks pay twice, gets charged twice for same order.

```bash
cd concurrent
mkdir -p out
find . -name '*.java' | xargs javac -d out
java -cp out Main
```

### Key Concurrency Techniques Used
| Technique | Where | Why |
|-----------|-------|-----|
| ConcurrentHashMap | processedPayments | Thread-safe transaction registry |
| putIfAbsent | processPayment() | Atomic idempotency — prevents double-processing |
| Immutable Transaction | Transaction class | Safe publication of payment records |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

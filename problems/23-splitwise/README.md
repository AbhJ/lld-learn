# Splitwise - Expense Sharing


## Problem Statement
Design an expense-sharing application like Splitwise that lets a group of users record shared expenses and tracks who owes whom. When one user pays for something on behalf of others (e.g., a dinner bill, rent, a trip booking), the application records the expense, splits the cost among the participants according to a chosen rule, and updates the running balances between each pair of users.

The system should support multiple **split strategies**: equal split (everyone pays the same), exact amounts (each participant's share is specified), and percentage split (each participant pays a stated percentage). Users can also belong to **groups** (e.g., "Roommates", "Goa Trip") so expenses can be scoped to a group rather than the global list.

At any time a user should be able to see (a) their net balance — how much they owe overall vs. are owed — and (b) per-person balances. The system should also produce a **settlement plan**: the minimum set of payments that would clear all debts within a group, since direct A→B→C chains can often be collapsed into fewer transactions.

## Requirements

### Functional Requirements
- Register users and form groups
- Add expenses with a payer, participants, and total amount
- Support split strategies: Equal, Exact amounts, Percentage
- Track balances between every pair of users
- Show a user's net balance and per-person breakdown
- Compute a minimal settlement plan (fewest transactions to clear all debts in a group)
- Record payments to settle balances
- List all expenses for a user or group

### Non-functional Requirements
- O(n) net-balance updates per expense in the optimized version (vs O(n²) pairwise)
- Thread-safe concurrent expense additions involving overlapping users
- Lock-free balance updates via atomic CAS where possible
- Extensible split strategies — adding a new one should not modify existing code

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Strategy | SplitStrategy | Different split algorithms (equal, %, exact) |
| Facade | SplitwiseService | Unified API for expense operations |

---

## Folder Structure
```
23-splitwise/
├── README.md
├── VARIATIONS.md
├── naive/
│   ├── model/        ← User, Expense, Group, Balance, Settlement
│   ├── service/      ← ExpenseManager, SplitwiseService
│   ├── strategy/     ← SplitStrategy (Equal, Percentage, Exact)
│   └── Main.java
└── optimized/
    ├── model/        ← Same entities
    ├── service/      ← ExpenseManager with net balance simplification
    ├── strategy/     ← Same strategies
    └── Main.java
```

### How to Run
```bash
# Naive
cd problems/23-splitwise/naive
mkdir -p out && javac -d out model/*.java strategy/*.java service/*.java Main.java && java -cp out Main

# Optimized
cd problems/23-splitwise/optimized
mkdir -p out && javac -d out model/*.java strategy/*.java service/*.java Main.java && java -cp out Main
```

### Naive vs Optimized
| Operation | Naive | Optimized |
|-----------|-------|-----------|
| Balance tracking | O(n^2) pairwise debts | O(n) net balance per user |
| Settlement calc | Simple greedy on pairs | Greedy on net amounts (min transactions) |
| Add expense | Updates all affected pairs | Updates 2 net balances |
| Space | O(n^2) balance sheet | O(n) net balances |

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** Two expenses added simultaneously involving same users — balance calculation becomes inconsistent.

```bash
cd concurrent
mkdir -p out
find . -name '*.java' | xargs javac -d out
java -cp out Main
```

### Key Concurrency Techniques Used
| Technique | Where | Why |
|-----------|-------|-----|
| AtomicLong | Per user-pair balance | Lock-free CAS updates to balances |
| ConcurrentHashMap | BalanceManager.balances | Thread-safe pair registry |
| computeIfAbsent | Pair initialization | Atomic lazy creation of balance entries |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

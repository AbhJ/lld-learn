# ATM Machine


## Problem Statement
Design an ATM (Automated Teller Machine) system that allows bank customers to perform various financial transactions. The system should handle card authentication, balance inquiries, cash withdrawals with denomination management, deposits, and fund transfers between accounts.

The ATM operates through a series of states - from inserting a card to authentication via PIN, then performing transactions. The cash dispenser must handle multiple denominations intelligently, always trying to minimize the number of notes dispensed.

The system should maintain transaction history, generate receipts, and handle edge cases like insufficient balance, invalid PIN attempts, and unavailable denominations.

## Requirements
### Functional Requirements
- Insert/eject card functionality
- PIN-based authentication with attempt limits (max 3 attempts)
- Balance inquiry
- Cash withdrawal with denomination handling
- Cash deposit
- Fund transfer between accounts
- Transaction receipt generation
- Transaction history

### Non-functional Requirements
- Thread-safe operations
- State integrity (no invalid state transitions)
- Audit trail for all transactions
- Graceful error handling

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| State | ATM states (NoCard, HasCard, Authenticated) | Clean state transitions, prevents invalid operations |
| Chain of Responsibility | DenominationHandler -> TwoThousandHandler -> FiveHundredHandler -> TwoHundredHandler -> HundredHandler -> FiftyHandler -> TwentyHandler | Each handler tries to dispense its own denomination then forwards the remainder; CashDispenser only knows the chain head |
| Strategy | Transaction types (Withdraw, Deposit, Transfer) | Different transaction logic encapsulated |

## Folder Structure
```
11-atm-machine/
├── naive/
│   ├── model/      -> Account, Card, Receipt, Transaction
│   ├── service/    -> ATM, CashDispenser
│   ├── state/      -> ATMState (NoCard, HasCard, Authenticated)
│   └── Main.java
└── optimized/
    ├── model/
    ├── service/    -> CashDispenser uses TreeMap for O(log n) largest-first
    ├── state/
    └── Main.java
```

### How to Run
```bash
# Run the naive (simple) version:
cd naive
mkdir -p out
javac -d out model/*.java state/*.java service/*.java Main.java
java -cp out Main

# Run the optimized version:
cd optimized
mkdir -p out
javac -d out model/*.java state/*.java service/*.java Main.java
java -cp out Main
```

### Naive vs Optimized
| Operation | Naive | Optimized |
|-----------|-------|-----------|
| Denomination selection | Sort list every time O(d log d) | TreeMap natural order O(log d) |
| Find largest available | Linear scan | TreeMap.firstKey() O(log d) |
| Load denomination | LinkedHashMap put | TreeMap.merge() with auto-sort |
| Empty denom cleanup | Manual | TreeMap.remove() maintains order |

---

## Class Diagram (Text)
```
ATM (Context)
 ├── ATMState (Interface)
 │    ├── NoCardState
 │    ├── HasCardState
 │    └── AuthenticatedState
 ├── CashDispenser
 │    └── DenominationHandler (Chain)
 ├── Transaction (Abstract)
 │    ├── Withdrawal
 │    ├── Deposit
 │    └── Transfer
 ├── Card --> Account
 └── Receipt
```

## How to Compile and Run
```bash
cd problems/11-atm-machine
mkdir -p out
javac -d out *.java
java -cp out Main
```

## Expected Output
```
=== ATM Machine System Demo ===

--- Test 1: Card Insert and Authentication ---
Card inserted: VISA ending 4321
PIN accepted. You are now authenticated.

--- Test 2: Balance Inquiry ---
Account Balance: $5000.00

--- Test 3: Cash Withdrawal ---
Withdrawal of $2750:
  Dispensing: 13 x $200
  Dispensing: 1 x $100
  Dispensing: 1 x $50
Transaction successful. Receipt generated.

--- Test 4: Deposit ---
Deposited $500.00 to account.
New balance: $2750.00

--- Test 5: Transfer ---
Transfer of $1000.00 from ACC-001 to ACC-002 successful.

--- Test 6: Invalid PIN ---
Invalid PIN. Attempts remaining: 2
Invalid PIN. Attempts remaining: 1
Invalid PIN. Card retained. Please contact your bank.

--- Test 7: Insufficient Balance ---
Transaction failed: Insufficient balance.

--- Test 8: Denomination Unavailable ---
Transaction failed: Cannot dispense exact amount with available denominations.
```

## Key Design Decisions
- State pattern prevents illegal operations (e.g., withdrawing without authentication)
- Chain of Responsibility allows flexible denomination handling
- Maximum 3 PIN attempts before card is retained for security
- Receipts are generated for every successful transaction

## Interview Tips
- Start by explaining the state transitions clearly
- Draw the state diagram: NoCard -> HasCard -> Authenticated -> (back to NoCard after eject)
- Explain why State pattern is better than if-else chains
- Discuss how Chain of Responsibility makes denomination logic extensible
- Mention thread safety considerations for real ATMs
- Talk about idempotency for transactions

---

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** Two ATM transactions from the same account — withdrawal exceeds balance when both read balance before either writes.

```bash
cd concurrent
mkdir -p out
find . -name '*.java' | xargs javac -d out
java -cp out Main
```

### Key Concurrency Techniques Used
| Technique | Where | Why |
|-----------|-------|-----|
| AtomicLong (CAS) | Account.withdraw() | CAS loop: read balance, check sufficient, compareAndSet to new balance |
| compareAndSet loop | Account.withdraw() | Retry on contention — no locks needed |
| AtomicInteger | ATM transaction counters | Thread-safe success/failure counting |
| CountDownLatch | Main.java | Ensures all 10 threads start simultaneously for maximum contention |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

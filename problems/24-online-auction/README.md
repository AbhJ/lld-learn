# Online Auction


## Problem Statement
Design an online auction system (like eBay) where sellers list items for auction and bidders place competing bids until the auction closes. Each auction has an item description, a starting price, a start and end time, and a state machine: `CREATED → ACTIVE → CLOSED`.

While an auction is `ACTIVE`, registered bidders can place bids. A new bid is only accepted if it is strictly higher than the current highest bid (and at or above the starting price). When a bid is outbid, the previous highest bidder should be **notified** so they can react. When the auction closes, the system determines the winner using a configurable **winner strategy** — for example, *first-price* (winner pays their own bid) or *Vickrey / second-price* (winner pays the second-highest bid).

Many bidders may compete in the final seconds. The system must guarantee that, under heavy concurrency, no bid lower than the current highest is ever accepted, and that the final "highest bid" is consistent with the bid history — i.e., bids are linearizable.

## Requirements

### Functional Requirements
- Sellers create auctions with starting price, start/end time, and item details
- Bidders register and place bids on active auctions
- Reject bids below the current highest (or below starting price)
- Notify previous high bidder when outbid
- Close auctions automatically at end time
- Determine winner via pluggable strategy (first-price, second-price)
- Query current highest bid and full bid history
- Auction state transitions: CREATED → ACTIVE → CLOSED

### Non-functional Requirements
- O(1) read of current highest bid (atomic reference in optimized)
- Lock-free placeBid via compare-and-set retry loop — linearizable under contention
- Immutable Bid objects for safe cross-thread publication
- Extensible winner strategies without modifying auction logic
- Observers should not block the bidding hot path

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Strategy | WinnerStrategy | Pluggable winner rules (first-price, Vickrey) |
| Observer | Bidder notifications | Outbid alerts without polling |

---

## Folder Structure
```
24-online-auction/
├── README.md
├── VARIATIONS.md
├── naive/
│   ├── model/        ← Item, Bidder, Bid, AuctionState
│   ├── service/      ← Auction, AuctionSystem
│   ├── strategy/     ← WinnerStrategy (HighestBid, SecondPrice)
│   └── Main.java
└── optimized/
    ├── model/        ← Same entities
    ├── service/      ← Auction with PriorityQueue + AtomicReference
    ├── strategy/     ← Same strategies
    └── Main.java
```

### How to Run
```bash
# Naive
cd problems/24-online-auction/naive
mkdir -p out && javac -d out model/*.java strategy/*.java service/*.java Main.java && java -cp out Main

# Optimized
cd problems/24-online-auction/optimized
mkdir -p out && javac -d out model/*.java strategy/*.java service/*.java Main.java && java -cp out Main
```

### Naive vs Optimized
| Operation | Naive | Optimized |
|-----------|-------|-----------|
| Get highest bid | O(n) linear scan | O(1) AtomicReference read |
| Place bid validation | O(n) scan + synchronized | CAS loop (lock-free) |
| Winner determination | O(n) scan | O(1) from max-heap peek |
| Concurrency | Not thread-safe | AtomicReference CAS for concurrent bids |

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** Two bidders placing bids at same millisecond — both think they're highest, system accepts lower bid.

```bash
cd concurrent
mkdir -p out
find . -name '*.java' | xargs javac -d out
java -cp out Main
```

### Key Concurrency Techniques Used
| Technique | Where | Why |
|-----------|-------|-----|
| AtomicReference\<Bid\> | AuctionService.currentHighest | CAS ensures only higher bids succeed |
| compareAndSet loop | placeBid() | Retry on contention — linearizable updates |
| Immutable Bid | Bid class | Safe publication across threads |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

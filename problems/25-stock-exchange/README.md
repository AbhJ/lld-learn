# Stock Exchange


## Problem Statement
Design a stock exchange that accepts buy and sell orders for various stock symbols and matches them according to **price-time priority** (the standard rule used by real exchanges like NYSE and NASDAQ). When orders match, the exchange records a **trade** at the agreed price and quantity, and notifies the involved traders.

The exchange supports two order types: **limit orders** (specify max buy / min sell price) and **market orders** (execute immediately at the best available price). Orders may be **partially filled** — for example, a buy of 100 shares may match against three sell orders of 30, 40, and 30 shares respectively, resulting in three trade records. An order's lifecycle is `PENDING → PARTIALLY_FILLED → FILLED` (or `CANCELLED`).

Each stock symbol has its own **order book** with a buy side (bids, sorted highest price first) and a sell side (asks, sorted lowest price first). Matching attempts to pair the highest bid with the lowest ask whenever they cross. Many traders submit orders simultaneously, so the exchange must serialize matching per-symbol while remaining concurrent across symbols, and must never over-fill an order.

## Requirements

### Functional Requirements
- Submit limit orders (specify price) and market orders (best available)
- Maintain a per-symbol order book with buy and sell sides
- Match orders by price-time priority (best price first; FIFO at same price)
- Support partial fills — generate one trade per match, update remaining quantity
- Cancel pending or partially-filled orders
- Order state transitions: PENDING → PARTIAL → FILLED → CANCELLED
- Notify traders when their orders execute
- Query best bid/ask, full order book, and trade history

### Non-functional Requirements
- O(1) best-price access via TreeMap.firstEntry / ConcurrentSkipListMap (optimized)
- O(log n) order insertion at correct price level
- Per-symbol matching lock — symbols match concurrently; same-symbol matches are serialized
- Atomic partial-fill updates via CAS — never over-fills under concurrency
- Thread-safe trade recording

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Observer | Trader notifications | Real-time trade execution alerts |
| Facade | StockExchange | Unified API for order submission |

---

## Folder Structure
```
25-stock-exchange/
├── README.md
├── VARIATIONS.md
├── naive/
│   ├── model/        ← Order, LimitOrder, MarketOrder, Stock, Trader, Trade, enums
│   ├── service/      ← OrderBook (ArrayList), MatchingEngine, StockExchange
│   └── Main.java
└── optimized/
    ├── model/        ← Same entities
    ├── service/      ← OrderBook (TreeMap), MatchingEngine, StockExchange
    └── Main.java
```

### How to Run
```bash
# Naive
cd problems/25-stock-exchange/naive
mkdir -p out && javac -d out model/*.java service/*.java Main.java && java -cp out Main

# Optimized
cd problems/25-stock-exchange/optimized
mkdir -p out && javac -d out model/*.java service/*.java Main.java && java -cp out Main
```

### Naive vs Optimized
| Operation | Naive | Optimized |
|-----------|-------|-----------|
| Best price access | O(n) scan of unsorted list | O(1) TreeMap first entry |
| Order insertion | O(1) list append | O(log n) TreeMap insert |
| Order matching | O(n) per match attempt | O(log n) with price-time priority |
| Price levels | Not tracked | TreeMap<Price, Queue<Order>> |

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** Order matching with concurrent submissions — partial fills, out-of-order execution.

```bash
cd concurrent
mkdir -p out
javac -d out model/*.java service/*.java Main.java
java -cp out Main
```

### Key Concurrency Techniques Used
| Technique | Where | Why |
|-----------|-------|-----|
| ConcurrentSkipListMap | ConcurrentOrderBook | Lock-free sorted price levels for bid/ask |
| ReentrantLock per book | ConcurrentOrderBook.matchLock | Serialize matching within same symbol |
| AtomicInteger (CAS loop) | Order.fill() | Atomic partial fill — prevents over-fill under concurrency |
| AtomicReference | Order.status | Thread-safe state transitions (PENDING -> PARTIAL -> FILLED) |
| CopyOnWriteArrayList | MatchingEngine.trades | Safe concurrent trade recording |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

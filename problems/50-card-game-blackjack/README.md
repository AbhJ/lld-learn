# Card Game - Blackjack


## Problem Statement
Design a single-player blackjack game with a dealer. The player places bets, is dealt two cards, then chooses hit or stand. The dealer follows a configurable strategy (e.g. hit until 17). Hand scores handle aces as 1 or 11; bets are paid out by game outcome.

The optimized variant uses Fisher-Yates deck shuffle, memoized hand scores, and a strategy lookup table for the dealer.

## Requirements

### Functional Requirements
- Place a bet from the player balance
- Deal two cards to player and dealer
- Hit (draw) and stand actions
- Compute hand score with ace flexibility (1 or 11)
- Dealer follows a configurable play strategy
- Resolve outcome and adjust player balance
- Start a new round with the same player

### Non-functional Requirements
- Deterministic shuffle via seed for testability
- O(1) hand score updates (optimized: memoized)

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Strategy | DealerStrategy (Standard, table-driven) | Swappable dealer decision logic |
| State | GameState (Betting, Playing, Dealer, Done) | Valid actions depend on current game phase |
| Facade | BlackjackGame | Single API orchestrating deck, dealer, player, bets |
| Factory | Deck (builds 52 cards) | Encapsulates deck construction and shuffling |

## Folder Structure

```
50-card-game-blackjack/
├── README.md
├── VARIATIONS.md
├── naive/
│   ├── model/        ← Card, Deck, Hand, Player, Bet, Suit, Rank, GameState
│   ├── service/      ← BlackjackGame, Dealer
│   ├── strategy/     ← DealerStrategy (Standard)
│   └── Main.java
└── optimized/
    ├── model/        ← Card, Deck (Fisher-Yates), Hand (memoized), Player, Bet, Suit, Rank, GameState
    ├── service/      ← BlackjackGame, Dealer
    ├── strategy/     ← DealerStrategy (Standard, Aggressive), BasicStrategyChartDealer (2D chart lookup)
    └── Main.java
```

## How to Run

```bash
# Naive
cd problems/50-card-game-blackjack/naive
mkdir -p out && javac -d out model/*.java strategy/*.java service/*.java Main.java && java -cp out Main

# Optimized
cd problems/50-card-game-blackjack/optimized
mkdir -p out && javac -d out model/*.java strategy/*.java service/*.java Main.java && java -cp out Main
```

## Naive vs Optimized

| Aspect | Naive | Optimized |
|--------|-------|-----------|
| Deck shuffle | Collections.shuffle (creates new ArrayList) | Fisher-Yates in-place on fixed array (no GC) |
| Hand score | Recomputes from all cards every call | Memoized: cached, invalidated only on addCard |
| Dealer decision logic | Single threshold (`hit < 17`) | **Basic-strategy chart**: pluggable `BasicStrategyChartDealer` consults a 2D `Action[][]` table indexed by (current-total, opponent-upcard) and returns HIT / STAND / DOUBLE — the real expected-value-optimal action from blackjack basic strategy (hard totals 8-21 vs upcards 2-A). `StandardDealerStrategy` (`score < 17`) remains as the default; `Main` runs both side-by-side. |
| Deck reset | buildDeck() allocates new Card objects | Reshuffles existing cards in-place |

---

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** Multiple players at same table hitting/standing simultaneously — deck deals same card twice.

```bash
cd concurrent
mkdir -p out
find . -name '*.java' | xargs javac -d out
java -cp out Main
```

### Key Concurrency Techniques Used
| Technique | Where | Why |
|-----------|-------|-----|
| synchronized (dealLock) | Deck.deal() | Only one thread draws at a time — no duplicate cards |
| AtomicInteger | Deck.position | Atomic deck pointer ensures consistent card counting |
| Immutable Card | Card class | Thread-safe by construction — dealt cards cannot be mutated |
| ConcurrentHashMap | Main (playerHands) | Each player's hand safely accumulated from concurrent deals |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

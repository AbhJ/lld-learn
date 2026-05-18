# Snake and Ladder Game


## Problem Statement
Design a multiplayer Snake and Ladder game with a configurable board. Players take turns rolling dice and moving across a numbered board. Landing on a ladder's start moves the player up to its end, while landing on a snake's head moves the player down to its tail.

The game supports multiple players, configurable board size, different dice types (normal, crooked), and a factory pattern for creating various board configurations.

## Requirements

### Functional Requirements
- Support 2+ players taking turns
- Configurable board size (default 100)
- Place snakes (head -> tail, goes down) and ladders (start -> end, goes up)
- Roll dice to determine movement
- Automatic snake/ladder application on landing
- Win detection (exact landing on last cell)
- Support different dice types (normal 1-6, crooked even-only)
- Game history/move log

### Non-functional Requirements
- Configurable board via factory
- Extensible dice strategies
- Template method for game flow
- Fair random dice rolling

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Template Method | `AbstractBoardGame.playTurn()` skeleton with abstract `applyMove`/`checkVictory` and `onBeforeMove`/`onAfterMove`/`onVictory` hooks; `SnakeAndLadderGame` provides snake/ladder-specific steps | Define the invariant turn flow once, let subclasses customise individual steps |
| Strategy | `Dice` interface (`NormalDice`, `CrookedDice`, `FixedDice`) | Swap dice implementations |
| Factory | `BoardFactory` | Create different board configurations |

## Folder Structure
```
06-snake-and-ladder/
├── naive/          <- Start here. Cell[] array with object delegation.
│   ├── model/      -> Data classes (Snake, Ladder, Cell, Board, Player)
│   ├── service/    -> Business logic (AbstractBoardGame template + SnakeAndLadderGame)
│   ├── strategy/   -> Swappable algorithms (Dice, BoardFactory)
│   └── Main.java   -> Entry point — run this
└── optimized/      <- Production-grade. HashMap jumps for O(1) position resolution.
    ├── model/
    ├── service/
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

### Naive vs Optimized — What Changes?
| Operation | Naive | Optimized |
|-----------|-------|-----------|
| Board storage | Cell[] array with Snake/Ladder objects | HashMap<Integer,Integer> jump map |
| Position resolution | Cell.getFinalPosition() via object delegation | O(1) HashMap.get() lookup |
| Memory usage | N Cell objects + Snake/Ladder objects | Single HashMap with jump entries only |
| Board creation | Factory creates Cell objects | Factory populates jump map directly |

---

## Class Diagram (Text)
```
AbstractBoardGame (Template Method: playTurn)
└── SnakeAndLadderGame (concrete subclass: applyMove / checkVictory / hooks)
├── Board
│   ├── Cell[] / HashMap<Integer,Integer>
│   ├── Snake[] (head, tail)
│   └── Ladder[] (start, end)
├── Player[]
├── Dice (interface)
│   ├── NormalDice
│   └── CrookedDice
└── BoardFactory (creates board configurations)
```

## Key Design Decisions
- Exact landing: player must roll exact number to reach 100 (otherwise stays)
- Snakes and ladders don't chain (land once, apply once)
- Board validates no snake/ladder overlap
- Template method allows customizing each step (logging, animation, etc.)

## Interview Tips
- Start with game rules clarification
- Separate Board (data) from Game (logic)
- Discuss validation: no snake at position 100, no ladder starting where snake heads are
- Talk about fairness: dice probability affects game length
- Consider extensions: power-ups, multiple dice, special cells

---

## Concurrency Version

**Race condition:** In online multiplayer, two players rolling dice at the same time and both moving "at the same turn" — violating strict turn order.

```bash
cd concurrent
mkdir -p out
find . -name '*.java' | xargs javac -d out
java -cp out Main
```

### Key Concurrency Techniques
| Technique | Where | Why |
|-----------|-------|-----|
| AtomicInteger | Game.currentPlayerIndex | CAS to claim your turn — only the correct player can move |
| ReentrantLock | Game.moveLock | Ensures CAS + move execution + log addition are atomic (preserves log order) |
| Fast rejection | attemptMove() pre-check | Quick non-blocking check before acquiring lock reduces contention |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

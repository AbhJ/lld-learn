# Tic Tac Toe


## Problem Statement
Design an NxN Tic Tac Toe game supporting two players (human or AI). Players alternate placing their symbols (X or O) on a board. The game detects wins (complete row, column, or diagonal) and draws (board full with no winner).

The system supports variable board sizes, a command pattern for move/undo functionality, and different player types (human with specified moves, AI with random valid moves). Win checking handles arbitrary NxN boards.

## Requirements

### Functional Requirements
- Support NxN board (default 3x3)
- Two players alternating turns with X and O symbols
- Place symbol at specified row, column position
- Detect win: complete row, column, or diagonal
- Detect draw: board full with no winner
- Undo last move (Command pattern)
- Support AI player (random valid moves)
- Validate moves: position must be empty and in bounds

### Non-functional Requirements
- Extensible player types (Strategy pattern)
- Observable game state changes
- Command pattern for undo support
- Efficient win checking

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Strategy | Player types (Human, AI) | Different move selection strategies |
| Observer | Game state notification (`GameObserver` -> `ConsoleGameObserver`) | Notify UI/logs of moves and results |
| Command | Move class with undo | Support move reversal |

## Folder Structure
```
07-tic-tac-toe/
├── naive/          <- Start here. O(N) full-board scan for win check.
│   ├── model/      -> Data classes (Symbol, Board, Move, Player)
│   ├── service/    -> Business logic (WinChecker, Game)
│   └── Main.java   -> Entry point — run this
└── optimized/      <- Production-grade. O(1) win detection with row/col/diag counters.
    ├── model/
    ├── service/
    └── Main.java
```

### How to Run
```bash
# Run the naive (simple) version:
cd naive
mkdir -p out
javac -d out model/*.java service/*.java Main.java
java -cp out Main

# Run the optimized version:
cd optimized
mkdir -p out
javac -d out model/*.java service/*.java Main.java
java -cp out Main
```

### Naive vs Optimized — What Changes?
| Operation | Naive | Optimized |
|-----------|-------|-----------|
| Win detection | O(N) scan of full row/col/diag | O(1) counter check at move position |
| Data structures | 2D array only | 2D array + rowCounts[] + colCounts[] + diagCount + antiDiagCount |
| Check method | WinChecker.checkWinner() scans all | Board.checkWinAt(row,col) checks if counter == size |
| Undo support | Remove from board | Remove from board + decrement counters |

---

## Class Diagram (Text)
```
Game (Controller)
├── Board (NxN grid)
├── Player (abstract)
│   ├── HumanPlayer (specified moves)
│   └── AIPlayer (random moves)
├── Move (Command: execute/undo)
├── WinChecker (rows, cols, diagonals)
└── Symbol (enum: X, O)
```

## Key Design Decisions
- Board uses 2D array with Symbol enum (X, O, or null for empty)
- WinChecker is separate class for single responsibility
- Move stores row, col, and symbol for undo
- AI player picks random empty cell (simple strategy)

## Interview Tips
- Start with board representation and move validation
- Discuss win-checking efficiency: O(N) per check vs O(1) with counters
- Talk about undo: stack of moves, command pattern
- Consider scalability: NxN with N-in-a-row win condition
- Extension: minimax AI, network multiplayer

---

## Concurrency Version

**Race condition:** Two players placing moves at the exact same time on the last winning cell — one overwrites the other, or both claim victory.

```bash
cd concurrent
mkdir -p out
find . -name '*.java' | xargs javac -d out
java -cp out Main
```

### Key Concurrency Techniques
| Technique | Where | Why |
|-----------|-------|-----|
| AtomicReference\<Symbol\>\[\]\[\] | Board.cells | CAS to place: compareAndSet(EMPTY, X) prevents cell overwrite |
| ReentrantLock | Game.turnLock | Enforces alternating turns — only one player acts at a time |
| Double-check pattern | Game.attemptMove() | Checks gameOver before and after acquiring lock to avoid unnecessary work |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

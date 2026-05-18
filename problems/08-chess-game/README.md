# Chess Game


## Problem Statement
Design a standard chess game with proper piece movement validation, check/checkmate detection, and move history. The system supports two players (White and Black) alternating turns on an 8x8 board.

Each piece type (King, Queen, Rook, Bishop, Knight, Pawn) has distinct movement rules. The system validates moves for legality (piece movement rules, path obstruction, not moving into check), detects check and checkmate, and maintains a move history with undo capability.

## Requirements

### Functional Requirements
- Standard 8x8 chess board with initial piece placement
- Six piece types with correct movement rules
- Move validation: piece rules, path clear, not moving into check
- Capture opponent's pieces
- Check detection: warn when king is threatened
- Basic game flow: alternate turns, validate, detect end
- Move history and undo (Command pattern)
- Display board state

### Non-functional Requirements
- Extensible piece movement (Strategy pattern)
- Command pattern for move history
- Clear separation of concerns (Board, Piece, Validation)
- Factory for initial board setup

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Strategy | Piece movement rules | Each piece has unique movement logic |
| Factory | Piece creation, board setup | Standard initial piece placement |
| Command | Move/undo | Record and reverse moves |
| Observer | Check notification | Alert when king is in check |

## Folder Structure
```
08-chess-game/
├── naive/          <- Start here. Iterates all pieces for attack detection.
│   ├── model/      -> Data classes (Position, Player, Piece types, Move, Board)
│   ├── service/    -> Business logic (MoveValidator, Game)
│   └── Main.java   -> Entry point — run this
└── optimized/      <- Production-grade. Directional attack-map scanning.
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
| isSquareAttacked | Iterate ALL opponent pieces | Check only pieces that could reach target by type |
| Knight check | Scan all pieces, filter knights | Check 8 L-shaped offsets directly |
| Diagonal check | Scan all pieces for bishops/queens | Ray-cast along 4 diagonals until hit |
| Straight check | Scan all pieces for rooks/queens | Ray-cast along 4 cardinal dirs until hit |
| King adjacency | Iterate all pieces | Check 8 adjacent squares directly |

---

## Class Diagram (Text)
```
Game (Controller)
├── Board (8x8 grid of Pieces)
├── Piece (abstract)
│   ├── King, Queen, Rook, Bishop, Knight, Pawn
├── Position (row, col)
├── Move (Command: from, to, captured piece)
├── Player (WHITE, BLACK)
└── MoveValidator (validates legality)
```

## Key Design Decisions
- Position uses (row, col) with row 0 = rank 8 (top, black side)
- Each piece validates its own movement pattern
- MoveValidator checks: piece rules + path clear + not moving into check
- Simplified: no castling, en passant, or pawn promotion for clarity
- Check detection: scan all opponent pieces for king threats

## Interview Tips
- Start with piece hierarchy and movement rules
- Discuss validation layers: piece rules, obstruction, check
- Mention special moves as extensions (castling, en passant, promotion)
- Talk about check/checkmate algorithm complexity
- Consider optimization: maintaining attack maps

---

## Concurrency Version

**Race condition:** In a timed game, one thread processing a move while timer thread declares timeout — inconsistent game state where both a move result and timeout are recorded.

```bash
cd concurrent
mkdir -p out
find . -name '*.java' | xargs javac -d out
java -cp out Main
```

### Key Concurrency Techniques
| Technique | Where | Why |
|-----------|-------|-----|
| AtomicReference\<GameState\> | GameController.state | CAS for state transitions — only one event (move or timeout) determines outcome |
| synchronized(board) | GameController.makeMove() | Protects board mutation while allowing timer CAS to race independently |
| CAS exclusivity | declareTimeout() / makeMove() | compareAndSet(PLAYING, X) — exactly one wins the race, other is rejected |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

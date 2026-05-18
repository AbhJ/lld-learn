# Chess Game - Variations

## Variation 1: Chess960 (Fischer Random)
**Learning Value:** Teaches randomized initialization, flexible validation, and separation of rules from board setup.

### Additional Requirements
- Randomized back rank positions (960 possible arrangements)
- Constraints: bishops on opposite colors, king between rooks
- Modified castling rules (king and rook end on standard squares)
- Position generation and validation

### Design Changes
- Add `Chess960Setup` generating valid random positions
- Modify `CastlingRule` to handle non-standard king/rook positions
- Add `PositionValidator` ensuring bishop color and king-between-rooks constraints
- Modify `Board.initialize()` to accept custom starting position

### Solution Approach
Generate random positions satisfying: (1) one bishop on light square, one on dark, (2) king between the two rooks, (3) remaining pieces placed randomly. Store original rook/king positions for castling rights. Castling in Chess960: king and rook end on the same squares as standard chess (c1/g1 for king, d1/f1 for rook), regardless of starting position. All squares between king's start/end AND rook's start/end must be empty (except the pieces themselves). Generate all 960 positions at startup for random selection.

### Key Classes to Add
```java
public class Chess960Setup {
    private static final List<int[]> ALL_960_POSITIONS = generateAll960();

    public Piece[] generateRandomBackRank() {
        int index = new Random().nextInt(960);
        return createPiecesFromTemplate(ALL_960_POSITIONS.get(index));
    }

    private static List<int[]> generateAll960() {
        // Generate all valid arrangements:
        // Place bishops on opposite colors, king between rooks
        ...
    }
}

public class Chess960CastlingRule extends CastlingRule {
    private Position originalKingPos;
    private Position originalKingRookPos;
    private Position originalQueenRookPos;

    @Override
    public boolean canCastle(Board board, Color color, CastleSide side) {
        // Check: no pieces between king start/end AND rook start/end
        // King doesn't pass through check
        ...
    }

    @Override
    public void executeCastle(Board board, Color color, CastleSide side) {
        // Move king to standard target (c1 or g1)
        // Move rook to standard target (d1 or f1)
    }
}
```

---

## Variation 2: Timed Chess (Blitz/Bullet)
**Learning Value:** Introduces time management systems, clock synchronization, and timeout-driven game state transitions.

### Additional Requirements
- Time controls: Bullet (1min), Blitz (3+2), Rapid (10+5), Classical (30+30)
- Clock management with increment per move
- Flag fall (timeout) ends game immediately
- Low-time warnings and pre-move functionality

### Design Changes
- Add `ChessClock` with dual timers
- Add `TimeControl` configuration object
- Add `FlagFall` event handler
- Modify game loop to check clock before and after each move

### Solution Approach
`ChessClock` runs two independent timers. On move completion: stop active player's clock, add increment, start opponent's clock. A background thread monitors for flag fall (time = 0). On flag fall: if opponent has sufficient mating material, they win; otherwise draw (FIDE rules). Pre-move: allow player to queue a move before it's their turn (executes instantly when clock switches). Time pressure affects UI but not game logic. Support time odds (giving weaker player more time).

### Key Classes to Add
```java
public class ChessClock {
    private long[] remainingMs; // [WHITE, BLACK]
    private Color activeColor;
    private long lastTickTime;
    private TimeControl timeControl;
    private List<ClockListener> listeners;

    public void pressClock() { // called after a move
        long elapsed = System.currentTimeMillis() - lastTickTime;
        remainingMs[activeColor.ordinal()] -= elapsed;
        remainingMs[activeColor.ordinal()] += timeControl.getIncrementMs();
        activeColor = activeColor.opposite();
        lastTickTime = System.currentTimeMillis();
    }

    public boolean isFlagged(Color color) {
        return getRemaining(color) <= 0;
    }
}

public class TimeControl {
    private long initialTimeMs;
    private long incrementMs;
    private TimeControlType type; // BULLET, BLITZ, RAPID, CLASSICAL

    public static TimeControl BLITZ_3_2 = new TimeControl(180_000, 2_000, BLITZ);
    public static TimeControl BULLET_1_0 = new TimeControl(60_000, 0, BULLET);
}
```

---

## Variation 3: Chess Puzzles Mode
**Learning Value:** Practices puzzle state management, solution validation, and difficulty rating systems.

### Additional Requirements
- Mate in N puzzles (find forced checkmate)
- Best move finder from any position
- Puzzle difficulty rating (ELO-based)
- Hint system with progressive reveal

### Design Changes
- Add `PuzzleEngine` loading positions and expected solutions
- Add `Puzzle` with position, solution line, difficulty
- Add `HintSystem` revealing solution incrementally
- Add `PuzzleSolver` using game tree search

### Solution Approach
A `Puzzle` stores a FEN position, the correct move sequence (solution), and distractors. `PuzzleSolver` validates player moves against the solution tree. For "mate in N": use depth-limited minimax - search all forcing sequences up to depth 2N (N moves for each side). `HintSystem` reveals: (1) which piece to move, (2) which square to target, (3) full move. Rate puzzles based on solve success rate. Track player puzzle ELO separately from game ELO.

### Key Classes to Add
```java
public class Puzzle {
    private String puzzleId;
    private String fen; // starting position
    private List<String> solutionMoves; // e.g., ["Qh7+", "Kf8", "Qf7#"]
    private int difficultyRating;
    private PuzzleType type; // MATE_IN_1, MATE_IN_2, BEST_MOVE, ENDGAME

    public boolean validateMove(int moveIndex, String playerMove) {
        return solutionMoves.get(moveIndex).equals(playerMove);
    }
}

public class PuzzleSolver {
    public List<String> findMateInN(Board board, Color toMove, int n) {
        // Depth-limited search for forced checkmate
        // Returns the sequence of moves or empty if no mate exists
        ...
    }

    public String findBestMove(Board board, Color toMove, int searchDepth) {
        // Alpha-beta search with position evaluation
        ...
    }
}

public class HintSystem {
    private int hintsUsed;
    private static final int MAX_HINTS = 3;

    public String getHint(Puzzle puzzle, int moveIndex, int hintLevel) {
        switch (hintLevel) {
            case 1: return "Move the " + getPieceType(puzzle, moveIndex);
            case 2: return "Move to " + getTargetSquare(puzzle, moveIndex);
            case 3: return puzzle.getSolutionMoves().get(moveIndex);
            default: return "";
        }
    }
}
```

---

## Variation 4: Tournament Management
**Learning Value:** Explores trade-offs between scalability and fairness in bracket design and ELO-based matchmaking.

### Additional Requirements
- Swiss system pairing (players with same score play each other)
- Round-robin format for small groups
- ELO rating calculation after each game
- Tiebreak rules (Buchholz, Sonneborn-Berger)

### Design Changes
- Add `Tournament` with format configuration
- Add `SwissPairing` algorithm implementation
- Add `ELOCalculator` for rating adjustments
- Add `TiebreakCalculator` with multiple criteria

### Solution Approach
`Tournament` manages rounds. Swiss pairing: sort players by score, pair top-half with bottom-half of each score group, avoid repeat matchups, alternate colors. Round-robin: generate all pairings upfront using circle method. After each game, update ELO: K-factor varies by rating (higher K for newer players). Tiebreaks computed at end: Buchholz (sum of opponents' scores), Sonneborn-Berger (sum of beaten opponents' scores + half of drawn opponents' scores).

### Key Classes to Add
```java
public class Tournament {
    private TournamentFormat format;
    private List<TournamentPlayer> players;
    private List<Round> rounds;
    private int totalRounds;

    public Round generateNextRound() {
        if (format == TournamentFormat.SWISS) {
            return new SwissPairing(players, rounds).generatePairings();
        } else {
            return new RoundRobinPairing(players, rounds).generatePairings();
        }
    }
}

public class SwissPairing {
    public Round generatePairings() {
        List<TournamentPlayer> sorted = sortByScore(players);
        List<Pairing> pairings = new ArrayList<>();
        // Group by score, pair top half vs bottom half within each group
        // Avoid rematch, alternate colors
        ...
        return new Round(pairings);
    }
}

public class ELOCalculator {
    public static RatingChange calculate(int whiteRating, int blackRating, GameResult result) {
        double expectedWhite = 1.0 / (1 + Math.pow(10, (blackRating - whiteRating) / 400.0));
        double scoreWhite = result == WHITE_WIN ? 1 : result == DRAW ? 0.5 : 0;
        int kFactor = getKFactor(whiteRating);
        int whiteChange = (int)(kFactor * (scoreWhite - expectedWhite));
        return new RatingChange(whiteChange, -whiteChange);
    }
}
```

---

## Variation 5: Variant - Crazyhouse
**Learning Value:** Deepens understanding of rule extension, piece lifecycle management, and variant polymorphism.

### Additional Requirements
- Captured pieces switch color and can be "dropped" back on board
- Drops count as a move (place captured piece on any empty square)
- Pawns cannot be dropped on 1st or 8th rank
- Dropped pieces lose prior move history (no castling for dropped rooks)

### Design Changes
- Add `PieceReserve` (pocket) tracking captured pieces per player
- Add `DropMove` as a new move type alongside regular moves
- Modify capture logic to transfer piece to capturer's reserve
- Add drop validation rules (no pawn drops on back ranks)

### Solution Approach
Each player has a `PieceReserve` (pocket). When a piece is captured, it changes color and goes to capturer's reserve. On their turn, instead of moving a board piece, a player can "drop" a reserve piece onto any empty square (with restrictions: pawns not on 1st/8th rank, drop cannot give immediate checkmate in some variants). This dramatically changes strategy - material advantage means more drops available. Modify move generation to include all valid drops as legal moves. Check/checkmate detection must consider both board moves and drops.

### Key Classes to Add
```java
public class PieceReserve {
    private Map<PieceType, Integer> pieces; // piece type -> count in reserve

    public void addCaptured(Piece piece) {
        pieces.merge(piece.getType(), 1, Integer::sum);
    }

    public boolean hasPiece(PieceType type) {
        return pieces.getOrDefault(type, 0) > 0;
    }

    public void removePiece(PieceType type) {
        pieces.merge(type, -1, Integer::sum);
    }
}

public class DropMove extends Move {
    private PieceType pieceType;
    private Position targetSquare;

    @Override
    public boolean isValid(Board board, PieceReserve reserve) {
        if (!reserve.hasPiece(pieceType)) return false;
        if (!board.isEmpty(targetSquare)) return false;
        if (pieceType == PAWN && (targetSquare.getRank() == 1 || targetSquare.getRank() == 8)) 
            return false;
        return true;
    }

    @Override
    public void execute(Board board, PieceReserve reserve) {
        reserve.removePiece(pieceType);
        board.placePiece(new Piece(pieceType, color), targetSquare);
    }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

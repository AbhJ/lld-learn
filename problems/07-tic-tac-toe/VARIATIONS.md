# Tic Tac Toe - Variations

## Variation 1: Ultimate Tic-Tac-Toe (9x9)
**Learning Value:** Teaches recursive game structures, nested board management, and meta-level win conditions.

### Additional Requirements
- 3x3 grid of 3x3 boards (9 small boards total)
- Win a small board to claim that cell in the meta-board
- Your move determines which small board opponent must play in
- Win by getting 3 in a row on the meta-board

### Design Changes
- Add `MetaBoard` containing 9 `SmallBoard` instances
- Add `MetaCell` tracking which small boards are won
- Modify move validation to enforce "must play in designated board"
- Add `UltimateBoardWinChecker` checking both levels

### Solution Approach
`MetaBoard` holds a 3x3 array of `SmallBoard`. Each small board is a standard 3x3 game. When a player places their mark at position (r,c) within a small board, the opponent must play in small board at position (r,c) in the meta-grid. If that small board is already won/full, opponent can play anywhere. Winning a small board claims that meta-cell. Check meta-board for 3-in-a-row to determine game winner. Key complexity: tracking which board is active and handling the "play anywhere" exception.

### Key Classes to Add
```java
public class UltimateGame {
    private SmallBoard[][] metaGrid; // 3x3 of SmallBoards
    private Symbol[][] metaBoardState; // which player won each small board
    private int activeRow, activeCol; // which small board must be played in (-1 = any)

    public boolean makeMove(int metaRow, int metaCol, int cellRow, int cellCol, Symbol symbol) {
        if (activeRow != -1 && (metaRow != activeRow || metaCol != activeCol)) return false;
        SmallBoard target = metaGrid[metaRow][metaCol];
        if (target.isWon()) return false;
        target.place(cellRow, cellCol, symbol);
        if (target.checkWin(symbol)) metaBoardState[metaRow][metaCol] = symbol;
        activeRow = cellRow;
        activeCol = cellCol;
        if (metaGrid[activeRow][activeCol].isWon()) { activeRow = -1; activeCol = -1; }
        return true;
    }
}

public class SmallBoard {
    private Symbol[][] cells;
    private boolean won;
    private Symbol winner;
}
```

---

## Variation 2: 3D Tic-Tac-Toe (4x4x4)
**Learning Value:** Introduces multi-dimensional data structures and spatial reasoning in 3D game logic.

### Additional Requirements
- 4x4x4 three-dimensional board (64 cells)
- Win by getting 4 in a row along any axis or diagonal
- 76 possible winning lines to check
- Much deeper strategy than standard game

### Design Changes
- Add `Board3D` with 3D array representation
- Add `WinChecker3D` handling all 76 winning lines
- Modify rendering for 3D visualization (layer by layer)
- Add pre-computed winning line sets for efficiency

### Solution Approach
Represent board as `Symbol[4][4][4]`. Pre-compute all 76 winning lines: 16 rows per layer (48), 16 columns through layers (16), 4 pillar diagonals, plus 8 space diagonals across all three dimensions. For each move, only check winning lines that include that cell (typically 4-7 lines per cell). Use a lookup table mapping each cell to its relevant winning lines for O(1) win checking. The increased branching factor makes AI opponents computationally interesting (alpha-beta pruning essential).

### Key Classes to Add
```java
public class Board3D {
    private Symbol[][][] cells; // [layer][row][col]
    private static final List<int[][]> WINNING_LINES = precomputeWinningLines();

    public boolean checkWin(int layer, int row, int col, Symbol symbol) {
        return getWinningLinesThrough(layer, row, col).stream()
            .anyMatch(line -> allMatch(line, symbol));
    }

    private static List<int[][]> precomputeWinningLines() {
        List<int[][]> lines = new ArrayList<>();
        // rows, columns, pillars, and diagonals in all 3 planes + space diagonals
        // Total: 76 lines
        ...
        return lines;
    }
}

public class WinChecker3D {
    private Map<Integer, List<int[][]>> cellToLines; // cell index -> relevant winning lines

    public boolean isWinningMove(Board3D board, int l, int r, int c, Symbol symbol) {
        int cellIndex = l * 16 + r * 4 + c;
        return cellToLines.get(cellIndex).stream()
            .anyMatch(line -> board.allMatch(line, symbol));
    }
}
```

---

## Variation 3: Multiplayer (3+ Players)
**Learning Value:** Practices dynamic player management, flexible turn ordering, and scalable win-condition checks.

### Additional Requirements
- Support 3-5 players with unique symbols
- Larger board to accommodate more players (5x5 or 6x6)
- Configurable win length (3, 4, or 5 in a row)
- Alliance/blocking mechanics between players

### Design Changes
- Add configurable `GameConfig` (board size, player count, win length)
- Modify `WinChecker` for variable win lengths
- Add more `Symbol` options (X, O, #, @, *)
- Add `TurnManager` for round-robin turn order

### Solution Approach
Generalize the game with `GameConfig` specifying N players, MxM board, and K-in-a-row win condition. Common configs: 3 players on 5x5 with 4-in-a-row, or 4 players on 6x6 with 4-in-a-row. Win checker slides a window of size K across all rows, columns, and diagonals. Turn order is round-robin. A player is eliminated if they cannot prevent another from winning (optional rule). Last player standing or first to get K-in-a-row wins.

### Key Classes to Add
```java
public class GameConfig {
    private int boardSize;
    private int winLength;
    private int playerCount;
    private List<Symbol> symbols;

    public static GameConfig standard() { return new GameConfig(3, 3, 2, List.of(X, O)); }
    public static GameConfig threePlayer() { return new GameConfig(5, 4, 3, List.of(X, O, HASH)); }
}

public class MultiPlayerGame {
    private GameConfig config;
    private Symbol[][] board;
    private List<Player> players;
    private int currentPlayerIndex;

    public void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    public boolean checkWin(int row, int col) {
        Symbol s = board[row][col];
        return checkDirection(row, col, s, 1, 0)  // horizontal
            || checkDirection(row, col, s, 0, 1)  // vertical
            || checkDirection(row, col, s, 1, 1)  // diagonal
            || checkDirection(row, col, s, 1, -1); // anti-diagonal
    }
}
```

---

## Variation 4: Timed Moves
**Learning Value:** Explores trade-offs between time pressure and decision quality using timer-driven state transitions.

### Additional Requirements
- Each player has a total time bank (e.g., 5 minutes)
- Lose on timeout (opponent wins)
- Optional increment per move (e.g., +3 seconds)
- Visual countdown timer display

### Design Changes
- Add `GameClock` with per-player time tracking
- Add `TimeControl` configuration (total time, increment)
- Add `ClockDisplay` for countdown visualization
- Modify game loop to check timeout before accepting moves

### Solution Approach
Each player gets a `GameClock` initialized with their time bank. When it's a player's turn, their clock starts counting down. On making a move, their clock stops and increment is added. If clock reaches zero, game ends with timeout loss. `TimeControl` defines formats: Bullet (1+0), Blitz (3+2), Rapid (10+5). The game loop checks `isTimeExpired()` before processing any move. Use `System.nanoTime()` for precise measurement.

### Key Classes to Add
```java
public class GameClock {
    private Map<Player, Long> remainingTimeMs;
    private Player activePlayer;
    private long turnStartTime;
    private long incrementMs;

    public void startTurn(Player player) {
        activePlayer = player;
        turnStartTime = System.currentTimeMillis();
    }

    public void endTurn(Player player) {
        long elapsed = System.currentTimeMillis() - turnStartTime;
        remainingTimeMs.merge(player, -(elapsed - incrementMs), Long::sum);
    }

    public boolean isTimeExpired(Player player) {
        if (player != activePlayer) return remainingTimeMs.get(player) <= 0;
        long elapsed = System.currentTimeMillis() - turnStartTime;
        return remainingTimeMs.get(player) - elapsed <= 0;
    }
}

public class TimeControl {
    private long initialTimeMs;
    private long incrementMs;

    public static TimeControl BULLET = new TimeControl(60_000, 0);
    public static TimeControl BLITZ = new TimeControl(180_000, 2_000);
    public static TimeControl RAPID = new TimeControl(600_000, 5_000);
}
```

---

## Variation 5: AI Difficulty Levels
**Learning Value:** Deepens understanding of AI strategy patterns, difficulty scaling, and minimax algorithm design.

### Additional Requirements
- Easy: random valid moves
- Medium: blocks opponent wins, takes winning moves
- Hard: minimax with alpha-beta pruning (unbeatable)
- Adjustable difficulty mid-game

### Design Changes
- Add `AIPlayer extends Player` with strategy selection
- Add `AIStrategy` interface with difficulty implementations
- Add `MinimaxEngine` for Hard difficulty
- Add `DifficultyLevel` enum controlling behavior

### Solution Approach
Implement `AIStrategy` interface with three implementations. Easy: pick random empty cell. Medium: (1) if can win in one move, take it; (2) if opponent can win in one move, block it; (3) prefer center, then corners, then edges. Hard: full minimax with alpha-beta pruning - evaluate all possible games to terminal state, assign +10 for win, -10 for loss, 0 for draw, choose move maximizing minimum outcome. For larger boards, limit search depth and use heuristic evaluation.

### Key Classes to Add
```java
public interface AIStrategy {
    int[] getMove(Symbol[][] board, Symbol mySymbol);
}

public class MinimaxAI implements AIStrategy {
    @Override
    public int[] getMove(Symbol[][] board, Symbol mySymbol) {
        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = null;
        for (int[] move : getEmptyCells(board)) {
            board[move[0]][move[1]] = mySymbol;
            int score = minimax(board, 0, false, mySymbol, Integer.MIN_VALUE, Integer.MAX_VALUE);
            board[move[0]][move[1]] = null;
            if (score > bestScore) { bestScore = score; bestMove = move; }
        }
        return bestMove;
    }

    private int minimax(Symbol[][] board, int depth, boolean isMaximizing, 
                        Symbol aiSymbol, int alpha, int beta) {
        // Terminal checks, recursive evaluation with alpha-beta pruning
        ...
    }
}

public class MediumAI implements AIStrategy {
    @Override
    public int[] getMove(Symbol[][] board, Symbol mySymbol) {
        // 1. Win if possible, 2. Block opponent, 3. Strategic placement
        ...
    }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

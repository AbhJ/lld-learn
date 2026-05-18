# Snake and Ladder - Variations

## Variation 1: Power-ups
**Learning Value:** Teaches extensible game mechanics, item effect systems, and temporary state modifiers.

### Additional Requirements
- Double dice power-up: roll twice in one turn
- Shield: immune to snakes for 1 turn
- Teleport: move to any square of choice
- Power-ups placed on specific board squares or earned randomly

### Design Changes
- Add `PowerUp` abstract class with subclasses (DoubleDice, Shield, Teleport)
- Add `PlayerInventory` to track collected power-ups
- Modify `Player` to use power-ups before/after rolling
- Add `PowerUpSquare` on the board that grants power-ups

### Solution Approach
Place `PowerUpSquare` tiles at specific positions on the board. When a player lands on one, they collect the power-up into their `PlayerInventory` (max capacity enforced). Before each turn, player can choose to activate a power-up: Shield sets a flag that bypasses snake check for this turn; DoubleDice allows two consecutive rolls; Teleport lets player choose destination (with restrictions like not final square). Use Strategy pattern for power-up effects to make them extensible.

### Key Classes to Add
```java
public abstract class PowerUp {
    private String name;
    private String description;
    private int usesRemaining;

    public abstract void apply(Player player, Board board, GameContext context);
    public boolean isUsable() { return usesRemaining > 0; }
}

public class Shield extends PowerUp {
    @Override
    public void apply(Player player, Board board, GameContext context) {
        context.setSnakeImmunity(player, 1); // immune for 1 turn
    }
}

public class PlayerInventory {
    private List<PowerUp> powerUps;
    private int maxCapacity;

    public void collect(PowerUp powerUp) { ... }
    public void use(PowerUp powerUp, GameContext context) { ... }
}
```

---

## Variation 2: Multiplayer Online
**Learning Value:** Introduces real-time multiplayer synchronization, lobby management, and turn-based networking.

### Additional Requirements
- Network play over WebSocket/TCP
- Turn timeout (auto-skip after N seconds)
- Reconnection handling for dropped players
- Game state synchronization across clients

### Design Changes
- Add `GameServer` managing game rooms
- Add `NetworkPlayer` communicating over sockets
- Add `TurnTimer` with timeout and auto-skip
- Add `GameState` serializable for sync/reconnection

### Solution Approach
`GameServer` hosts game rooms. Each player connects via `NetworkPlayer` (WebSocket). Server maintains authoritative `GameState`. On each turn, server sends turn notification, starts `TurnTimer`. If player responds with roll within timeout, process move and broadcast new state. On timeout, auto-skip. On disconnect, mark player as DISCONNECTED, allow reconnection within grace period by sending full game state snapshot. Use Command pattern for all moves to enable replay and undo.

### Key Classes to Add
```java
public class GameServer {
    private Map<String, GameRoom> rooms;
    
    public String createRoom(GameConfig config) { ... }
    public void handlePlayerAction(String roomId, String playerId, Action action) { ... }
    public void broadcastState(String roomId) { ... }
}

public class TurnTimer {
    private Duration timeout;
    private ScheduledExecutorService scheduler;
    private Runnable onTimeout;

    public void startTurn(Player player) {
        scheduler.schedule(() -> onTimeout.run(), timeout.getSeconds(), TimeUnit.SECONDS);
    }
    public void cancelOnAction() { ... }
}

public class GameState implements Serializable {
    private Map<String, Integer> playerPositions;
    private String currentTurnPlayerId;
    private List<MoveRecord> moveHistory;

    public byte[] serialize() { ... }
    public static GameState deserialize(byte[] data) { ... }
}
```

---

## Variation 3: Custom Board Builder
**Learning Value:** Practices builder pattern for rule configuration, validation constraints, and user-generated content.

### Additional Requirements
- Players can design custom boards with arbitrary snakes/ladders
- Validate that board is solvable (reachable finish)
- Validate no infinite loops (snake -> ladder -> snake cycle)
- Save/load custom boards, share with others

### Design Changes
- Add `BoardBuilder` with fluent API for construction
- Add `BoardValidator` checking solvability and loops
- Add `BoardSerializer` for save/load functionality
- Add `BoardGallery` for sharing user-created boards

### Solution Approach
`BoardBuilder` provides a fluent API to place snakes and ladders. Before saving, `BoardValidator` runs checks: (1) BFS/DFS from start to verify finish is reachable, (2) detect cycles by building a graph of all transitions and checking for strongly connected components that trap players, (3) verify no snake head or ladder base at position 1 or position N. Serialize boards to JSON for persistence and sharing. Validate that a snake's head > tail and ladder's base < top.

### Key Classes to Add
```java
public class BoardBuilder {
    private int size;
    private List<Snake> snakes = new ArrayList<>();
    private List<Ladder> ladders = new ArrayList<>();

    public BoardBuilder size(int size) { this.size = size; return this; }
    public BoardBuilder addSnake(int head, int tail) { snakes.add(new Snake(head, tail)); return this; }
    public BoardBuilder addLadder(int base, int top) { ladders.add(new Ladder(base, top)); return this; }
    public Board build() { validate(); return new Board(size, snakes, ladders); }
}

public class BoardValidator {
    public ValidationResult validate(Board board) {
        boolean reachable = isFinishReachable(board);
        boolean noLoops = !hasInfiniteLoop(board);
        boolean validPositions = checkPositionConstraints(board);
        return new ValidationResult(reachable && noLoops && validPositions, getErrors());
    }

    private boolean hasInfiniteLoop(Board board) {
        // Build transition graph, detect cycles using DFS
        ...
    }
}
```

---

## Variation 4: Tournament Mode
**Learning Value:** Explores trade-offs between fairness and excitement in elimination-based competitive structures.

### Additional Requirements
- Best of 3 (or N) series
- Leaderboard with ELO rating
- Match history and statistics
- Bracket/Swiss tournament format

### Design Changes
- Add `Tournament` managing multiple matches
- Add `ELORating` for player skill tracking
- Add `Leaderboard` with ranking and stats
- Add `MatchSeries` for best-of-N format

### Solution Approach
`Tournament` orchestrates multiple `MatchSeries` between players. Each `MatchSeries` is best-of-N games. ELO rating updates after each series: winner gains points proportional to upset probability (beating higher-rated player = more points). `Leaderboard` sorts players by ELO. Support bracket (single/double elimination) and Swiss formats (pair players with similar records each round). Track statistics: win rate, average turns to win, longest snake survived, etc.

### Key Classes to Add
```java
public class Tournament {
    private TournamentFormat format; // BRACKET, SWISS, ROUND_ROBIN
    private List<Player> participants;
    private List<MatchSeries> matches;
    private Leaderboard leaderboard;

    public void generateNextRound() { ... }
    public Player getWinner() { ... }
}

public class ELORating {
    private static final int K_FACTOR = 32;

    public static int[] calculateNewRatings(int winnerRating, int loserRating) {
        double expectedWin = 1.0 / (1 + Math.pow(10, (loserRating - winnerRating) / 400.0));
        int winnerNew = winnerRating + (int)(K_FACTOR * (1 - expectedWin));
        int loserNew = loserRating + (int)(K_FACTOR * (0 - (1 - expectedWin)));
        return new int[]{winnerNew, loserNew};
    }
}

public class MatchSeries {
    private Player player1;
    private Player player2;
    private int bestOf;
    private List<Game> games;

    public Player getSeriesWinner() {
        int winsNeeded = bestOf / 2 + 1;
        // count wins for each player
        ...
    }
}
```

---

## Variation 5: Undo Last Move
**Learning Value:** Deepens understanding of command pattern, state snapshots, and reversible operations.

### Additional Requirements
- Players can undo their last move (limited uses per game)
- Command pattern for all game actions
- Undo stack with configurable depth
- Cannot undo opponent's moves

### Design Changes
- Add `Command` interface for all game moves
- Add `MoveCommand` encapsulating roll + position change
- Add `UndoManager` with limited undo stack
- Add undo count tracking per player

### Solution Approach
Implement Command pattern: every game action (roll dice, move, encounter snake/ladder) is wrapped in a `MoveCommand` that stores previous state. Each player gets N undo tokens per game. When undo is requested, pop the last command from that player's stack and call `undo()` which restores position. Cannot undo if it's not your most recent move (opponent moved after you). The `UndoManager` maintains per-player command history and validates undo eligibility.

### Key Classes to Add
```java
public interface GameCommand {
    void execute();
    void undo();
    String getPlayerId();
}

public class MoveCommand implements GameCommand {
    private Player player;
    private int previousPosition;
    private int newPosition;
    private int diceRoll;

    @Override
    public void execute() {
        previousPosition = player.getPosition();
        player.setPosition(newPosition);
    }

    @Override
    public void undo() {
        player.setPosition(previousPosition);
    }
}

public class UndoManager {
    private Map<String, Deque<GameCommand>> playerHistory;
    private Map<String, Integer> undoTokens;
    private int maxUndosPerGame;

    public boolean canUndo(String playerId) {
        return undoTokens.getOrDefault(playerId, 0) > 0 
               && !playerHistory.get(playerId).isEmpty();
    }

    public void undo(String playerId) {
        GameCommand lastMove = playerHistory.get(playerId).pop();
        lastMove.undo();
        undoTokens.merge(playerId, -1, Integer::sum);
    }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

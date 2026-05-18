# Card Game (Blackjack) - Variations

## Variation 1: Multi-Player Table
**Learning Value:** Teaches multi-player turn management, independent decision tracking, and side-bet extensibility.

### Additional Requirements
- Support 1-7 players at one table
- Individual player decisions (hit, stand, double, split)
- Insurance and side bets
- Proper turn order and dealer rules

### Design Changes
- Add `Table` managing multiple player seats
- Add `SideBet` interface for various side bets
- Add `Insurance` when dealer shows Ace
- Modify `BlackjackGame` to iterate through player turns

### Solution Approach
`Table` manages up to 7 `Seat` positions plus the dealer. The game loop deals to all players, then processes each player's turn in order (left to right). Each player makes independent decisions. When dealer shows Ace, offer `Insurance` (side bet paying 2:1 if dealer has blackjack). Add side bets (Perfect Pairs, 21+3) checked after initial deal. The dealer plays last following fixed rules. Payouts are calculated per player independently.

### Key Classes to Add
```java
public class Table {
    private String id;
    private List<Seat> seats; // max 7
    private Dealer dealer;
    private Deck shoe; // multi-deck shoe
    private double minimumBet;
    private double maximumBet;

    public void playRound() {
        // Deal to all, offer insurance, process each player, dealer plays, settle bets
    }
}

public class Seat {
    private int position;
    private Player player;
    private List<Hand> hands; // multiple if split
    private List<SideBet> sideBets;
}

public interface SideBet {
    double evaluate(Hand playerHand, Card dealerUpCard);
    String getName();
    double getPayoutRatio();
}
```

---

## Variation 2: Card Counting Simulation
**Learning Value:** Introduces statistical simulation, counting systems, and Monte Carlo analysis for edge calculation.

### Additional Requirements
- Implement Hi-Lo counting system
- Track running count and true count
- Bet spreading strategy based on count
- Simulate millions of hands for edge calculation

### Design Changes
- Add `CardCountingStrategy` with Hi-Lo system
- Add `CountTracker` maintaining running/true count
- Add `BetSpreadStrategy` adjusting bet size
- Add `Simulator` for Monte Carlo analysis

### Solution Approach
In Hi-Lo, cards 2-6 are +1, 7-9 are 0, 10-A are -1. `CountTracker` maintains the running count as cards are revealed and computes true count (running count / decks remaining). `BetSpreadStrategy` maps true count ranges to bet multipliers (e.g., TC+2 = 2x min bet, TC+5 = 5x). The `Simulator` runs millions of hands to compute expected edge at different spreads and playing deviations.

### Key Classes to Add
```java
public class CardCountingStrategy {
    private CountTracker tracker;
    private BetSpreadStrategy betStrategy;

    public int getCardValue(Card card) {
        // Hi-Lo: 2-6 = +1, 7-9 = 0, 10-A = -1
    }

    public double getOptimalBet(double minimumBet) {
        return betStrategy.calculateBet(tracker.getTrueCount(), minimumBet);
    }
}

public class CountTracker {
    private int runningCount;
    private int cardsDealt;
    private int totalDecks;

    public void cardRevealed(Card card) { /* Update running count */ }
    public double getTrueCount() { return runningCount / getDecksRemaining(); }
    public void onShuffle() { runningCount = 0; cardsDealt = 0; }
}

public class Simulator {
    public SimulationResult simulate(int hands, CardCountingStrategy strategy) {
        // Run N hands, track bankroll, compute edge percentage
    }
}
```

---

## Variation 3: Tournament Mode
**Learning Value:** Practices elimination tournament design, progressive difficulty, and leaderboard management.

### Additional Requirements
- Elimination rounds with increasing blinds
- Chip leader tracking and leaderboard
- Blind/minimum bet increases on schedule
- Final table dynamics

### Design Changes
- Add `Tournament` managing rounds and eliminations
- Add `BlindSchedule` increasing minimums over time
- Add `Leaderboard` tracking chip counts
- Add `EliminationRule` for when players are out

### Solution Approach
A `Tournament` has N players each starting with equal chips. Play proceeds in rounds; after each round (or time interval), the `BlindSchedule` increases minimum bets, forcing action. Players are eliminated when their chip count hits zero. The `Leaderboard` ranks players by chip count. As players are eliminated, tables are consolidated. The tournament ends when one player has all chips. Final table rules may differ (e.g., always show cards).

### Key Classes to Add
```java
public class Tournament {
    private String id;
    private List<Player> players;
    private BlindSchedule blindSchedule;
    private Leaderboard leaderboard;
    private TournamentStatus status;

    public void playRound() { /* Deal at all tables, eliminate bankrupt players */ }
    public void consolidateTables() { /* Merge tables as players are eliminated */ }
    public Player getWinner() { /* Last player standing */ }
}

public class BlindSchedule {
    private List<BlindLevel> levels;
    private int currentLevel;
    private Duration levelDuration;

    public double getCurrentMinimumBet() { return levels.get(currentLevel).minimum; }
    public void advanceLevel() { currentLevel++; }
}

public class Leaderboard {
    private TreeMap<Integer, Player> rankings; // chips -> player, sorted descending
    public List<Player> getTopN(int n) { /* Top chip counts */ }
    public int getRank(String playerId) { /* Player's position */ }
}
```

---

## Variation 4: Variant - Spanish 21
**Learning Value:** Explores trade-offs between rule complexity and game balance in variant rule engines.

### Additional Requirements
- Remove all 10-value cards (keep face cards)
- Bonus payouts for specific hands (e.g., 5-card 21)
- Player-favorable rules (late surrender, double after split)
- Player 21 always wins (even vs dealer 21)

### Design Changes
- Add `Spanish21Rules` extending base game rules
- Add `BonusPayout` for special hand combinations
- Modify `Deck` to exclude 10s
- Add `SpecialRule` interface for variant rules

### Solution Approach
Create a `Spanish21Rules` class overriding the base `BlackjackGame` rules. The deck removes all 10-rank cards (but keeps J, Q, K). Add bonus payouts: 5-card 21 pays 3:2, 6-card 21 pays 2:1, 7+ card 21 pays 3:1, 6-7-8 suited pays 2:1, three 7s pays 3:1. The key rule change: player's 21 always beats dealer's 21 (even if dealer has blackjack). Allow late surrender, double on any number of cards, and re-split aces.

### Key Classes to Add
```java
public class Spanish21Rules implements GameRules {
    private List<BonusPayout> bonusPayouts;

    @Override
    public Deck createDeck() { /* 48-card deck, no 10s */ }

    @Override
    public double calculatePayout(Hand playerHand, Hand dealerHand, Bet bet) {
        // Check bonus payouts first, then standard rules (player 21 always wins)
    }

    @Override
    public boolean playerWinsOnTie(Hand player, Hand dealer) {
        return player.getValue() == 21; // Player 21 always wins
    }
}

public class BonusPayout {
    private String name;
    private Predicate<Hand> condition;
    private double payoutMultiplier;

    public boolean applies(Hand hand) { return condition.test(hand); }
}
```

---

## Variation 5: Online Multiplayer with Chat
**Learning Value:** Deepens understanding of lobby systems, real-time communication, and commission-based revenue models.

### Additional Requirements
- Lobby system for finding/creating tables
- Table limits (min/max bet) and buy-in
- Real-time chat between players
- Rake (house commission) and VIP tiers

### Design Changes
- Add `Lobby` for matchmaking and table browsing
- Add `ChatService` for table chat
- Add `RakeCalculator` for house commission
- Add `VIPSystem` with loyalty tiers

### Solution Approach
The `Lobby` lists available tables with their limits, player count, and average pot. Players can filter by stakes and join or create tables. Each table has a `ChatService` for real-time text messages between seated players. The house takes a `Rake` (percentage of each pot, capped). `VIPSystem` tracks total rake contributed; players earn points toward tiers (Bronze, Silver, Gold, Platinum) which unlock benefits (lower rake, exclusive tables, cashback).

### Key Classes to Add
```java
public class Lobby {
    private List<Table> tables;

    public List<Table> getAvailableTables(double minStake, double maxStake) { /* Filter */ }
    public Table createTable(double minBet, double maxBet, int maxPlayers) { /* New table */ }
    public void joinTable(String tableId, Player player, double buyIn) { /* Seat player */ }
}

public class RakeCalculator {
    private double rakePercent; // e.g., 5%
    private double rakeCap;    // max rake per pot

    public double calculateRake(double potSize) {
        return Math.min(potSize * rakePercent, rakeCap);
    }
}

public class VIPSystem {
    private Map<String, VIPTier> playerTiers;
    private Map<String, Double> rakeContributed;

    public void addRakeContribution(String playerId, double amount) { /* Accumulate */ }
    public VIPTier getTier(String playerId) { /* Based on contribution */ }
    public double getRakeDiscount(String playerId) { /* Tier-based discount */ }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

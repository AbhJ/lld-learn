/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/DealerStrategy.java — Strategy interface for dealer's hit/stand decisions
// DESIGN PATTERN: Strategy
public interface DealerStrategy { // interface = contract; dealer strategies must implement shouldHit()
    boolean shouldHit(Hand dealerHand);
    String getDescription();

    // Default overload: most strategies (Standard, Aggressive) only need the dealer's own hand.
    // Chart-based strategies override this to consult the player's upcard as well.
    default boolean shouldHit(Hand dealerHand, Card playerUpcard) {
        return shouldHit(dealerHand);
    }
}

// Standard casino rule: hit on anything below 17, stand otherwise.
// Plain `score < 17` is already O(1) — no lookup table needed.
class StandardDealerStrategy implements DealerStrategy {
    @Override
    public boolean shouldHit(Hand dealerHand) {
        return dealerHand.getScore() < 17;
    }

    @Override public String getDescription() { return "Hit on 16, Stand on 17 (hard)"; }
}

class AggressiveDealerStrategy implements DealerStrategy { // implements = hits on soft 17
    @Override
    public boolean shouldHit(Hand dealerHand) {
        int score = dealerHand.getScore();
        if (score < 17) return true;
        if (score == 17) {
            for (Card card : dealerHand.getCards()) { if (card.isAce()) return true; }
        }
        return false;
    }

    @Override public String getDescription() { return "Hit on soft 17"; }
}

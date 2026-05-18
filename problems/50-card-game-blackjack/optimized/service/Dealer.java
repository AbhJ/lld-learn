/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/Dealer.java — Controls the dealer's actions
public class Dealer {
    private Hand hand;              // private = dealer's cards managed internally
    private DealerStrategy strategy; // private = strategy decides when to hit/stand

    public Dealer(DealerStrategy strategy) { this.hand = new Hand(); this.strategy = strategy; }
    public Hand getHand() { return hand; }
    public boolean shouldHit() { return strategy.shouldHit(hand); }
    // Chart-aware overload: passes the player's upcard so chart-based strategies can consult it.
    public boolean shouldHit(Card playerUpcard) { return strategy.shouldHit(hand, playerUpcard); }
    public String getStrategyDescription() { return strategy.getDescription(); }
    public void resetHand() { hand.clear(); }
}

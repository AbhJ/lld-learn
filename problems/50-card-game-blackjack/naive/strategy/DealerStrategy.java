/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/DealerStrategy.java — Defines the dealer's hit/stand decision rules
// DESIGN PATTERN: Strategy
public interface DealerStrategy { // interface = contract; dealer strategies must implement shouldHit()
    boolean shouldHit(Hand dealerHand);
    String getDescription();
}

class StandardDealerStrategy implements DealerStrategy { // implements = standard casino rules
    @Override public boolean shouldHit(Hand dealerHand) { return dealerHand.getScore() < 17; }
    @Override public String getDescription() { return "Hit on 16, Stand on 17"; }
}

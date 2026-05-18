/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/GreedyChangeStrategy.java — Greedy change-making with integer division + modulo
//
// Optimization vs naive: per-denomination O(1) using `count = amount / value`
// and `amount %= value`. Naive subtracts one coin's value at a time (O(amount/value)).
// For US canonical coins this greedy choice is also coin-count optimal.

import java.util.EnumMap;
import java.util.Map;

class GreedyChangeStrategy implements ChangeStrategy {

    private static final Coin[] DENOMINATIONS_DESC =
            {Coin.QUARTER, Coin.DIME, Coin.NICKEL, Coin.PENNY};

    @Override
    public Map<Coin, Integer> calculateChange(int amountInCents) {
        Map<Coin, Integer> change = new EnumMap<>(Coin.class);
        int remaining = amountInCents;
        for (Coin coin : DENOMINATIONS_DESC) {
            int count = remaining / coin.getValue();
            if (count > 0) {
                change.put(coin, count);
                remaining %= coin.getValue();
            }
        }
        return change;
    }
}

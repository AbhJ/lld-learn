/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/GreedyChangeStrategy.java — Greedy change-making (works for canonical coin systems)
//
// For US coin denominations (1, 5, 10, 25 cents) the greedy algorithm always
// returns the minimum-coin solution. For non-canonical systems (e.g. 1, 3, 4)
// it can be suboptimal — see DP-based strategies for those cases.

import java.util.LinkedHashMap;
import java.util.Map;

class GreedyChangeStrategy implements ChangeStrategy {

    private static final Coin[] DENOMINATIONS_DESC =
            {Coin.QUARTER, Coin.DIME, Coin.NICKEL, Coin.PENNY};

    @Override
    public Map<Coin, Integer> calculateChange(int amountInCents) {
        // Naive variant: still greedy, but uses a simple per-coin subtractive
        // loop. This is intentionally NOT modulo-based; the optimized
        // variant uses integer division + modulo for O(1) per denomination.
        Map<Coin, Integer> change = new LinkedHashMap<>();
        int remaining = amountInCents;
        for (Coin coin : DENOMINATIONS_DESC) {
            int count = 0;
            while (remaining >= coin.getValue()) {
                remaining -= coin.getValue();
                count++;
            }
            if (count > 0) change.put(coin, count);
        }
        return change;
    }
}

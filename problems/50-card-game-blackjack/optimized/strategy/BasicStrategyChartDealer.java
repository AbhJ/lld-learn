/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/BasicStrategyChartDealer.java — Hard-totals basic-strategy chart lookup
// DESIGN PATTERN: Strategy + table-driven decision (real basic strategy from blackjack literature)
//
// Algorithmic improvement: instead of a single threshold ("hit < 17"), decisions are looked up
// in a 2D chart indexed by (current-total, opponent-upcard). The chart encodes the
// expected-value-optimal action for each cell, derived from millions of simulated hands.
//
// Reference: https://en.wikipedia.org/wiki/Blackjack#Basic_strategy (hard totals 12-21).
//
// Chart legend:
//   H = HIT, S = STAND, D = DOUBLE-DOWN (treated as HIT here since dealer can't double).
// Rows: current hard total (12..21). Cols: opponent upcard value (2..10 then A=11).
//
// Hard totals (player) vs dealer upcard:
//          2    3    4    5    6    7    8    9   10    A
//   8 :    H    H    H    H    H    H    H    H    H    H
//   9 :    H    D    D    D    D    H    H    H    H    H
//  10 :    D    D    D    D    D    D    D    D    H    H
//  11 :    D    D    D    D    D    D    D    D    D    H
//  12 :    H    H    S    S    S    H    H    H    H    H
//  13 :    S    S    S    S    S    H    H    H    H    H
//  14 :    S    S    S    S    S    H    H    H    H    H
//  15 :    S    S    S    S    S    H    H    H    H    H
//  16 :    S    S    S    S    S    H    H    H    H    H
//  17+:    S    S    S    S    S    S    S    S    S    S

public class BasicStrategyChartDealer implements DealerStrategy {

    public enum Action { HIT, STAND, DOUBLE }

    // Chart for hard totals 8..21 against upcards 2..A. Indexed [total - 8][upcardValue - 2].
    // Pre-computed at class-load — O(1) lookup per decision.
    private static final Action[][] HARD_CHART = buildHardChart();

    private static Action[][] buildHardChart() {
        // 14 rows (totals 8..21), 10 columns (upcards 2..A=11).
        Action H = Action.HIT, S = Action.STAND, D = Action.DOUBLE;
        return new Action[][] {
            /* 8 */ { H, H, H, H, H, H, H, H, H, H },
            /* 9 */ { H, D, D, D, D, H, H, H, H, H },
            /*10 */ { D, D, D, D, D, D, D, D, H, H },
            /*11 */ { D, D, D, D, D, D, D, D, D, H },
            /*12 */ { H, H, S, S, S, H, H, H, H, H },
            /*13 */ { S, S, S, S, S, H, H, H, H, H },
            /*14 */ { S, S, S, S, S, H, H, H, H, H },
            /*15 */ { S, S, S, S, S, H, H, H, H, H },
            /*16 */ { S, S, S, S, S, H, H, H, H, H },
            /*17 */ { S, S, S, S, S, S, S, S, S, S },
            /*18 */ { S, S, S, S, S, S, S, S, S, S },
            /*19 */ { S, S, S, S, S, S, S, S, S, S },
            /*20 */ { S, S, S, S, S, S, S, S, S, S },
            /*21 */ { S, S, S, S, S, S, S, S, S, S },
        };
    }

    // Public chart lookup — exposed for testing/demonstration.
    // total: current hand total (8..21). upcardValue: opponent's upcard value (2..11 where A=11).
    // Returns the chart-recommended action, or HIT for totals below 8, STAND for 21+/bust prevention.
    public static Action lookup(int total, int upcardValue) {
        if (total < 8) return Action.HIT;       // very low totals: always hit
        if (total > 21) return Action.STAND;    // already busted: don't hit further
        int row = total - 8;
        int col = clamp(upcardValue, 2, 11) - 2;
        return HARD_CHART[row][col];
    }

    private static int clamp(int v, int lo, int hi) { return v < lo ? lo : (v > hi ? hi : v); }

    // The dealer-side adapter: when the player's upcard is unknown (e.g. fallback path), use the
    // most defensive assumption — a strong dealer-upcard equivalent (10) — so the dealer plays tight.
    @Override
    public boolean shouldHit(Hand dealerHand) {
        return shouldHit(dealerHand, /* playerUpcard */ null);
    }

    @Override
    public boolean shouldHit(Hand dealerHand, Card playerUpcard) {
        int total = dealerHand.getScore();
        // Treat ACE as 11 for chart-lookup purposes (chart's last column).
        int upVal = (playerUpcard == null) ? 10 : (playerUpcard.isAce() ? 11 : playerUpcard.getValue());
        Action a = lookup(total, upVal);
        // Dealer cannot actually DOUBLE — collapse DOUBLE -> HIT for this seat.
        return a == Action.HIT || a == Action.DOUBLE;
    }

    @Override public String getDescription() {
        return "Basic-strategy chart (hard totals 8-21 vs upcards 2-A)";
    }
}

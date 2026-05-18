/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/ChangeCalculator.java — Formatting helpers for a change Map.
// (Algorithm itself lives in ChangeStrategy implementations.)

import java.util.Map;

class ChangeCalculator {

    public static String formatChange(Map<Coin, Integer> change) {
        if (change.isEmpty()) return "none";
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<Coin, Integer> e : change.entrySet()) {
            if (!first) sb.append(", ");
            sb.append(e.getValue()).append("× ").append(e.getKey().name());
            first = false;
        }
        return sb.toString();
    }

    public static int totalValue(Map<Coin, Integer> change) {
        int total = 0;
        for (Map.Entry<Coin, Integer> e : change.entrySet()) {
            total += e.getKey().getValue() * e.getValue();
        }
        return total;
    }
}

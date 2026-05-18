/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/SplitStrategy.java — Interchangeable expense split algorithms
// DESIGN PATTERN: Strategy
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface SplitStrategy {                          // interface = swappable split algorithms (Strategy pattern)
    Map<User, Double> split(double totalAmount, List<User> participants, Map<User, Double> params);

    class EqualSplit implements SplitStrategy {
        @Override
        public Map<User, Double> split(double totalAmount, List<User> participants, Map<User, Double> params) {
            Map<User, Double> shares = new HashMap<>();
            double each = Math.round(totalAmount * 100.0 / participants.size()) / 100.0;
            for (User user : participants) {
                shares.put(user, each);
            }
            return shares;
        }
    }

    class PercentageSplit implements SplitStrategy {
        @Override
        public Map<User, Double> split(double totalAmount, List<User> participants, Map<User, Double> params) {
            double totalPercent = 0;
            for (Double pct : params.values()) totalPercent += pct;
            if (Math.abs(totalPercent - 100.0) > 0.01) {
                throw new IllegalArgumentException("Percentages must sum to 100, got " + totalPercent);
            }
            Map<User, Double> shares = new HashMap<>();
            for (User user : participants) {
                double pct = params.getOrDefault(user, 0.0);
                shares.put(user, Math.round(totalAmount * pct) / 100.0);
            }
            return shares;
        }
    }

    class ExactSplit implements SplitStrategy {
        @Override
        public Map<User, Double> split(double totalAmount, List<User> participants, Map<User, Double> params) {
            double totalShares = 0;
            for (Double amount : params.values()) totalShares += amount;
            if (Math.abs(totalShares - totalAmount) > 0.01) {
                throw new IllegalArgumentException(
                    "Exact amounts must sum to total. Expected " + totalAmount + ", got " + totalShares);
            }
            return new HashMap<>(params);
        }
    }
}

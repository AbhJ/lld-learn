/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/WinnerStrategy.java — Pluggable winner determination algorithms
// DESIGN PATTERN: Strategy
import java.util.List;

public interface WinnerStrategy {                         // interface = contract for pluggable winner algorithms
    WinnerResult determineWinner(List<Bid> bids, double reservePrice);

    class WinnerResult {
        private Bidder winner;
        private double winningPrice;
        private String description;

        public WinnerResult(Bidder winner, double winningPrice, String description) {
            this.winner = winner; this.winningPrice = winningPrice; this.description = description;
        }
        public Bidder getWinner() { return winner; }
        public double getWinningPrice() { return winningPrice; }
        public String getDescription() { return description; }
    }

    class HighestBidStrategy implements WinnerStrategy {   // implements = provides highest-bid-wins logic
        @Override
        public WinnerResult determineWinner(List<Bid> bids, double reservePrice) {
            if (bids.isEmpty()) return null;
            // Naive: linear scan through all bids to find highest
            Bid highest = bids.get(0);
            for (Bid bid : bids) {
                if (bid.getAmount() > highest.getAmount()) highest = bid;
            }
            if (highest.getAmount() < reservePrice) return null;
            return new WinnerResult(highest.getBidder(), highest.getAmount(),
                "Highest bid: $" + String.format("%.2f", highest.getAmount()));
        }
    }

    class SecondPriceStrategy implements WinnerStrategy {  // implements = Vickrey auction (pay 2nd price)
        @Override
        public WinnerResult determineWinner(List<Bid> bids, double reservePrice) {
            if (bids.isEmpty()) return null;
            if (bids.size() == 1) {
                Bid only = bids.get(0);
                if (only.getAmount() >= reservePrice)
                    return new WinnerResult(only.getBidder(), reservePrice, "Only bidder, pays reserve");
                return null;
            }
            Bid highest = null, secondHighest = null;
            for (Bid bid : bids) {
                if (highest == null || bid.getAmount() > highest.getAmount()) {
                    secondHighest = highest; highest = bid;
                } else if (secondHighest == null || bid.getAmount() > secondHighest.getAmount()) {
                    secondHighest = bid;
                }
            }
            if (highest.getAmount() < reservePrice) return null;
            double price = Math.max(secondHighest.getAmount(), reservePrice);
            return new WinnerResult(highest.getBidder(), price,
                "Winner pays second-highest: $" + String.format("%.2f", price));
        }
    }
}

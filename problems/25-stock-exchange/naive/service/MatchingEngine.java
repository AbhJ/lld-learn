/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/MatchingEngine.java — Matches orders using O(n) scans
import java.util.ArrayList;
import java.util.List;

public class MatchingEngine {
    private int tradeCounter = 0;                         // private = generates unique trade IDs

    public List<Trade> match(Order incoming, OrderBook book) {
        List<Trade> trades = new ArrayList<>();
        if (incoming.getSide() == OrderSide.BUY) {
            while (incoming.getRemainingQuantity() > 0) {
                Order bestSell = book.getBestSell(); // O(n) scan each time
                if (bestSell == null || (!incoming.isMarketOrder() && bestSell.getPrice() > incoming.getPrice())) break;
                executeTrade(incoming, bestSell, bestSell.getPrice(), trades);
            }
        } else {
            while (incoming.getRemainingQuantity() > 0) {
                Order bestBuy = book.getBestBuy(); // O(n) scan each time
                if (bestBuy == null || (!incoming.isMarketOrder() && bestBuy.getPrice() < incoming.getPrice())) break;
                executeTrade(bestBuy, incoming, bestBuy.getPrice(), trades);
            }
        }
        book.removeFilledOrders();
        return trades;
    }

    private void executeTrade(Order buy, Order sell, double price, List<Trade> trades) {
        int qty = Math.min(buy.getRemainingQuantity(), sell.getRemainingQuantity());
        Trade trade = new Trade("T" + (++tradeCounter), buy.getStock(), buy.getTrader(), sell.getTrader(), price, qty);
        trades.add(trade);
        buy.fill(qty); sell.fill(qty);
        buy.getTrader().notify("Bought " + qty + " @ $" + String.format("%.2f", price));
        sell.getTrader().notify("Sold " + qty + " @ $" + String.format("%.2f", price));
    }
}

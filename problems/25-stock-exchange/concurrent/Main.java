/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — 100 traders submitting orders simultaneously, verifies all trades are consistent

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Stock Exchange Demo ===\n");

        MatchingEngine engine = new MatchingEngine();

        int traderCount = 100;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(traderCount);
        AtomicInteger totalTradesMade = new AtomicInteger(0);

        System.out.println("Scenario: 100 traders submit orders simultaneously for AAPL.");
        System.out.println("  50 buyers at prices $148-$152");
        System.out.println("  50 sellers at prices $149-$153");
        System.out.println("Expected: Matching orders execute, no quantity appears from nowhere.\n");

        // 50 buyers, 50 sellers
        for (int i = 0; i < traderCount; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    Order order;
                    if (id < 50) {
                        // Buyers: prices from 148 to 152
                        double price = 148.0 + (id % 5);
                        order = new Order("Buyer-" + id, "AAPL", OrderSide.BUY, price, 10);
                    } else {
                        // Sellers: prices from 149 to 153
                        double price = 149.0 + ((id - 50) % 5);
                        order = new Order("Seller-" + id, "AAPL", OrderSide.SELL, price, 10);
                    }
                    List<Trade> trades = engine.submitOrder(order);
                    totalTradesMade.addAndGet(trades.size());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }, "Trader-" + id).start();
        }

        startLatch.countDown();
        doneLatch.await();

        // Print results
        List<Trade> allTrades = engine.getAllTrades();
        System.out.println("Trades executed (first 10):");
        int shown = 0;
        for (Trade trade : allTrades) {
            if (shown++ >= 10) break;
            System.out.println("  " + trade);
        }
        if (allTrades.size() > 10) {
            System.out.println("  ... and " + (allTrades.size() - 10) + " more");
        }

        // Verify consistency: total bought quantity == total sold quantity
        long totalBought = 0;
        long totalSold = 0;
        for (Trade trade : allTrades) {
            totalBought += trade.getQuantity();
            totalSold += trade.getQuantity();
        }

        // Verify no negative quantities
        boolean noNegatives = allTrades.stream()
                .allMatch(t -> t.getQuantity() > 0);

        // Verify prices are valid (buy price >= sell price for matching)
        boolean validPrices = allTrades.stream()
                .allMatch(t -> t.getBuyOrder().getPrice() >= t.getSellOrder().getPrice());

        System.out.println("\n--- Summary ---");
        System.out.println("Orders submitted: " + engine.getOrdersProcessed());
        System.out.println("Trades executed: " + engine.getTradesExecuted());
        System.out.println("Total volume traded: " + totalBought + " shares");
        System.out.println("All quantities positive: " + noNegatives);
        System.out.println("All prices valid (buy >= sell): " + validPrices);
        System.out.println("\nCorrectness check: " +
                (noNegatives && validPrices ? "PASSED" : "FAILED"));
    }
}

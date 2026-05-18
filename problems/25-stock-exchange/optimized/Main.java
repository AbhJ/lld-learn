/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Demonstrates stock exchange with O(log n) TreeMap matching
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Stock Exchange Demo (Optimized) ===");
        System.out.println("Optimization: TreeMap<Price, Queue<Order>> for O(log n) match\n");

        StockExchange exchange = new StockExchange();
        Stock aapl = new Stock("AAPL", "Apple Inc.");
        exchange.registerStock(aapl);

        Trader alice = exchange.registerTrader("t1", "Alice");
        Trader bob = exchange.registerTrader("t2", "Bob");
        Trader charlie = exchange.registerTrader("t3", "Charlie");

        System.out.println("--- Placing Limit Orders ---");
        exchange.placeLimitOrder(alice, aapl, OrderSide.BUY, 100, 150.0);
        exchange.placeLimitOrder(bob, aapl, OrderSide.BUY, 50, 149.0);
        exchange.placeLimitOrder(charlie, aapl, OrderSide.SELL, 80, 152.0);
        System.out.println("Alice BUY 100@150, Bob BUY 50@149, Charlie SELL 80@152");

        System.out.println("\n--- Incoming sell crosses spread (O(log n) via TreeMap) ---");
        Order daveOrder = exchange.placeLimitOrder(exchange.registerTrader("t4", "Dave"), aapl, OrderSide.SELL, 120, 148.0);
        System.out.println("Dave SELL 120@148 → filled " + daveOrder.getFilledQuantity() + "/" + daveOrder.getQuantity());

        System.out.println("\n--- Trade History ---");
        for (Trade t : exchange.getTradeHistory()) System.out.println("  " + t);

        System.out.println("\n=== Demo Complete ===");
    }
}

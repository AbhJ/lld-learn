/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/StockExchange.java — Exchange facade
import java.util.*;

public class StockExchange {
    private Map<String, OrderBook> orderBooks;            // HashMap = O(1) order book per symbol
    private Map<String, Trader> traders;                  // HashMap = O(1) trader lookup by ID
    private List<Trade> tradeHistory;                     // ArrayList = sequential trade log
    private MatchingEngine matchingEngine;
    private int orderCounter = 0;

    public StockExchange() {
        this.orderBooks = new HashMap<>(); this.traders = new HashMap<>();
        this.tradeHistory = new ArrayList<>(); this.matchingEngine = new MatchingEngine();
    }

    public void registerStock(Stock stock) { orderBooks.put(stock.getSymbol(), new OrderBook(stock)); }
    public Trader registerTrader(String id, String name) { Trader t = new Trader(id, name); traders.put(id, t); return t; }

    public Order placeLimitOrder(Trader trader, Stock stock, OrderSide side, int qty, double price) {
        Order order = new LimitOrder("O" + (++orderCounter), trader, stock, side, qty, price);
        return processOrder(order);
    }

    public Order placeMarketOrder(Trader trader, Stock stock, OrderSide side, int qty) {
        Order order = new MarketOrder("O" + (++orderCounter), trader, stock, side, qty);
        return processOrder(order);
    }

    private Order processOrder(Order order) {
        OrderBook book = orderBooks.get(order.getStock().getSymbol());
        List<Trade> trades = matchingEngine.match(order, book);
        tradeHistory.addAll(trades);
        if (order.getRemainingQuantity() > 0 && !order.isMarketOrder()) book.addOrder(order);
        return order;
    }

    public List<Trade> getTradeHistory() { return tradeHistory; }
}

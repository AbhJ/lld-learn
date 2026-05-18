/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/StockExchange.java — Exchange facade
import java.util.*;

public class StockExchange {                              // Facade pattern = single entry point for exchange ops
    private Map<String, OrderBook> orderBooks;            // private = one order book per stock symbol
    private Map<String, Trader> traders;                  // private = registered traders
    private List<Trade> tradeHistory;                     // private = all executed trades
    private MatchingEngine matchingEngine;                // private = delegates order matching logic
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

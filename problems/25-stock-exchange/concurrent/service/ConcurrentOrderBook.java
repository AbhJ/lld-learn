/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/ConcurrentOrderBook.java — ConcurrentSkipListMap for price levels, ReentrantLock per price level

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe order book using ConcurrentSkipListMap for ordered price levels.
 * Buy side: descending (highest price first — best bid)
 * Sell side: ascending (lowest price first — best ask)
 */
class ConcurrentOrderBook {
    private final String symbol;                          // final = immutable; safe to share across threads
    // Buy orders: highest price first (reverse order)
    private final ConcurrentSkipListMap<Double, ConcurrentLinkedQueue<Order>> buyLevels = // ConcurrentSkipListMap = thread-safe sorted map; O(log n) insert
            new ConcurrentSkipListMap<>(Comparator.reverseOrder());
    // Sell orders: lowest price first (natural order)
    private final ConcurrentSkipListMap<Double, ConcurrentLinkedQueue<Order>> sellLevels = // ConcurrentSkipListMap = lock-free reads; sorted by price
            new ConcurrentSkipListMap<>();
    private final ReentrantLock matchLock = new ReentrantLock(); // ReentrantLock = serializes matching for one symbol; explicit control

    public ConcurrentOrderBook(String symbol) {
        this.symbol = symbol;
    }

    public void addOrder(Order order) {
        ConcurrentSkipListMap<Double, ConcurrentLinkedQueue<Order>> levels =
                (order.getSide() == OrderSide.BUY) ? buyLevels : sellLevels;
        levels.computeIfAbsent(order.getPrice(), k -> new ConcurrentLinkedQueue<>()).offer(order);
    }

    public ConcurrentSkipListMap<Double, ConcurrentLinkedQueue<Order>> getBuyLevels() { return buyLevels; }
    public ConcurrentSkipListMap<Double, ConcurrentLinkedQueue<Order>> getSellLevels() { return sellLevels; }
    public String getSymbol() { return symbol; }
    public ReentrantLock getMatchLock() { return matchLock; }

    public Order getBestBid() {
        Map.Entry<Double, ConcurrentLinkedQueue<Order>> entry = buyLevels.firstEntry();
        if (entry == null) return null;
        return entry.getValue().peek();
    }

    public Order getBestAsk() {
        Map.Entry<Double, ConcurrentLinkedQueue<Order>> entry = sellLevels.firstEntry();
        if (entry == null) return null;
        return entry.getValue().peek();
    }
}

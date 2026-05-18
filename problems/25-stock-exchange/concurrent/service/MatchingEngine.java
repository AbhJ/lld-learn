/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/MatchingEngine.java — Atomic matching with proper locking

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Matching engine that processes orders and matches them atomically.
 * Uses per-orderbook locking to allow parallel matching across different symbols.
 */
class MatchingEngine {
    private final ConcurrentHashMap<String, ConcurrentOrderBook> orderBooks = new ConcurrentHashMap<>(); // ConcurrentHashMap = one order book per symbol; thread-safe registry
    private final CopyOnWriteArrayList<Trade> trades = new CopyOnWriteArrayList<>(); // CopyOnWriteArrayList = safe iteration; writes copy the array
    private final AtomicInteger ordersProcessed = new AtomicInteger(0); // AtomicInteger = lock-free counter
    private final AtomicInteger tradesExecuted = new AtomicInteger(0);  // AtomicInteger = lock-free counter

    public ConcurrentOrderBook getOrCreateOrderBook(String symbol) {
        return orderBooks.computeIfAbsent(symbol, ConcurrentOrderBook::new);
    }

    /**
     * Submit an order and attempt to match it.
     * Lock is per-order-book, so different symbols can match in parallel.
     */
    public List<Trade> submitOrder(Order order) {
        ordersProcessed.incrementAndGet();
        ConcurrentOrderBook book = getOrCreateOrderBook(order.getSymbol());
        List<Trade> newTrades = new ArrayList<>();

        book.getMatchLock().lock();
        try {
            if (order.getSide() == OrderSide.BUY) {
                matchBuyOrder(order, book, newTrades);
            } else {
                matchSellOrder(order, book, newTrades);
            }

            // If order still has remaining quantity, add to book
            if (order.getRemainingQuantity() > 0) {
                book.addOrder(order);
            }
        } finally {
            book.getMatchLock().unlock();
        }

        trades.addAll(newTrades);
        tradesExecuted.addAndGet(newTrades.size());
        return newTrades;
    }

    private void matchBuyOrder(Order buyOrder, ConcurrentOrderBook book, List<Trade> newTrades) {
        var sellLevels = book.getSellLevels();

        while (buyOrder.getRemainingQuantity() > 0 && !sellLevels.isEmpty()) {
            Map.Entry<Double, ConcurrentLinkedQueue<Order>> bestAsk = sellLevels.firstEntry();
            if (bestAsk == null || bestAsk.getKey() > buyOrder.getPrice()) break;

            ConcurrentLinkedQueue<Order> queue = bestAsk.getValue();
            Order sellOrder = queue.peek();
            if (sellOrder == null) {
                sellLevels.remove(bestAsk.getKey());
                continue;
            }

            int fillQty = Math.min(buyOrder.getRemainingQuantity(), sellOrder.getRemainingQuantity());
            buyOrder.fill(fillQty);
            sellOrder.fill(fillQty);

            newTrades.add(new Trade(buyOrder, sellOrder, bestAsk.getKey(), fillQty));

            if (sellOrder.isFilled()) {
                queue.poll();
                if (queue.isEmpty()) {
                    sellLevels.remove(bestAsk.getKey());
                }
            }
        }
    }

    private void matchSellOrder(Order sellOrder, ConcurrentOrderBook book, List<Trade> newTrades) {
        var buyLevels = book.getBuyLevels();

        while (sellOrder.getRemainingQuantity() > 0 && !buyLevels.isEmpty()) {
            Map.Entry<Double, ConcurrentLinkedQueue<Order>> bestBid = buyLevels.firstEntry();
            if (bestBid == null || bestBid.getKey() < sellOrder.getPrice()) break;

            ConcurrentLinkedQueue<Order> queue = bestBid.getValue();
            Order buyOrder = queue.peek();
            if (buyOrder == null) {
                buyLevels.remove(bestBid.getKey());
                continue;
            }

            int fillQty = Math.min(sellOrder.getRemainingQuantity(), buyOrder.getRemainingQuantity());
            sellOrder.fill(fillQty);
            buyOrder.fill(fillQty);

            newTrades.add(new Trade(buyOrder, sellOrder, bestBid.getKey(), fillQty));

            if (buyOrder.isFilled()) {
                queue.poll();
                if (queue.isEmpty()) {
                    buyLevels.remove(bestBid.getKey());
                }
            }
        }
    }

    public List<Trade> getAllTrades() { return new ArrayList<>(trades); }
    public int getOrdersProcessed() { return ordersProcessed.get(); }
    public int getTradesExecuted() { return tradesExecuted.get(); }
}

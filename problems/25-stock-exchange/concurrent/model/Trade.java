/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Trade.java — Executed trade record

import java.util.concurrent.atomic.AtomicInteger;

class Trade {
    private static final AtomicInteger counter = new AtomicInteger(0); // static AtomicInteger = thread-safe unique trade IDs
    private final String tradeId;                         // final = immutable; safe to read from any thread
    private final Order buyOrder;                         // final = reference won't change after creation
    private final Order sellOrder;                        // final = reference won't change after creation
    private final double price;                           // final = execution price is permanent record
    private final int quantity;                           // final = filled quantity is permanent record
    private final long timestamp;                         // final = when the trade occurred

    public Trade(Order buyOrder, Order sellOrder, double price, int quantity) {
        this.tradeId = "TRD-" + counter.incrementAndGet();
        this.buyOrder = buyOrder;
        this.sellOrder = sellOrder;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = System.currentTimeMillis();
    }

    public String getTradeId() { return tradeId; }
    public Order getBuyOrder() { return buyOrder; }
    public Order getSellOrder() { return sellOrder; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public long getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return tradeId + " [" + buyOrder.getTraderId() + " bought " + quantity +
               " from " + sellOrder.getTraderId() + " @" + price + "]";
    }

    public static void resetCounter() { counter.set(0); }
}

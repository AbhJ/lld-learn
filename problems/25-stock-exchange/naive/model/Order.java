/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Order.java — Base trade order with fill tracking
public abstract class Order {                             // abstract = base for LimitOrder and MarketOrder
    private String id;
    private Trader trader;
    private Stock stock;
    private OrderSide side;                               // private = BUY or SELL direction
    private int quantity;
    private int filledQuantity;                           // private = tracks partial fills
    private OrderStatus status;                           // private = lifecycle state (OPEN/FILLED/etc.)
    private long timestamp;                               // private = for time-priority in matching

    public Order(String id, Trader trader, Stock stock, OrderSide side, int quantity) {
        this.id = id; this.trader = trader; this.stock = stock; this.side = side;
        this.quantity = quantity; this.filledQuantity = 0; this.status = OrderStatus.OPEN;
        this.timestamp = System.nanoTime();
    }

    public String getId() { return id; }
    public Trader getTrader() { return trader; }
    public Stock getStock() { return stock; }
    public OrderSide getSide() { return side; }
    public int getQuantity() { return quantity; }
    public int getFilledQuantity() { return filledQuantity; }
    public int getRemainingQuantity() { return quantity - filledQuantity; }
    public OrderStatus getStatus() { return status; }
    public long getTimestamp() { return timestamp; }
    public abstract double getPrice();                     // abstract = Limit has fixed price, Market uses sentinel
    public abstract boolean isMarketOrder();              // abstract = subclass identifies its own type

    public void fill(int qty) {
        this.filledQuantity += qty;
        this.status = filledQuantity >= quantity ? OrderStatus.FILLED : OrderStatus.PARTIALLY_FILLED;
    }
    public void cancel() { this.status = OrderStatus.CANCELLED; }
}

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Order.java — Order with atomic state management for concurrent matching

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

enum OrderSide { BUY, SELL }
enum OrderStatus { PENDING, PARTIAL, FILLED, CANCELLED }

class Order {
    private static final AtomicInteger counter = new AtomicInteger(0); // static AtomicInteger = thread-safe global ID generator
    private final String orderId;                         // final = immutable after construction; safe publication
    private final String traderId;                        // final = never changes; no synchronization needed
    private final String symbol;                          // final = immutable; shared safely across threads
    private final OrderSide side;                         // final = direction fixed at creation
    private final double price;                           // final = limit price never changes
    private final int originalQuantity;                   // final = initial qty for reference
    private final AtomicInteger remainingQuantity;        // AtomicInteger = CAS-based partial fill without locks
    private final AtomicReference<OrderStatus> status;    // AtomicReference = lock-free status transitions
    private final long timestamp;                         // final = creation time for price-time priority

    public Order(String traderId, String symbol, OrderSide side, double price, int quantity) {
        this.orderId = "ORD-" + counter.incrementAndGet();
        this.traderId = traderId;
        this.symbol = symbol;
        this.side = side;
        this.price = price;
        this.originalQuantity = quantity;
        this.remainingQuantity = new AtomicInteger(quantity);
        this.status = new AtomicReference<>(OrderStatus.PENDING);
        this.timestamp = System.nanoTime();
    }

    /**
     * Atomically reduce quantity. Returns actual amount filled.
     * Handles partial fills correctly under concurrency.
     */
    public int fill(int quantity) {
        while (true) {
            int current = remainingQuantity.get();
            if (current <= 0) return 0;
            int toFill = Math.min(quantity, current);
            if (remainingQuantity.compareAndSet(current, current - toFill)) {
                if (current - toFill == 0) {
                    status.set(OrderStatus.FILLED);
                } else {
                    status.set(OrderStatus.PARTIAL);
                }
                return toFill;
            }
            // CAS failed — another thread filled some quantity, retry
        }
    }

    public String getOrderId() { return orderId; }
    public String getTraderId() { return traderId; }
    public String getSymbol() { return symbol; }
    public OrderSide getSide() { return side; }
    public double getPrice() { return price; }
    public int getOriginalQuantity() { return originalQuantity; }
    public int getRemainingQuantity() { return remainingQuantity.get(); }
    public OrderStatus getStatus() { return status.get(); }
    public long getTimestamp() { return timestamp; }

    public boolean isFilled() { return status.get() == OrderStatus.FILLED; }

    @Override
    public String toString() {
        return orderId + " [" + side + " " + symbol + " " + remainingQuantity.get() + "@" + price + " " + status.get() + "]";
    }

    public static void resetCounter() { counter.set(0); }
}

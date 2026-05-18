/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/LimitOrder.java — Order with price constraint
public class LimitOrder extends Order {                    // extends = inherits fill-tracking from Order
    private double price;                                 // private = the limit price for this order
    public LimitOrder(String id, Trader trader, Stock stock, OrderSide side, int quantity, double price) {
        super(id, trader, stock, side, quantity); this.price = price;
    }
    @Override public double getPrice() { return price; }
    @Override public boolean isMarketOrder() { return false; }
}

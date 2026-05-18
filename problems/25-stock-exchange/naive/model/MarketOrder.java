/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/MarketOrder.java — Immediate execution at best available price
public class MarketOrder extends Order {                   // extends = inherits Order; executes at any price
    public MarketOrder(String id, Trader trader, Stock stock, OrderSide side, int quantity) {
        super(id, trader, stock, side, quantity);
    }
    @Override public double getPrice() { return getSide() == OrderSide.BUY ? Double.MAX_VALUE : 0; }
    @Override public boolean isMarketOrder() { return true; }
}

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/OrderBook.java — Naive: unsorted lists requiring O(n) scan for best price
import java.util.*;

public class OrderBook {
    private Stock stock;
    // Naive: unsorted lists — finding best price requires O(n) scan
    private List<Order> buyOrders;                        // private = unsorted; O(n) scan for best buy
    private List<Order> sellOrders;                       // private = unsorted; O(n) scan for best sell

    public OrderBook(Stock stock) {
        this.stock = stock;
        this.buyOrders = new ArrayList<>();
        this.sellOrders = new ArrayList<>();
    }

    public void addOrder(Order order) {
        if (order.getSide() == OrderSide.BUY) buyOrders.add(order);
        else sellOrders.add(order);
    }

    // O(n) scan to find best buy (highest price)
    public Order getBestBuy() {
        Order best = null;
        for (Order o : buyOrders) {
            if (o.getRemainingQuantity() > 0 && (best == null || o.getPrice() > best.getPrice())) best = o;
        }
        return best;
    }

    // O(n) scan to find best sell (lowest price)
    public Order getBestSell() {
        Order best = null;
        for (Order o : sellOrders) {
            if (o.getRemainingQuantity() > 0 && (best == null || o.getPrice() < best.getPrice())) best = o;
        }
        return best;
    }

    public void removeFilledOrders() {
        buyOrders.removeIf(o -> o.getStatus() == OrderStatus.FILLED || o.getStatus() == OrderStatus.CANCELLED);
        sellOrders.removeIf(o -> o.getStatus() == OrderStatus.FILLED || o.getStatus() == OrderStatus.CANCELLED);
    }

    public Stock getStock() { return stock; }
    public List<Order> getBuyOrders() { return buyOrders; }
    public List<Order> getSellOrders() { return sellOrders; }
}

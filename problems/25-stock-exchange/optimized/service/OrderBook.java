/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/OrderBook.java — TreeMap-based order book for O(log n) best-price access
import java.util.*;

public class OrderBook {
    private Stock stock;
    // TreeMap keeps orders sorted by price: O(log n) insert, O(1) best price
    // Buy side: descending price (highest first) for price-time priority
    private TreeMap<Double, Queue<Order>> buyLevels;      // TreeMap = sorted by price; firstEntry() gives best bid
    // Sell side: ascending price (lowest first) for price-time priority
    private TreeMap<Double, Queue<Order>> sellLevels;     // TreeMap = sorted naturally; firstEntry() gives best ask

    public OrderBook(Stock stock) {
        this.stock = stock;
        this.buyLevels = new TreeMap<>(Collections.reverseOrder()); // highest first
        this.sellLevels = new TreeMap<>(); // lowest first
    }

    public void addOrder(Order order) {
        TreeMap<Double, Queue<Order>> levels = order.getSide() == OrderSide.BUY ? buyLevels : sellLevels;
        levels.computeIfAbsent(order.getPrice(), k -> new LinkedList<>()).add(order);
    }

    // O(1): first entry of sorted map
    public Order getBestBuy() {
        while (!buyLevels.isEmpty()) {
            Queue<Order> queue = buyLevels.firstEntry().getValue();
            while (!queue.isEmpty()) {
                Order o = queue.peek();
                if (o.getRemainingQuantity() > 0) return o;
                queue.poll(); // remove filled
            }
            buyLevels.pollFirstEntry(); // remove empty price level
        }
        return null;
    }

    // O(1): first entry of sorted map
    public Order getBestSell() {
        while (!sellLevels.isEmpty()) {
            Queue<Order> queue = sellLevels.firstEntry().getValue();
            while (!queue.isEmpty()) {
                Order o = queue.peek();
                if (o.getRemainingQuantity() > 0) return o;
                queue.poll();
            }
            sellLevels.pollFirstEntry();
        }
        return null;
    }

    public void removeFilled(Order order) {
        TreeMap<Double, Queue<Order>> levels = order.getSide() == OrderSide.BUY ? buyLevels : sellLevels;
        Queue<Order> queue = levels.get(order.getPrice());
        if (queue != null) {
            queue.remove(order);
            if (queue.isEmpty()) levels.remove(order.getPrice());
        }
    }

    public Stock getStock() { return stock; }
}

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/PizzaShop.java — Shop with concurrent order tracking
import java.util.concurrent.ConcurrentHashMap;

public class PizzaShop {
    // ConcurrentHashMap for thread-safe concurrent order tracking
    private ConcurrentHashMap<String, Order> activeOrders; // ConcurrentHashMap = O(1) thread-safe order lookup
    private int orderCounter;                              // sequential counter for unique order IDs

    public PizzaShop() { this.activeOrders = new ConcurrentHashMap<>(); this.orderCounter = 0; }

    public Order createOrder() {
        String id = "ORD-" + String.format("%03d", ++orderCounter);
        Order order = new Order(id);
        activeOrders.put(id, order);
        return order;
    }

    // WHY: O(1) order lookup by ID for status checks
    public Order getOrder(String orderId) { return activeOrders.get(orderId); }

    public void processOrder(Order order) {
        while (order.getStatus() != Order.OrderStatus.DELIVERED) order.advanceStatus();
        // WHY: Remove from active orders when delivered to free memory
        activeOrders.remove(order.getId());
    }
}

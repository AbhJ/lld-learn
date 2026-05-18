/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/OrderService.java — Orchestrates order creation and lifecycle
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderService {
    private Map<String, Order> orders; // HashMap = O(1) order lookup by ID
    private int orderCounter;          // sequential counter for unique order IDs

    public OrderService() { this.orders = new HashMap<>(); this.orderCounter = 0; }

    public Order createOrder(List<OrderItem> items) {
        String orderId = "ORD-" + String.format("%03d", ++orderCounter);
        Order order = new Order(orderId);
        for (OrderItem item : items) order.addItem(item);
        orders.put(orderId, order);
        return order;
    }

    public Order getOrder(String orderId) { return orders.get(orderId); }
}

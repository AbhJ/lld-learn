/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/PizzaShop.java — Manages pizza ordering and delivery
import java.util.ArrayList;
import java.util.List;

public class PizzaShop {
    private List<Order> orders;    // private = order list managed internally
    private int orderCounter;      // private = generates unique order IDs

    public PizzaShop() { this.orders = new ArrayList<>(); this.orderCounter = 0; }

    public Order createOrder() {
        String id = "ORD-" + String.format("%03d", ++orderCounter);
        Order order = new Order(id);
        orders.add(order);
        return order;
    }

    public void processOrder(Order order) {
        while (order.getStatus() != Order.OrderStatus.DELIVERED) order.advanceStatus();
    }
}

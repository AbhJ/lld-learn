/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Order.java — Food order with items, customer, restaurant, and state tracking

import java.util.*;
import java.time.LocalDateTime;

public class Order {
    private String orderId;              // private = encapsulated unique order ID
    private Customer customer;           // private = who placed the order
    private Restaurant restaurant;       // private = which restaurant prepares food
    private Map<MenuItem, Integer> items; // private = order items with quantities
    private OrderState state;            // private = current lifecycle state (enum)
    private DeliveryAgent agent;         // private = assigned driver; null until dispatched
    private LocalDateTime orderTime;     // private = when order was placed
    private LocalDateTime deliveryTime;  // private = when order was delivered

    private static int counter = 0;      // static = shared counter for unique order IDs

    public Order(Customer customer, Restaurant restaurant) {
        this.orderId = "ORD-" + (++counter);
        this.customer = customer;
        this.restaurant = restaurant;
        this.items = new LinkedHashMap<>();
        this.state = OrderState.PLACED;
        this.orderTime = LocalDateTime.now();
    }

    public void addItem(MenuItem item, int quantity) {
        items.put(item, items.getOrDefault(item, 0) + quantity);
    }

    public boolean transition(OrderState newState) {
        if (state.canTransitionTo(newState)) {
            state = newState;
            if (newState == OrderState.DELIVERED) {
                deliveryTime = LocalDateTime.now();
            }
            customer.onOrderUpdate(this);
            return true;
        }
        System.out.println("Invalid transition: " + state + " -> " + newState);
        return false;
    }

    public double getTotalAmount() {
        double total = 0;
        for (Map.Entry<MenuItem, Integer> entry : items.entrySet()) {
            total += entry.getKey().getPrice() * entry.getValue();
        }
        return total;
    }

    public void assignAgent(DeliveryAgent agent) {
        this.agent = agent;
        agent.assignOrder();
    }

    public String getOrderId() { return orderId; }
    public Customer getCustomer() { return customer; }
    public Restaurant getRestaurant() { return restaurant; }
    public Map<MenuItem, Integer> getItems() { return Collections.unmodifiableMap(items); }
    public OrderState getState() { return state; }
    public DeliveryAgent getAgent() { return agent; }

    public String getItemsSummary() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<MenuItem, Integer> entry : items.entrySet()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(entry.getValue()).append("x ").append(entry.getKey().getName());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("Order[%s] from %s - %s - $%.2f [%s]",
                orderId, restaurant.getName(), getItemsSummary(), getTotalAmount(), state);
    }
}

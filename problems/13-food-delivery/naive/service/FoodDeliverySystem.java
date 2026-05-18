/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/FoodDeliverySystem.java — Facade orchestrating ordering, agent assignment, and delivery

import java.util.*;

public class FoodDeliverySystem {
    private List<Restaurant> restaurants;       // private = registered restaurant partners
    private List<DeliveryAgent> agents;         // private = registered delivery drivers
    private List<Order> orders;                 // private = all orders in the system
    private DeliveryStrategy deliveryStrategy;  // private = strategy pattern for agent selection

    public FoodDeliverySystem() {
        this.restaurants = new ArrayList<>();
        this.agents = new ArrayList<>();
        this.orders = new ArrayList<>();
        this.deliveryStrategy = new NearestAgentStrategy();
    }

    public void setDeliveryStrategy(DeliveryStrategy strategy) {
        this.deliveryStrategy = strategy;
    }

    public void registerRestaurant(Restaurant restaurant) { restaurants.add(restaurant); }
    public void registerAgent(DeliveryAgent agent) { agents.add(agent); }

    public List<Restaurant> getOpenRestaurants() {
        List<Restaurant> open = new ArrayList<>();
        for (Restaurant r : restaurants) {
            if (r.isOpen()) open.add(r);
        }
        return open;
    }

    public Order placeOrder(Customer customer, Restaurant restaurant, Map<MenuItem, Integer> items) {
        if (!restaurant.isOpen()) {
            System.out.println("Restaurant is closed.");
            return null;
        }
        Order order = new Order(customer, restaurant);
        for (Map.Entry<MenuItem, Integer> entry : items.entrySet()) {
            order.addItem(entry.getKey(), entry.getValue());
        }
        orders.add(order);
        System.out.println("Order placed: " + order);
        return order;
    }

    public boolean confirmOrder(Order order) {
        return order.transition(OrderState.CONFIRMED);
    }

    public boolean startPreparing(Order order) {
        return order.transition(OrderState.PREPARING);
    }

    public boolean assignAndDispatch(Order order) {
        DeliveryAgent agent = deliveryStrategy.selectAgent(agents, order.getRestaurant());
        if (agent == null) {
            System.out.println("No available delivery agents!");
            return false;
        }
        order.assignAgent(agent);
        System.out.println("Agent assigned: " + agent.getName() + " (strategy: " + deliveryStrategy.getName() + ")");
        return order.transition(OrderState.OUT_FOR_DELIVERY);
    }

    public boolean deliverOrder(Order order) {
        boolean result = order.transition(OrderState.DELIVERED);
        if (result && order.getAgent() != null) {
            order.getAgent().completeOrder();
        }
        return result;
    }

    public boolean cancelOrder(Order order) {
        boolean result = order.transition(OrderState.CANCELLED);
        if (result && order.getAgent() != null) {
            order.getAgent().completeOrder();
        }
        return result;
    }

    public Rating rateOrder(Order order, double restaurantRating, double agentRating, String comment) {
        Rating rating = new Rating(order.getOrderId(), restaurantRating, agentRating, comment);
        order.getRestaurant().addRating(restaurantRating);
        if (order.getAgent() != null) {
            order.getAgent().addRating(agentRating);
        }
        return rating;
    }

    public List<Order> getOrders() { return Collections.unmodifiableList(orders); }
}

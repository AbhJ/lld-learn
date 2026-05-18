/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/DeliveryTracker.java — ConcurrentHashMap for thread-safe tracking

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DeliveryTracker {
    private final ConcurrentHashMap<String, PizzaOrder> orders; // ConcurrentHashMap = thread-safe order registry; O(1) lookup
    private final AtomicInteger completedCount;                 // AtomicInteger = thread-safe delivered counter

    public DeliveryTracker() {
        this.orders = new ConcurrentHashMap<>();
        this.completedCount = new AtomicInteger(0);
    }

    public void addOrder(PizzaOrder order) {
        orders.put(order.getOrderId(), order);
    }

    public PizzaOrder getOrder(String orderId) {
        return orders.get(orderId);
    }

    /**
     * Progress an order through its full lifecycle.
     * Each transition uses CAS to ensure ordered progression.
     */
    public boolean progressOrder(String orderId) {
        PizzaOrder order = orders.get(orderId);
        if (order == null) return false;

        // Walk through state machine: PLACED -> PREPARING -> BAKING -> READY -> OUT_FOR_DELIVERY -> DELIVERED
        DeliveryState[] progression = DeliveryState.values();
        for (int i = 0; i < progression.length - 1; i++) {
            boolean advanced = order.advanceTo(progression[i], progression[i + 1]);
            if (!advanced) {
                return false; // Someone else already advanced this state
            }
            // Simulate work at each stage
            try { Thread.sleep(1); } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        if (order.isDelivered()) {
            completedCount.incrementAndGet();
        }
        return order.isDelivered();
    }

    public int getCompletedCount() { return completedCount.get(); }
    public int getTotalOrders() { return orders.size(); }

    public Collection<PizzaOrder> getAllOrders() {
        return orders.values();
    }
}

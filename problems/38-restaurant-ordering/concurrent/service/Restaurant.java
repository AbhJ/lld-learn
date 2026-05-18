/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/Restaurant.java — Order queue with thread-safe lifecycle management

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Restaurant {
    private final ConcurrentLinkedQueue<Order> orderQueue = new ConcurrentLinkedQueue<>(); // ConcurrentLinkedQueue = lock-free FIFO; multiple threads can offer/poll
    private final CopyOnWriteArrayList<Order> allOrders = new CopyOnWriteArrayList<>();   // CopyOnWriteArrayList = safe iteration while orders added
    private final AtomicInteger partialOrdersDetected = new AtomicInteger(0); // AtomicInteger = lock-free counter
    private final AtomicInteger ordersCompleted = new AtomicInteger(0);       // AtomicInteger = lock-free counter

    public Order createOrder(int id) {
        Order order = new Order(id);
        allOrders.add(order);
        return order;
    }

    public void submitOrder(Order order) {
        if (order.submit()) {
            orderQueue.offer(order);
        }
    }

    /**
     * Kitchen polls for the next submitted order.
     * Only processes orders in SUBMITTED state.
     */
    public Order kitchenPickup() {
        Order order = orderQueue.poll();
        if (order == null) return null;

        if (order.startPreparing()) {
            return order;
        }
        return null; // order was in unexpected state
    }

    /**
     * Check if an order was picked up with fewer items than expected.
     */
    public void verifyOrder(Order order, int expectedItems) {
        if (order.getItemCount() < expectedItems) {
            partialOrdersDetected.incrementAndGet();
        }
        order.markReady();
        ordersCompleted.incrementAndGet();
    }

    public int getPartialOrdersDetected() { return partialOrdersDetected.get(); }
    public int getOrdersCompleted() { return ordersCompleted.get(); }
    public int getQueueSize() { return orderQueue.size(); }
}

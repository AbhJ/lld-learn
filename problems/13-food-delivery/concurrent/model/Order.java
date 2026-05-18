/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Order.java — Order awaiting delivery agent assignment

import java.util.concurrent.atomic.AtomicLong;

class Order {
    private static final AtomicLong ID_GEN = new AtomicLong(1); // AtomicLong = thread-safe unique ID generator

    private final String orderId;        // final = set once; safe to read from any thread
    private final String customerName;   // final = immutable after construction
    private final String restaurant;     // final = immutable after construction
    private volatile String assignedAgentId; // volatile = visible to all threads when written

    public Order(String customerName, String restaurant) {
        this.orderId = "ORD-" + ID_GEN.getAndIncrement();
        this.customerName = customerName;
        this.restaurant = restaurant;
    }

    public String getOrderId() { return orderId; }
    public String getCustomerName() { return customerName; }
    public String getRestaurant() { return restaurant; }
    public String getAssignedAgentId() { return assignedAgentId; }
    public void setAssignedAgentId(String agentId) { this.assignedAgentId = agentId; }

    @Override
    public String toString() {
        return orderId + " [" + customerName + " from " + restaurant + "]";
    }
}

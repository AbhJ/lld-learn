/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/DeliveryAgent.java — Agent with AtomicReference for CAS-based order assignment

import java.util.concurrent.atomic.AtomicReference;

class DeliveryAgent {
    private final String agentId;   // final = immutable identity; safe to read from any thread
    private final String name;      // final = never changes; safe publication guaranteed
    private final AtomicReference<Order> currentOrder = new AtomicReference<>(null); // AtomicReference = CAS-based assignment; prevents double-booking

    public DeliveryAgent(String agentId, String name) {
        this.agentId = agentId;
        this.name = name;
    }

    /**
     * CAS-based assignment: only ONE order can claim this agent.
     * Atomically transitions from null (free) to the given order (assigned).
     */
    public boolean tryAssign(Order order) {
        return currentOrder.compareAndSet(null, order);
    }

    /**
     * Release agent after delivery is complete.
     */
    public void release() {
        currentOrder.set(null);
    }

    public boolean isFree() {
        return currentOrder.get() == null;
    }

    public Order getCurrentOrder() { return currentOrder.get(); }
    public String getAgentId() { return agentId; }
    public String getName() { return name; }

    @Override
    public String toString() {
        Order o = currentOrder.get();
        return agentId + " (" + name + ") [" + (o == null ? "FREE" : "ASSIGNED: " + o.getOrderId()) + "]";
    }
}

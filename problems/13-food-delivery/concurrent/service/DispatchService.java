/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/DispatchService.java — CAS-based agent assignment preventing double-booking

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe dispatch service.
 *
 * Race condition solved: Multiple orders trying to claim the same delivery agent.
 * Each agent uses AtomicReference<Order> — CAS from null to order ensures
 * only one order can claim each agent.
 */
class DispatchService {
    private final CopyOnWriteArrayList<DeliveryAgent> agents = new CopyOnWriteArrayList<>(); // CopyOnWriteArrayList = safe iteration while adding agents
    private final ConcurrentLinkedQueue<Order> pendingOrders = new ConcurrentLinkedQueue<>(); // ConcurrentLinkedQueue = lock-free queue for pending orders
    private final AtomicInteger assignedCount = new AtomicInteger(0); // AtomicInteger = lock-free counter via CAS
    private final AtomicInteger queuedCount = new AtomicInteger(0);   // AtomicInteger = lock-free counter via CAS

    public void registerAgent(DeliveryAgent agent) {
        agents.add(agent);
    }

    /**
     * Dispatch an order to any available agent.
     * Iterates agents and attempts CAS assignment.
     * If no agent available, order goes to pending queue.
     */
    public boolean dispatch(Order order) {
        for (DeliveryAgent agent : agents) {
            if (agent.tryAssign(order)) {
                // CAS succeeded — this order got the agent
                order.setAssignedAgentId(agent.getAgentId());
                assignedCount.incrementAndGet();
                return true;
            }
            // CAS failed — agent already taken, try next
        }

        // No agent available — queue the order
        pendingOrders.offer(order);
        queuedCount.incrementAndGet();
        return false;
    }

    /**
     * Complete delivery and try to assign next pending order to freed agent.
     */
    public void completeDelivery(DeliveryAgent agent) {
        agent.release();

        // Try to assign a pending order
        Order pending = pendingOrders.poll();
        if (pending != null) {
            if (agent.tryAssign(pending)) {
                pending.setAssignedAgentId(agent.getAgentId());
                assignedCount.incrementAndGet();
                queuedCount.decrementAndGet();
            } else {
                pendingOrders.offer(pending); // re-queue if agent was taken
            }
        }
    }

    public int getAssignedCount() { return assignedCount.get(); }
    public int getQueuedCount() { return queuedCount.get(); }
    public int getPendingQueueSize() { return pendingOrders.size(); }
    public CopyOnWriteArrayList<DeliveryAgent> getAgents() { return agents; }
}

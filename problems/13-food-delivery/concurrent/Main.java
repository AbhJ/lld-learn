/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — 20 orders competing for 5 agents, exactly 5 assigned immediately

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Food Delivery Demo ===\n");

        DispatchService service = new DispatchService();

        // Register 5 delivery agents
        for (int i = 0; i < 5; i++) {
            service.registerAgent(new DeliveryAgent("AGENT-" + i, "Agent-" + i));
        }

        int orderCount = 20;
        System.out.println("Scenario: " + orderCount + " orders competing for 5 delivery agents simultaneously.");
        System.out.println("Expected: Exactly 5 assigned immediately, 15 queued.\n");

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(orderCount);
        AtomicInteger assigned = new AtomicInteger(0);
        AtomicInteger queued = new AtomicInteger(0);
        List<String> results = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < orderCount; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    Order order = new Order("Customer-" + id, "Restaurant-" + (id % 5));
                    boolean dispatched = service.dispatch(order);
                    if (dispatched) {
                        assigned.incrementAndGet();
                        results.add("  [ASSIGNED] " + order.getOrderId() + " -> " + order.getAssignedAgentId());
                    } else {
                        queued.incrementAndGet();
                        results.add("  [QUEUED]   " + order.getOrderId() + " — no agents available");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }, "Order-" + id).start();
        }

        startLatch.countDown();
        doneLatch.await();

        // Print results
        results.sort(String::compareTo);
        for (String r : results) {
            System.out.println(r);
        }

        // Agent states
        System.out.println("\nAgent states:");
        for (DeliveryAgent agent : service.getAgents()) {
            System.out.println("  " + agent);
        }

        // Verification
        System.out.println("\n--- Summary ---");
        System.out.println("Orders: " + orderCount);
        System.out.println("Agents: 5");
        System.out.println("Assigned immediately: " + assigned.get());
        System.out.println("Queued: " + queued.get());
        System.out.println("Pending queue size: " + service.getPendingQueueSize());

        // Verify no agent has multiple orders
        Set<String> assignedAgents = new HashSet<>();
        boolean noDuplicateAssignment = true;
        for (DeliveryAgent agent : service.getAgents()) {
            if (agent.getCurrentOrder() != null) {
                if (!assignedAgents.add(agent.getAgentId())) {
                    noDuplicateAssignment = false;
                }
            }
        }

        boolean correctAssigned = assigned.get() == 5;
        boolean correctQueued = queued.get() == 15;

        System.out.println("\nExactly 5 assigned: " + (correctAssigned ? "PASSED" : "FAILED"));
        System.out.println("Exactly 15 queued: " + (correctQueued ? "PASSED" : "FAILED"));
        System.out.println("No duplicate agent assignment: " + (noDuplicateAssignment ? "PASSED" : "FAILED"));

        boolean allPassed = correctAssigned && correctQueued && noDuplicateAssignment;
        System.out.println("\nOverall: " + (allPassed ? "ALL TESTS PASSED" : "SOME TESTS FAILED"));
    }
}

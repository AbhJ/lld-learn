/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/KitchenQueue.java — Single FIFO queue for kitchen processing
import java.util.LinkedList;
import java.util.Queue;

public class KitchenQueue {
    private final Queue<Order> queue = new LinkedList<>(); // Queue = FIFO; first order placed is first cooked

    public void addOrder(Order order) {
        queue.add(order);
        System.out.println("  [Kitchen] Order#" + order.getId() + " queued (size=" + queue.size() + ")");
    }

    public Order prepareNext() {
        Order order = queue.poll();
        if (order == null) { System.out.println("  [Kitchen] Empty queue"); return null; }
        order.setState("PREPARING");
        System.out.println("  [Kitchen] Preparing Order#" + order.getId());
        order.setState("READY");
        System.out.println("  [Kitchen] Order#" + order.getId() + " READY!");
        return order;
    }

    public int size() { return queue.size(); }
}

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/PriorityKitchen.java — Priority queue (VIP/large first) + station-based routing
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PriorityKitchen {
    // WHY PriorityQueue: VIP orders and large orders get processed first.
    // Naive FIFO treats all orders equally, leading to VIP dissatisfaction
    // and large orders blocking small ones.
    private final PriorityQueue<Order> orderQueue = new PriorityQueue<>(); // PriorityQueue = heap; O(log n) insert, highest-priority poll

    // WHY station routing: Items route to specialized stations (grill, pasta, cold)
    // for parallel preparation, instead of one chef doing everything sequentially.
    private final Map<String, ConcurrentLinkedQueue<String>> stationQueues = new HashMap<>(); // ConcurrentLinkedQueue = thread-safe per-station queue

    public PriorityKitchen() {
        stationQueues.put("grill", new ConcurrentLinkedQueue<>());
        stationQueues.put("pasta", new ConcurrentLinkedQueue<>());
        stationQueues.put("cold", new ConcurrentLinkedQueue<>());
        stationQueues.put("drinks", new ConcurrentLinkedQueue<>());
    }

    public void addOrder(Order order) {
        orderQueue.add(order);
        // Route items to stations
        for (MenuItem item : order.getItems()) {
            ConcurrentLinkedQueue<String> station = stationQueues.get(item.getStation());
            if (station != null) {
                station.add("Order#" + order.getId() + ":" + item.getName());
            }
        }
        System.out.println("  [Kitchen] Queued Order#" + order.getId() +
                " (pri=" + order.getPriority() + ", queue=" + orderQueue.size() + ")");
    }

    // WHY priority poll: Highest-priority order is always prepared first
    public Order prepareNext() {
        Order order = orderQueue.poll();
        if (order == null) return null;
        order.setState("PREPARING");
        System.out.println("  [Kitchen] Preparing Order#" + order.getId() + " (priority=" + order.getPriority() + ")");
        // Simulate station-based parallel prep
        for (MenuItem item : order.getItems()) {
            System.out.println("    [" + item.getStation() + "] " + item.getName());
        }
        order.setState("READY");
        return order;
    }

    public List<Order> prepareAll() {
        List<Order> ready = new ArrayList<>();
        Order o;
        while ((o = prepareNext()) != null) ready.add(o);
        return ready;
    }

    public int size() { return orderQueue.size(); }
    public Map<String, Integer> getStationLoad() {
        Map<String, Integer> load = new HashMap<>();
        for (var e : stationQueues.entrySet()) load.put(e.getKey(), e.getValue().size());
        return load;
    }
}

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Order.java — Order with priority for VIP/size-based scheduling
import java.util.ArrayList;
import java.util.List;

public class Order implements Comparable<Order> { // implements Comparable = PriorityQueue uses this for ordering
    private static int counter = 0;               // static = shared counter for unique IDs across all orders
    private final int id;                         // final = order ID is permanent
    private final int tableNumber;                // final = table assignment fixed
    private final List<MenuItem> items;           // private = encapsulates item list
    private final int priority;                   // final = priority set at creation (VIP=10, large=5, normal=1)
    private volatile String state = "PLACED";     // volatile = state visible to all threads immediately

    public Order(int tableNumber, List<MenuItem> items, int priority) {
        this.id = ++counter;
        this.tableNumber = tableNumber;
        this.items = new ArrayList<>(items);
        this.priority = priority;
    }

    public double getSubtotal() {
        double t = 0; for (MenuItem i : items) t += i.getPrice(); return t;
    }

    // WHY Comparable: PriorityQueue uses this for VIP-first ordering
    @Override
    public int compareTo(Order other) {
        return Integer.compare(other.priority, this.priority); // higher priority first
    }

    public int getId() { return id; }
    public int getTableNumber() { return tableNumber; }
    public List<MenuItem> getItems() { return items; }
    public int getPriority() { return priority; }
    public String getState() { return state; }
    public void setState(String s) { this.state = s; }
    @Override public String toString() { return "Order#" + id + " (pri=" + priority + ") " + state; }
    public static void resetCounter() { counter = 0; }
}

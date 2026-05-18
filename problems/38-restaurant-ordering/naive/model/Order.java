/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Order.java — Customer order with items and state
import java.util.ArrayList;
import java.util.List;

public class Order {
    private static int counter = 0;        // static = shared across all Order instances for ID generation
    private final int id;                  // final = order ID never changes once assigned
    private final int tableNumber;         // final = table assignment is fixed
    private final List<MenuItem> items;    // private = only this class manages the item list
    private String state = "PLACED";       // private = controls order lifecycle externally

    public Order(int tableNumber, List<MenuItem> items) {
        this.id = ++counter;
        this.tableNumber = tableNumber;
        this.items = new ArrayList<>(items);
    }

    public double getSubtotal() {
        double t = 0; for (MenuItem i : items) t += i.getPrice(); return t;
    }

    public int getId() { return id; }
    public int getTableNumber() { return tableNumber; }
    public List<MenuItem> getItems() { return items; }
    public String getState() { return state; }
    public void setState(String s) { this.state = s; }
    @Override public String toString() { return "Order#" + id + " [Table " + tableNumber + "] " + state; }
    public static void resetCounter() { counter = 0; }
}

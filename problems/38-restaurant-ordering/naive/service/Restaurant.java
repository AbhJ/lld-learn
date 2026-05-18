/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/Restaurant.java — Coordinates tables, orders, kitchen, and billing
import java.util.ArrayList;
import java.util.List;

public class Restaurant {
    private final String name;                             // final = restaurant name never changes
    private final List<MenuItem> menu = new ArrayList<>();  // private = only restaurant manages menu
    private final List<Table> tables = new ArrayList<>();   // private = only restaurant manages tables
    private final KitchenQueue kitchen = new KitchenQueue(); // private = internal kitchen queue

    public Restaurant(String name, int tableCount) {
        this.name = name;
        for (int i = 1; i <= tableCount; i++) tables.add(new Table(i));
    }

    public void addMenuItem(MenuItem item) { menu.add(item); }

    public Table assignTable(String customer) {
        for (Table t : tables) {
            if (!t.isOccupied()) { t.assign(customer); return t; }
        }
        return null;
    }

    public Order placeOrder(int tableNum, List<String> itemNames) {
        List<MenuItem> items = new ArrayList<>();
        for (String n : itemNames) {
            for (MenuItem m : menu) {
                if (m.getName().equalsIgnoreCase(n) && m.isAvailable()) { items.add(m); break; }
            }
        }
        if (items.isEmpty()) return null;
        Order order = new Order(tableNum, items);
        kitchen.addOrder(order);
        return order;
    }

    public Order processNext() { return kitchen.prepareNext(); }
    public void serve(Order o) { o.setState("SERVED"); System.out.println("  Order#" + o.getId() + " served"); }

    public double bill(Order o, BillingStrategy strategy) {
        double total = strategy.calculateTotal(o.getSubtotal());
        o.setState("PAID");
        return total;
    }

    public KitchenQueue getKitchen() { return kitchen; }
    public List<Table> getTables() { return tables; }
}

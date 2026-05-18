/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/Restaurant.java — Restaurant with priority queue kitchen and station routing
import java.util.ArrayList;
import java.util.List;

public class Restaurant {
    private final String name;                                 // final = restaurant name fixed
    private final List<MenuItem> menu = new ArrayList<>();      // menu items available
    private final List<Table> tables = new ArrayList<>();       // all tables
    private final PriorityKitchen kitchen = new PriorityKitchen(); // PriorityKitchen = VIP-first processing

    public Restaurant(String name, int regularTables, int vipTables) {
        this.name = name;
        for (int i = 1; i <= regularTables; i++) tables.add(new Table(i, false));
        for (int i = regularTables + 1; i <= regularTables + vipTables; i++) tables.add(new Table(i, true));
    }

    public void addMenuItem(MenuItem item) { menu.add(item); }

    public Table assignTable(String customer, boolean vip) {
        for (Table t : tables) {
            if (!t.isOccupied() && t.isVip() == vip) { t.assign(customer); return t; }
        }
        // Fallback to any available
        for (Table t : tables) {
            if (!t.isOccupied()) { t.assign(customer); return t; }
        }
        return null;
    }

    public Order placeOrder(Table table, List<String> itemNames) {
        List<MenuItem> items = new ArrayList<>();
        for (String n : itemNames) {
            for (MenuItem m : menu) {
                if (m.getName().equalsIgnoreCase(n) && m.isAvailable()) { items.add(m); break; }
            }
        }
        if (items.isEmpty()) return null;
        // WHY priority calc: VIP tables get priority 10, large orders (4+) get 5, normal=1
        int priority = table.isVip() ? 10 : (items.size() >= 4 ? 5 : 1);
        Order order = new Order(table.getNumber(), items, priority);
        kitchen.addOrder(order);
        return order;
    }

    public Order processNext() { return kitchen.prepareNext(); }
    public PriorityKitchen getKitchen() { return kitchen; }
}

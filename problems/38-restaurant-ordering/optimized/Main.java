/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Demonstrates priority-queue kitchen with VIP-first ordering and station routing
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Restaurant Ordering (Optimized: Priority + Stations) Demo ===\n");
        Order.resetCounter();

        Restaurant r = new Restaurant("Java Bistro", 3, 2);
        r.addMenuItem(new MenuItem("Steak", 24.99, "Mains", "grill"));
        r.addMenuItem(new MenuItem("Pasta", 16.99, "Mains", "pasta"));
        r.addMenuItem(new MenuItem("Salad", 7.99, "Appetizers", "cold"));
        r.addMenuItem(new MenuItem("Soup", 5.99, "Appetizers", "cold"));
        r.addMenuItem(new MenuItem("Coffee", 3.99, "Drinks", "drinks"));
        r.addMenuItem(new MenuItem("Wine", 9.99, "Drinks", "drinks"));

        // --- Test 1: Normal vs VIP ordering ---
        System.out.println("--- Test 1: Priority Ordering ---");
        Table normal = r.assignTable("Alice", false);
        Table vip = r.assignTable("Bob-VIP", true);
        Table normal2 = r.assignTable("Charlie", false);

        // Normal order placed FIRST
        Order o1 = r.placeOrder(normal, Arrays.asList("Pasta", "Coffee"));
        // VIP order placed SECOND
        Order o2 = r.placeOrder(vip, Arrays.asList("Steak", "Wine", "Salad"));
        // Another normal order placed THIRD
        Order o3 = r.placeOrder(normal2, Arrays.asList("Soup"));

        // --- Test 2: Process — VIP should come first ---
        System.out.println("\n--- Test 2: Kitchen Processes (VIP first) ---");
        r.processNext(); // Should be o2 (VIP priority=10)
        r.processNext(); // Should be o1 (normal priority=1)
        r.processNext(); // Should be o3 (normal priority=1)

        // --- Test 3: Large Order Priority ---
        System.out.println("\n--- Test 3: Large Order Priority ---");
        Order.resetCounter();
        Restaurant r2 = new Restaurant("Bistro2", 3, 1);
        r2.addMenuItem(new MenuItem("Steak", 24.99, "M", "grill"));
        r2.addMenuItem(new MenuItem("Pasta", 16.99, "M", "pasta"));
        r2.addMenuItem(new MenuItem("Soup", 5.99, "A", "cold"));
        r2.addMenuItem(new MenuItem("Coffee", 3.99, "D", "drinks"));

        Table t1 = r2.assignTable("Small", false);
        Table t2 = r2.assignTable("Large", false);

        Order small = r2.placeOrder(t1, Arrays.asList("Coffee"));
        // Large order (4+ items) gets priority 5
        Order large = r2.placeOrder(t2, Arrays.asList("Steak", "Pasta", "Soup", "Coffee"));

        System.out.println("  Processing order of priority...");
        r2.processNext(); // Large first (pri=5)
        r2.processNext(); // Small (pri=1)

        // --- Test 4: Station Load ---
        System.out.println("\n--- Test 4: Station Load ---");
        System.out.println("  Station queues: " + r.getKitchen().getStationLoad());

        System.out.println("\n=== Restaurant Ordering (Optimized) Demo Complete ===");
    }
}

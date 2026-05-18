/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Demonstrates restaurant with single FIFO kitchen queue
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Restaurant Ordering (Naive) Demo ===\n");
        Order.resetCounter();

        Restaurant r = new Restaurant("Java Bistro", 3);
        r.addMenuItem(new MenuItem("Steak", 24.99, "Mains"));
        r.addMenuItem(new MenuItem("Pasta", 16.99, "Mains"));
        r.addMenuItem(new MenuItem("Soup", 5.99, "Appetizers"));
        r.addMenuItem(new MenuItem("Coffee", 3.99, "Drinks"));

        Table t1 = r.assignTable("Alice");
        Table t2 = r.assignTable("Bob");
        System.out.println("  " + t1 + ", " + t2);

        Order o1 = r.placeOrder(t1.getNumber(), Arrays.asList("Steak", "Coffee"));
        Order o2 = r.placeOrder(t2.getNumber(), Arrays.asList("Pasta", "Soup"));

        r.processNext();
        r.processNext();
        r.serve(o1);
        r.serve(o2);

        double bill1 = r.bill(o1, new DineInBilling(10));
        System.out.println("  Alice total: $" + String.format("%.2f", bill1));
        t1.free();

        System.out.println("\n=== Restaurant Ordering (Naive) Demo Complete ===");
    }
}

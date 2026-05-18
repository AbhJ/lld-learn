/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Demonstrates inventory system with linear warehouse scan
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Inventory Management (Naive) Demo ===\n");

        InventorySystem sys = new InventorySystem();
        sys.addListener(new LoggingLowStockListener());
        sys.addProduct(new Product("LAP001", "Laptop", 999.99, 10));
        sys.addProduct(new Product("PHN001", "Phone", 699.99, 20));

        Warehouse wh1 = new Warehouse("WH1", "Main", "NYC");
        Warehouse wh2 = new Warehouse("WH2", "Branch", "LAX");
        sys.addWarehouse(wh1);
        sys.addWarehouse(wh2);

        System.out.println("--- Add Stock ---");
        sys.addStock("LAP001", "WH1", 100);
        sys.addStock("PHN001", "WH1", 200);
        sys.addStock("LAP001", "WH2", 30);

        System.out.println("\n--- Remove Stock ---");
        sys.removeStock("LAP001", "WH1", 50);
        sys.removeStock("LAP001", "WH1", 45); // triggers low stock

        System.out.println("\n--- Transfer ---");
        sys.transfer("PHN001", "WH1", "WH2", 50);

        System.out.println("\n--- Total Stock (scans all warehouses) ---");
        System.out.println("  Laptop total: " + sys.getTotalStock("LAP001"));
        System.out.println("  Phone total: " + sys.getTotalStock("PHN001"));

        System.out.println("\n--- Alerts ---");
        for (String a : sys.getAlerts()) System.out.println("  " + a);

        System.out.println("\n=== Inventory Management (Naive) Demo Complete ===");
    }
}

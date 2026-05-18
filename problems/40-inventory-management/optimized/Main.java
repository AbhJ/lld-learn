/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Demonstrates indexed inventory with O(1) product lookup and proximity-sorted fulfillment
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Inventory Management (Optimized: Indexed + Proximity) Demo ===\n");

        InventorySystem sys = new InventorySystem();
        sys.addListener(new LoggingLowStockListener());
        sys.addProduct(new Product("LAP001", "Laptop", 999.99, 10));
        sys.addProduct(new Product("PHN001", "Phone", 699.99, 20));

        // Warehouses with distance from customer hub
        Warehouse wh1 = new Warehouse("WH1", "Main-NYC", "NYC", 0);
        Warehouse wh2 = new Warehouse("WH2", "Branch-CHI", "CHI", 800);
        Warehouse wh3 = new Warehouse("WH3", "Branch-LAX", "LAX", 2800);
        sys.addWarehouse(wh1);
        sys.addWarehouse(wh2);
        sys.addWarehouse(wh3);

        System.out.println("--- Add Stock ---");
        sys.addStock("LAP001", "WH1", 100);
        sys.addStock("LAP001", "WH2", 50);
        sys.addStock("LAP001", "WH3", 30);
        sys.addStock("PHN001", "WH1", 200);
        sys.addStock("PHN001", "WH2", 100);

        System.out.println("\n--- Total Stock (O(1) product lookup) ---");
        System.out.println("  Laptop total: " + sys.getTotalStock("LAP001"));
        System.out.println("  Phone total: " + sys.getTotalStock("PHN001"));

        System.out.println("\n--- Find Nearest Fulfiller ---");
        Warehouse nearest = sys.findNearestFulfiller("LAP001", 20);
        System.out.println("  Nearest with 20+ Laptops: " + nearest);
        nearest = sys.findNearestFulfiller("LAP001", 80);
        System.out.println("  Nearest with 80+ Laptops: " + nearest);

        System.out.println("\n--- Remove Stock (triggers alert) ---");
        sys.removeStock("LAP001", "WH1", 95); // drops to 5, below reorder point 10

        System.out.println("\n--- Transfer ---");
        sys.transfer("PHN001", "WH1", "WH3", 50);
        System.out.println("  Phone in LAX after transfer: " + sys.getIndex().getStock("PHN001", wh3));

        System.out.println("\n--- Nearest After Stock Change ---");
        nearest = sys.findNearestFulfiller("LAP001", 20);
        System.out.println("  Nearest with 20+ Laptops (after depletion): " + nearest);

        System.out.println("\n--- All Alerts ---");
        for (String a : sys.getAlerts()) System.out.println("  " + a);

        System.out.println("\n=== Inventory Management (Optimized) Demo Complete ===");
    }
}

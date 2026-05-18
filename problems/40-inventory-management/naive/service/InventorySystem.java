/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/InventorySystem.java — Loops through all warehouses for stock queries
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventorySystem {
    private final Map<String, Product> products = new HashMap<>();    // private = SKU -> Product registry
    private final Map<String, Warehouse> warehouses = new HashMap<>(); // private = ID -> Warehouse registry
    private final List<String> alerts = new ArrayList<>();            // private = low-stock alerts log
    private ReorderStrategy reorderStrategy = new FixedQuantityReorder(50); // private = pluggable strategy

    // Observer: fan-out for low-stock events.
    private final List<LowStockListener> listeners = new ArrayList<>();

    public void setReorderStrategy(ReorderStrategy s) { this.reorderStrategy = s; }
    public void addProduct(Product p) { products.put(p.getSku(), p); }
    public void addWarehouse(Warehouse w) { warehouses.put(w.getId(), w); }

    // === Observer plumbing ===
    public void addListener(LowStockListener l) { listeners.add(l); }
    public void removeListener(LowStockListener l) { listeners.remove(l); }

    public void addStock(String sku, String warehouseId, int qty) {
        Warehouse w = warehouses.get(warehouseId);
        if (w != null) {
            w.addStock(sku, qty);
            System.out.println("  Added " + qty + " x " + products.get(sku).getName() + " to " + w.getName());
        }
    }

    public boolean removeStock(String sku, String warehouseId, int qty) {
        Warehouse w = warehouses.get(warehouseId);
        if (w == null || !w.removeStock(sku, qty)) {
            System.out.println("  Insufficient stock");
            return false;
        }
        System.out.println("  Removed " + qty + " x " + products.get(sku).getName() + " from " + w.getName());
        checkLowStock(sku, warehouseId);
        return true;
    }

    public boolean transfer(String sku, String fromId, String toId, int qty) {
        Warehouse from = warehouses.get(fromId);
        Warehouse to = warehouses.get(toId);
        if (from == null || to == null || !from.removeStock(sku, qty)) return false;
        to.addStock(sku, qty);
        System.out.println("  Transferred " + qty + " x " + products.get(sku).getName() +
                " from " + from.getName() + " to " + to.getName());
        return true;
    }

    // Linear scan: loops through ALL warehouses to sum stock
    public int getTotalStock(String sku) {
        int total = 0;
        for (Warehouse w : warehouses.values()) total += w.getStock(sku);
        return total;
    }

    private void checkLowStock(String sku, String warehouseId) {
        Product p = products.get(sku);
        Warehouse w = warehouses.get(warehouseId);
        int remaining = w.getStock(sku);
        if (remaining <= p.getReorderPoint()) {
            String alert = "[LOW] " + p.getName() + " in " + w.getName() + " (qty=" + remaining + ")";
            alerts.add(alert);
            System.out.println("  ** ALERT: " + alert);
            for (LowStockListener l : listeners) l.onLowStock(sku, warehouseId, remaining);
        }
    }

    public List<String> getAlerts() { return alerts; }
    public Map<String, Warehouse> getWarehouses() { return warehouses; }
}

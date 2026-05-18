/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/InventorySystem.java — Indexed inventory with O(1) product lookup and proximity-sorted warehouses
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventorySystem {
    private final Map<String, Product> products = new HashMap<>();     // HashMap = O(1) product lookup by SKU
    private final Map<String, Warehouse> warehouses = new HashMap<>(); // HashMap = O(1) warehouse lookup by ID
    private final InventoryIndex index = new InventoryIndex();         // InventoryIndex = O(1) + TreeMap for proximity
    private final List<String> alerts = new ArrayList<>();
    private ReorderStrategy reorderStrategy = new FixedQuantityReorder(50);

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
        if (w == null) return;
        index.addStock(sku, w, qty);
        System.out.println("  Added " + qty + " x " + products.get(sku).getName() + " to " + w.getName());
    }

    public boolean removeStock(String sku, String warehouseId, int qty) {
        Warehouse w = warehouses.get(warehouseId);
        if (w == null || !index.removeStock(sku, w, qty)) {
            System.out.println("  Insufficient stock");
            return false;
        }
        System.out.println("  Removed " + qty + " x " + products.get(sku).getName() + " from " + w.getName());
        checkLowStock(sku, w);
        return true;
    }

    public boolean transfer(String sku, String fromId, String toId, int qty) {
        Warehouse from = warehouses.get(fromId);
        Warehouse to = warehouses.get(toId);
        if (!index.removeStock(sku, from, qty)) return false;
        index.addStock(sku, to, qty);
        System.out.println("  Transferred " + qty + " x " + products.get(sku).getName() +
                ": " + from.getName() + " -> " + to.getName());
        return true;
    }

    // O(1) product lookup + sum across warehouses in TreeMap
    public int getTotalStock(String sku) { return index.getTotalStock(sku); }

    // O(log n) — finds nearest warehouse with sufficient stock
    public Warehouse findNearestFulfiller(String sku, int qty) {
        return index.findNearestWithStock(sku, qty);
    }

    private void checkLowStock(String sku, Warehouse w) {
        Product p = products.get(sku);
        int current = index.getStock(sku, w);
        if (current <= p.getReorderPoint()) {
            int reorder = reorderStrategy.calculateReorderQuantity(p, current);
            String alert = "[LOW] " + p.getName() + " in " + w.getName() +
                    " (qty=" + current + ", suggest reorder " + reorder + ")";
            alerts.add(alert);
            System.out.println("  ** ALERT: " + alert);
            for (LowStockListener l : listeners) l.onLowStock(sku, w.getId(), current);
        }
    }

    public List<String> getAlerts() { return alerts; }
    public InventoryIndex getIndex() { return index; }
}

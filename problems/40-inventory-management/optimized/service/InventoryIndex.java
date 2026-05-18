/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/InventoryIndex.java — HashMap<SKU, TreeMap<Warehouse, StockEntry>> for O(1) product lookup
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class InventoryIndex {
    // WHY this structure:
    // - HashMap<SKU, TreeMap>: O(1) to find all stock locations for a product
    // - TreeMap<Warehouse, StockEntry>: Warehouses sorted by distance (proximity)
    //   so "find nearest warehouse with stock" is O(log n) not O(n)
    //
    // Naive approach loops through ALL warehouses for every stock query = O(warehouses).
    private final Map<String, TreeMap<Warehouse, StockEntry>> index = new HashMap<>(); // HashMap + TreeMap = O(1) product + O(log n) nearest

    public void addStock(String sku, Warehouse warehouse, int qty) {
        TreeMap<Warehouse, StockEntry> warehouseMap =
                index.computeIfAbsent(sku, k -> new TreeMap<>());
        StockEntry entry = warehouseMap.get(warehouse);
        if (entry == null) {
            warehouseMap.put(warehouse, new StockEntry(qty));
        } else {
            entry.add(qty);
        }
    }

    public boolean removeStock(String sku, Warehouse warehouse, int qty) {
        TreeMap<Warehouse, StockEntry> warehouseMap = index.get(sku);
        if (warehouseMap == null) return false;
        StockEntry entry = warehouseMap.get(warehouse);
        if (entry == null) return false;
        return entry.remove(qty);
    }

    // O(1) lookup for total stock of a product across all warehouses
    public int getTotalStock(String sku) {
        TreeMap<Warehouse, StockEntry> map = index.get(sku);
        if (map == null) return 0;
        int total = 0;
        for (StockEntry e : map.values()) total += e.getQuantity();
        return total;
    }

    // O(1) lookup + first entry = nearest warehouse with stock
    public Warehouse findNearestWithStock(String sku, int minQty) {
        TreeMap<Warehouse, StockEntry> map = index.get(sku);
        if (map == null) return null;
        // WHY TreeMap iteration: Already sorted by distance, so first match is nearest
        for (Map.Entry<Warehouse, StockEntry> entry : map.entrySet()) {
            if (entry.getValue().getQuantity() >= minQty) {
                return entry.getKey();
            }
        }
        return null;
    }

    public int getStock(String sku, Warehouse warehouse) {
        TreeMap<Warehouse, StockEntry> map = index.get(sku);
        if (map == null) return 0;
        StockEntry entry = map.get(warehouse);
        return entry == null ? 0 : entry.getQuantity();
    }

    public TreeMap<Warehouse, StockEntry> getStockMap(String sku) {
        return index.getOrDefault(sku, new TreeMap<>());
    }
}

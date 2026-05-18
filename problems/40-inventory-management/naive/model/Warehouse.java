/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Warehouse.java — Physical storage location with stock tracking
import java.util.HashMap;
import java.util.Map;

public class Warehouse {
    private final String id;               // final = warehouse ID is permanent
    private final String name;             // final = name never changes
    private final String location;         // final = location is fixed
    private final Map<String, Integer> stock = new HashMap<>(); // private = SKU -> quantity mapping

    public Warehouse(String id, String name, String location) {
        this.id = id; this.name = name; this.location = location;
    }

    public void addStock(String sku, int qty) {
        stock.merge(sku, qty, Integer::sum);
    }

    public boolean removeStock(String sku, int qty) {
        int current = stock.getOrDefault(sku, 0);
        if (current < qty) return false;
        stock.put(sku, current - qty);
        return true;
    }

    public int getStock(String sku) { return stock.getOrDefault(sku, 0); }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public Map<String, Integer> getAllStock() { return stock; }
}

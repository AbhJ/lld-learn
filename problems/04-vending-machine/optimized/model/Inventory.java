/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Inventory.java — Thread-safe inventory with atomic stock operations

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Optimized: Uses ConcurrentHashMap + AtomicInteger for thread-safe
 * inventory operations without synchronized blocks. Dispense uses
 * compareAndSet for atomic decrement, preventing race conditions.
 */
class Inventory {
    private ConcurrentHashMap<String, Product> products; // ConcurrentHashMap = thread-safe product lookup
    private ConcurrentHashMap<String, AtomicInteger> quantities; // AtomicInteger per product = CAS-based stock decrement

    public Inventory() {
        this.products = new ConcurrentHashMap<>();
        this.quantities = new ConcurrentHashMap<>();
    }

    public void addProduct(Product product, int quantity) {
        products.put(product.getCode(), product);
        quantities.computeIfAbsent(product.getCode(), k -> new AtomicInteger(0))
                  .addAndGet(quantity);
    }

    public Product getProduct(String code) {
        return products.get(code);
    }

    public boolean isAvailable(String code) {
        AtomicInteger qty = quantities.get(code);
        return qty != null && qty.get() > 0;
    }

    /**
     * Atomic dispense: uses compareAndSet loop to prevent double-dispense
     * under concurrent access.
     */
    public boolean dispense(String code) {
        AtomicInteger qty = quantities.get(code);
        if (qty == null) return false;
        while (true) {
            int current = qty.get();
            if (current <= 0) return false;
            if (qty.compareAndSet(current, current - 1)) return true;
        }
    }

    public int getQuantity(String code) {
        AtomicInteger qty = quantities.get(code);
        return qty != null ? qty.get() : 0;
    }

    public List<Product> getAllProducts() {
        return new ArrayList<>(products.values());
    }

    public String getDisplayInfo() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Product> entry : products.entrySet()) {
            Product p = entry.getValue();
            int qty = getQuantity(entry.getKey());
            sb.append("  ").append(p).append(" - Stock: ").append(qty);
            if (qty == 0) sb.append(" [SOLD OUT]");
            sb.append("\n");
        }
        return sb.toString().trim();
    }
}

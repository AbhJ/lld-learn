/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/InventoryService.java — Thread-safe inventory with CAS-based stock deduction

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class InventoryService {
    private final ConcurrentHashMap<String, Product> products = new ConcurrentHashMap<>(); // ConcurrentHashMap = lock-free reads, segmented writes
    private final AtomicInteger successfulOrders = new AtomicInteger(0); // AtomicInteger = lock-free counter
    private final AtomicInteger failedOrders = new AtomicInteger(0);     // AtomicInteger = lock-free counter

    public void addProduct(Product product) {
        products.put(product.getId(), product);
    }

    /**
     * Process an order: deduct stock atomically.
     * Returns true only if stock was sufficient and deducted.
     */
    public boolean processOrder(String productId, int quantity) {
        Product product = products.get(productId);
        if (product == null) {
            failedOrders.incrementAndGet();
            return false;
        }

        if (product.tryDeduct(quantity)) {
            successfulOrders.incrementAndGet();
            return true;
        }

        failedOrders.incrementAndGet();
        return false;
    }

    public int getStock(String productId) {
        Product product = products.get(productId);
        return product != null ? product.getStock() : -1;
    }

    public int getSuccessfulOrders() { return successfulOrders.get(); }
    public int getFailedOrders() { return failedOrders.get(); }
}

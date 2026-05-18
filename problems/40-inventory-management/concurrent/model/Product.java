/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Product.java — Product with AtomicInteger stock for thread-safe updates

import java.util.concurrent.atomic.AtomicInteger;

public class Product {
    private final String id;               // final = safe publication; ID visible to all threads
    private final String name;             // final = safe publication; name visible to all threads
    private final AtomicInteger stock;     // AtomicInteger = CAS-based stock; no locks needed for deduction

    public Product(String id, String name, int initialStock) {
        this.id = id;
        this.name = name;
        this.stock = new AtomicInteger(initialStock);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getStock() { return stock.get(); }

    /**
     * Try to deduct quantity using CAS loop.
     * Only succeeds if current stock >= quantity.
     * Prevents overselling.
     */
    public boolean tryDeduct(int quantity) {
        while (true) {
            int current = stock.get();
            if (current < quantity) {
                return false; // Not enough stock
            }
            if (stock.compareAndSet(current, current - quantity)) { // CAS = atomic deduct; prevents overselling
                return true; // Successfully deducted
            }
            // CAS failed — another thread changed stock, retry
        }
    }

    public void restock(int quantity) {
        stock.addAndGet(quantity);          // addAndGet = atomic increment; safe from any thread
    }
}

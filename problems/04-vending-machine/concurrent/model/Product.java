/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Product.java — Product with AtomicInteger stock for thread-safe vending

package model;

import java.util.concurrent.atomic.AtomicInteger;

public class Product {
    private final String name;              // immutable product identity — no sync needed
    private final double price;             // never changes after init — final ensures visibility
    private final AtomicInteger quantity;   // many buyers decrement concurrently — CAS loop prevents overselling

    public Product(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = new AtomicInteger(quantity);
    }

    /**
     * CAS-based purchase: try to decrement quantity.
     * Only succeeds if current quantity > 0.
     */
    public boolean tryPurchase() {
        while (true) {
            int current = quantity.get();
            if (current <= 0) {
                return false;
            }
            if (quantity.compareAndSet(current, current - 1)) {
                return true;
            }
            // CAS failed — another thread changed it, retry
        }
    }

    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity.get(); }

    @Override
    public String toString() {
        return "Product(\"" + name + "\" price=$" + String.format("%.2f", price) + " stock=" + quantity.get() + ")";
    }
}

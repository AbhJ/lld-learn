/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/CartItem.java — Immutable cart item

public class CartItem {
    private final String itemId;              // final = immutable after construction; thread-safe to share
    private final String name;                // final = no synchronization needed to read
    private final double price;               // final = safe publication guaranteed by JMM
    private final int quantity;               // final = quantity fixed at creation time

    public CartItem(String itemId, String name, double price, int quantity) {
        this.itemId = itemId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getItemId() { return itemId; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public double getTotal() { return price * quantity; }

    @Override
    public String toString() {
        return name + " x" + quantity + " @ $" + String.format("%.2f", price);
    }
}

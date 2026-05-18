/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/CartItem.java — Represents a product in the cart with quantity
public class CartItem {
    private Product product;
    private int quantity;

    public CartItem(Product product, int quantity) { this.product = product; this.quantity = quantity; }

    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { if (quantity > 0) this.quantity = quantity; }
    public void incrementQuantity(int amount) { this.quantity += amount; }
    public double getSubtotal() { return product.getPrice() * quantity; }

    @Override public String toString() { return product.getName() + " x" + quantity + ": $" + String.format("%.2f", getSubtotal()); }
}

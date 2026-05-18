/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/OrderItem.java — Represents a single line item in an order
public class OrderItem {
    private String productId;       // private = only this class can access; hides internal data
    private String productName;     // private = encapsulates product name from outside changes
    private double price;           // private = price can only change through this class's methods
    private int quantity;           // private = prevents external code from setting invalid qty

    public OrderItem(String productId, String productName, double price, int quantity) {
        this.productId = productId; this.productName = productName; this.price = price; this.quantity = quantity;
    }

    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public double getSubtotal() { return price * quantity; }

    @Override public String toString() { return productName + " x" + quantity + ": $" + String.format("%.2f", getSubtotal()); }
}

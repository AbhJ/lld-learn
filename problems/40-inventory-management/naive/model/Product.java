/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Product.java — Product in the catalog with reorder point
public class Product {
    private final String sku;              // final = SKU identifier is permanent
    private final String name;             // final = product name never changes
    private final double price;            // final = price fixed at creation
    private final int reorderPoint;        // final = threshold for low-stock alert

    public Product(String sku, String name, double price, int reorderPoint) {
        this.sku = sku; this.name = name; this.price = price; this.reorderPoint = reorderPoint;
    }

    public String getSku() { return sku; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getReorderPoint() { return reorderPoint; }
    @Override public String toString() { return name + " (" + sku + ")"; }
}

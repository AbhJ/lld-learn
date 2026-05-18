/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Product.java — Represents a purchasable product with price and details
public class Product {
    private String id;
    private String name;
    private double price;
    private String category;

    public Product(String id, String name, double price, String category) {
        this.id = id; this.name = name; this.price = price; this.category = category;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getCategory() { return category; }
    public void setPrice(double newPrice) { this.price = newPrice; }

    @Override public String toString() { return name + " ($" + String.format("%.2f", price) + ")"; }
}

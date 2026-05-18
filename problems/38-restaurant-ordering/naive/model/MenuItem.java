/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/MenuItem.java — Single dish or drink with price and category
public class MenuItem {
    private final String name;             // final = dish name never changes
    private final double price;            // final = price is fixed once set
    private final String category;         // final = category (Mains, Drinks, etc.) is immutable
    private boolean available = true;      // private = only this class controls availability

    public MenuItem(String name, double price, String category) {
        this.name = name; this.price = price; this.category = category;
    }

    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getCategory() { return category; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean a) { this.available = a; }
    @Override public String toString() { return name + " ($" + String.format("%.2f", price) + ")"; }
}

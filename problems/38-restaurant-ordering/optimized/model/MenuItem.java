/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/MenuItem.java — Dish with station assignment for routing
public class MenuItem {
    private final String name;             // final = dish name never changes
    private final double price;            // final = price is fixed
    private final String category;         // final = food category is immutable
    private final String station;          // final = assigned kitchen station (grill, pasta, cold, drinks)
    private boolean available = true;

    public MenuItem(String name, double price, String category, String station) {
        this.name = name; this.price = price; this.category = category; this.station = station;
    }

    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getCategory() { return category; }
    public String getStation() { return station; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean a) { available = a; }
    @Override public String toString() { return name + " ($" + String.format("%.2f", price) + ")"; }
}

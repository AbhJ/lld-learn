/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/MenuItem.java — Menu item with name, price, and availability

public class MenuItem {
    private String itemId;          // private = encapsulated unique identifier
    private String name;            // private = item name; accessed via getter
    private String description;     // private = item details; accessed via getter
    private double price;           // private = pricing hidden; accessed via getter
    private boolean available;      // private = availability controlled by restaurant

    public MenuItem(String itemId, String name, String description, double price) {
        this.itemId = itemId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.available = true;
    }

    public String getItemId() { return itemId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public String toString() {
        return String.format("%s - $%.2f%s", name, price, available ? "" : " [Unavailable]");
    }
}

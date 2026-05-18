/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Restaurant.java — Restaurant partner with menu, location, and ratings

import java.util.*;

public class Restaurant {
    private String restaurantId;    // private = encapsulated unique identifier
    private String name;            // private = restaurant name; accessed via getter
    private String address;         // private = location details; accessed via getter
    private double latitude;        // private = GPS coordinate for distance calculation
    private double longitude;       // private = GPS coordinate for distance calculation
    private List<MenuItem> menu;    // private = menu managed internally; exposed read-only
    private List<Double> ratings;   // private = rating history encapsulated
    private boolean open;           // private = open/closed status managed internally

    public Restaurant(String restaurantId, String name, String address, double lat, double lng) {
        this.restaurantId = restaurantId;
        this.name = name;
        this.address = address;
        this.latitude = lat;
        this.longitude = lng;
        this.menu = new ArrayList<>();
        this.ratings = new ArrayList<>();
        this.open = true;
    }

    public void addMenuItem(MenuItem item) { menu.add(item); }
    public void removeMenuItem(String itemId) { menu.removeIf(i -> i.getItemId().equals(itemId)); }

    public List<MenuItem> getMenu() { return Collections.unmodifiableList(menu); }
    public List<MenuItem> getAvailableMenu() {
        List<MenuItem> available = new ArrayList<>();
        for (MenuItem item : menu) {
            if (item.isAvailable()) available.add(item);
        }
        return available;
    }

    public void addRating(double rating) {
        if (rating >= 1.0 && rating <= 5.0) ratings.add(rating);
    }

    public double getAverageRating() {
        if (ratings.isEmpty()) return 0.0;
        double sum = 0;
        for (double r : ratings) sum += r;
        return sum / ratings.size();
    }

    public String getRestaurantId() { return restaurantId; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public boolean isOpen() { return open; }
    public void setOpen(boolean open) { this.open = open; }

    @Override
    public String toString() {
        return String.format("%s (%.1f stars) - %s", name, getAverageRating(), address);
    }
}

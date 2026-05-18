/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/DeliveryAgent.java — Delivery driver with location, availability, and rating

import java.util.*;

public class DeliveryAgent {
    private String agentId;         // unique agent identifier
    private String name;            // agent display name
    private double latitude;        // GPS lat; used as spatial grid index key
    private double longitude;       // GPS lng; used as spatial grid index key
    private boolean available;      // controls whether agent appears in grid index
    private int activeOrders;       // load factor used by PriorityDispatchStrategy
    private List<Double> ratings;   // ArrayList = O(1) append for rolling average

    public DeliveryAgent(String agentId, String name, double lat, double lng) {
        this.agentId = agentId;
        this.name = name;
        this.latitude = lat;
        this.longitude = lng;
        this.available = true;
        this.activeOrders = 0;
        this.ratings = new ArrayList<>();
    }

    public void assignOrder() { activeOrders++; available = activeOrders < 3; }
    public void completeOrder() { activeOrders--; available = true; }

    public double distanceTo(double lat, double lng) {
        // Simple Euclidean distance for demonstration
        double dx = this.latitude - lat;
        double dy = this.longitude - lng;
        return Math.sqrt(dx * dx + dy * dy);
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

    public String getAgentId() { return agentId; }
    public String getName() { return name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public int getActiveOrders() { return activeOrders; }

    @Override
    public String toString() {
        return String.format("Agent %s (%s) - %d active orders, %.1f rating",
                name, available ? "Available" : "Busy", activeOrders, getAverageRating());
    }
}

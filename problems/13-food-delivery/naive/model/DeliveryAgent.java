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
    private String agentId;         // private = encapsulated unique identifier
    private String name;            // private = agent name; accessed via getter
    private double latitude;        // private = current GPS position
    private double longitude;       // private = current GPS position
    private boolean available;      // private = availability managed internally
    private int activeOrders;       // private = tracks how many orders agent is carrying
    private List<Double> ratings;   // private = rating history encapsulated

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

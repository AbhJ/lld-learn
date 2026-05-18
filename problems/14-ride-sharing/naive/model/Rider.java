/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Rider.java — Rider with personal info and trip history

import java.util.ArrayList;
import java.util.List;

public class Rider {
    private String riderId;           // private = only this class can access; encapsulates data
    private String name;              // private = hidden from outside; must use getter to read
    private String phone;             // private = contact info encapsulated
    private Location currentLocation; // private = GPS position managed internally
    private List<Double> ratings;     // private = rating history encapsulated

    public Rider(String riderId, String name, String phone, Location location) {
        this.riderId = riderId;
        this.name = name;
        this.phone = phone;
        this.currentLocation = location;
        this.ratings = new ArrayList<>();
    }

    public void onTripUpdate(Trip trip) {
        System.out.println("  [Rider " + name + "] Trip " + trip.getTripId() + " -> " + trip.getState());
    }

    public void addRating(double rating) { if (rating >= 1 && rating <= 5) ratings.add(rating); }

    public double getAverageRating() {
        if (ratings.isEmpty()) return 5.0;
        double sum = 0;
        for (double r : ratings) sum += r;
        return sum / ratings.size();
    }

    public String getRiderId() { return riderId; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public Location getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(Location loc) { this.currentLocation = loc; }

    @Override
    public String toString() { return String.format("Rider[%s, %.1f stars]", name, getAverageRating()); }
}

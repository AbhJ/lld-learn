/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Driver.java — Driver with vehicle info, location, availability, and rating

import java.util.ArrayList;
import java.util.List;

public class Driver {
    public enum Status { AVAILABLE, ON_TRIP, OFFLINE } // enum = fixed set of driver states

    private String driverId;          // private = only this class can access; encapsulates data
    private String name;              // private = hidden from outside; must use getter to read
    private String vehicleType;       // private = encapsulates vehicle category
    private String licensePlate;      // private = encapsulates plate number
    private Location currentLocation; // private = GPS position managed internally
    private Status status;            // private = state managed by trip lifecycle
    private List<Double> ratings;     // private = rating history encapsulated

    public Driver(String driverId, String name, String vehicleType, String licensePlate, Location location) {
        this.driverId = driverId;
        this.name = name;
        this.vehicleType = vehicleType;
        this.licensePlate = licensePlate;
        this.currentLocation = location;
        this.status = Status.AVAILABLE;
        this.ratings = new ArrayList<>();
    }

    public void onTripUpdate(Trip trip) {
        System.out.println("  [Driver " + name + "] Trip " + trip.getTripId() + " -> " + trip.getState());
    }

    public void addRating(double rating) { if (rating >= 1 && rating <= 5) ratings.add(rating); }

    public double getAverageRating() {
        if (ratings.isEmpty()) return 5.0;
        double sum = 0;
        for (double r : ratings) sum += r;
        return sum / ratings.size();
    }

    public double distanceTo(Location loc) {
        return currentLocation.distanceTo(loc);
    }

    public String getDriverId() { return driverId; }
    public String getName() { return name; }
    public String getVehicleType() { return vehicleType; }
    public String getLicensePlate() { return licensePlate; }
    public Location getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(Location loc) { this.currentLocation = loc; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public boolean isAvailable() { return status == Status.AVAILABLE; }

    @Override
    public String toString() {
        return String.format("Driver[%s, %s %s, %.1f stars, %s]",
                name, vehicleType, licensePlate, getAverageRating(), status);
    }
}

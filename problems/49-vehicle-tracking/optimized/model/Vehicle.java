/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Vehicle.java — Vehicle with circular buffer and distance-based dedup
public class Vehicle {
    private String id;                          // private = vehicle ID encapsulated
    private String name;                        // private = display name
    private String type;                        // private = vehicle type (Truck, Car, etc.)
    private VehicleState state;                 // private = current state managed via setState()
    private Location currentLocation;           // private = latest GPS position
    // CircularBuffer instead of ArrayList prevents unbounded memory growth
    private CircularBuffer locationHistory;     // CircularBuffer = bounded O(1) add; oldest entries dropped
    private double currentSpeedKmh;             // private = last calculated speed
    private static final double MIN_DISTANCE_METERS = 5.0; // static final = dedup threshold constant

    public Vehicle(String id, String name, String type) {
        this.id = id; this.name = name; this.type = type;
        this.state = VehicleState.PARKED;
        this.locationHistory = new CircularBuffer(500);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public VehicleState getState() { return state; }
    public Location getCurrentLocation() { return currentLocation; }
    public CircularBuffer getLocationHistory() { return locationHistory; }
    public double getCurrentSpeedKmh() { return currentSpeedKmh; }

    public void setState(VehicleState state) { this.state = state; }
    public void setCurrentSpeedKmh(double speed) { this.currentSpeedKmh = speed; }

    // WHY: Distance-based dedup — only store if moved > threshold, reducing noise
    public boolean updateLocation(Location location) {
        if (currentLocation != null && currentLocation.distanceTo(location) < MIN_DISTANCE_METERS) {
            return false; // Dedup: too close to previous point
        }
        this.currentLocation = location;
        this.locationHistory.add(location);
        return true;
    }

    @Override public String toString() { return name + " (" + type + ") - " + state; }
}

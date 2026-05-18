/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Vehicle.java — Represents a tracked vehicle
import java.util.ArrayList;
import java.util.List;

public class Vehicle {
    private String id;                          // private = vehicle ID encapsulated
    private String name;                        // private = display name
    private String type;                        // private = vehicle type (Truck, Car, etc.)
    private VehicleState state;                 // private = current state managed via setState()
    private Location currentLocation;           // private = latest GPS position
    // Stores ALL GPS points (unbounded memory)
    private List<Location> locationHistory;     // private = full history; grows without bound
    private double currentSpeedKmh;             // private = last calculated speed

    public Vehicle(String id, String name, String type) {
        this.id = id; this.name = name; this.type = type;
        this.state = VehicleState.PARKED; this.locationHistory = new ArrayList<>();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public VehicleState getState() { return state; }
    public Location getCurrentLocation() { return currentLocation; }
    public List<Location> getLocationHistory() { return locationHistory; }
    public double getCurrentSpeedKmh() { return currentSpeedKmh; }

    public void setState(VehicleState state) { this.state = state; }
    public void setCurrentSpeedKmh(double speed) { this.currentSpeedKmh = speed; }

    public void updateLocation(Location location) {
        this.currentLocation = location;
        this.locationHistory.add(location);
    }

    @Override public String toString() { return name + " (" + type + ") - " + state; }
}

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Vehicle.java — Rental vehicle hierarchy with type, make, model, and availability

public abstract class Vehicle {        // abstract = can't instantiate; use Car/SUV/Van/Truck
    private String vehicleId;          // indexed by PriorityQueue in RentalSystem
    private String make;               // part of vehicle description
    private String model;              // part of vehicle description
    private int year;                  // model year for display
    private String licensePlate;       // unique identifier per vehicle
    private double baseDailyRate;      // used as PriorityQueue sort key (cheapest first)
    private boolean available;         // controls index/deindex in optimized RentalSystem
    private Location currentLocation;  // used as secondary index key in type+location map

    public Vehicle(String vehicleId, String make, String model, int year,
                   String licensePlate, double baseDailyRate, Location location) {
        this.vehicleId = vehicleId;
        this.make = make;
        this.model = model;
        this.year = year;
        this.licensePlate = licensePlate;
        this.baseDailyRate = baseDailyRate;
        this.available = true;
        this.currentLocation = location;
    }

    public abstract String getType();

    public String getVehicleId() { return vehicleId; }
    public String getMake() { return make; }
    public String getModel() { return model; }
    public int getYear() { return year; }
    public String getLicensePlate() { return licensePlate; }
    public double getBaseDailyRate() { return baseDailyRate; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public Location getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(Location location) { this.currentLocation = location; }

    @Override
    public String toString() {
        return String.format("%s %s (%s) - $%.2f/day [%s]",
                make, model, getType(), baseDailyRate, available ? "Available" : "Rented");
    }
}

class Car extends Vehicle {
    public Car(String id, String make, String model, int year, String plate, double rate, Location loc) {
        super(id, make, model, year, plate, rate, loc);
    }
    @Override public String getType() { return "Car"; }
}

class SUV extends Vehicle {
    public SUV(String id, String make, String model, int year, String plate, double rate, Location loc) {
        super(id, make, model, year, plate, rate, loc);
    }
    @Override public String getType() { return "SUV"; }
}

class Van extends Vehicle {
    public Van(String id, String make, String model, int year, String plate, double rate, Location loc) {
        super(id, make, model, year, plate, rate, loc);
    }
    @Override public String getType() { return "Van"; }
}

class Truck extends Vehicle {
    public Truck(String id, String make, String model, int year, String plate, double rate, Location loc) {
        super(id, make, model, year, plate, rate, loc);
    }
    @Override public String getType() { return "Truck"; }
}

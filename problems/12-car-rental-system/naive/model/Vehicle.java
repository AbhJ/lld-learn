/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Vehicle.java — Rental vehicle hierarchy with type, make, model, and availability

public abstract class Vehicle {       // abstract = can't create Vehicle directly; must subclass
    private String vehicleId;         // private = only this class can access; encapsulates data
    private String make;              // private = hidden from outside; must use getter to read
    private String model;             // private = encapsulated; accessed via getModel()
    private int year;                 // private = encapsulated; accessed via getYear()
    private String licensePlate;      // private = encapsulates plate; never exposed raw
    private double baseDailyRate;     // private = pricing data; only getters expose it
    private boolean available;        // private = availability tracked internally
    private Location currentLocation; // private = location managed by rental system logic

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

    public abstract String getType(); // abstract = subclass MUST provide its own version

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

class Car extends Vehicle { // extends = inherits from Vehicle; IS-A relationship
    public Car(String id, String make, String model, int year, String plate, double rate, Location loc) {
        super(id, make, model, year, plate, rate, loc);
    }
    @Override public String getType() { return "Car"; }
}

class SUV extends Vehicle { // extends = inherits from Vehicle; IS-A relationship
    public SUV(String id, String make, String model, int year, String plate, double rate, Location loc) {
        super(id, make, model, year, plate, rate, loc);
    }
    @Override public String getType() { return "SUV"; }
}

class Van extends Vehicle { // extends = inherits from Vehicle; IS-A relationship
    public Van(String id, String make, String model, int year, String plate, double rate, Location loc) {
        super(id, make, model, year, plate, rate, loc);
    }
    @Override public String getType() { return "Van"; }
}

class Truck extends Vehicle { // extends = inherits from Vehicle; IS-A relationship
    public Truck(String id, String make, String model, int year, String plate, double rate, Location loc) {
        super(id, make, model, year, plate, rate, loc);
    }
    @Override public String getType() { return "Truck"; }
}

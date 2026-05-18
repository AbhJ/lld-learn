/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Vehicle.java — Domain entities for vehicles in the parking system

enum VehicleType {                    // enum = fixed set of constants; safer than raw strings
    MOTORCYCLE, CAR, TRUCK
}

abstract class Vehicle {              // abstract = can't create Vehicle directly; must be Car/Truck/Motorcycle
    private String licensePlate;      // private = only this class can access; encapsulates data
    private VehicleType type;         // private = hidden from outside; must use getter to read

    public Vehicle(String licensePlate, VehicleType type) {
        this.licensePlate = licensePlate;
        this.type = type;
    }

    public String getLicensePlate() { return licensePlate; }
    public VehicleType getType() { return type; }

    public abstract SpotSize getRequiredSpotSize(); // abstract = each subclass MUST implement this differently

    @Override                           // tells compiler: I'm intentionally replacing parent's method
    public String toString() {
        return type + " (" + licensePlate + ")";
    }
}

class Motorcycle extends Vehicle {    // extends = Motorcycle IS-A Vehicle; inherits its fields/methods
    public Motorcycle(String licensePlate) {
        super(licensePlate, VehicleType.MOTORCYCLE);
    }

    @Override                           // tells compiler: I'm intentionally replacing parent's method
    public SpotSize getRequiredSpotSize() {
        return SpotSize.SMALL;
    }
}

class Car extends Vehicle {           // extends = Car IS-A Vehicle; inherits its fields/methods
    public Car(String licensePlate) {
        super(licensePlate, VehicleType.CAR);
    }

    @Override
    public SpotSize getRequiredSpotSize() {
        return SpotSize.MEDIUM;
    }
}

class Truck extends Vehicle {          // extends = Truck IS-A Vehicle; inherits its fields/methods
    public Truck(String licensePlate) {
        super(licensePlate, VehicleType.TRUCK);
    }

    @Override
    public SpotSize getRequiredSpotSize() {
        return SpotSize.LARGE;
    }
}

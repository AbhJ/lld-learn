/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Vehicle.java — Domain entities for vehicles (same as naive, models are already clean)

enum VehicleType {                    // enum = fixed set of constants; safer than raw strings
    MOTORCYCLE, CAR, TRUCK
}

abstract class Vehicle {              // abstract = can't create Vehicle directly; must subclass
    private final String licensePlate; // final = set once in constructor, never changes after
    private final VehicleType type;   // final = immutable after creation; safe to share

    public Vehicle(String licensePlate, VehicleType type) {
        if (licensePlate == null || licensePlate.isBlank()) {
            throw new IllegalArgumentException("License plate cannot be null or empty");
        }
        this.licensePlate = licensePlate;
        this.type = type;
    }

    public String getLicensePlate() { return licensePlate; }
    public VehicleType getType() { return type; }

    public abstract SpotSize getRequiredSpotSize(); // abstract = each subclass MUST implement this

    @Override                           // tells compiler: I'm intentionally replacing parent's method
    public String toString() {
        return type + " (" + licensePlate + ")";
    }
}

class Motorcycle extends Vehicle {    // extends = Motorcycle IS-A Vehicle; inherits fields/methods
    public Motorcycle(String licensePlate) {
        super(licensePlate, VehicleType.MOTORCYCLE);
    }

    @Override
    public SpotSize getRequiredSpotSize() {
        return SpotSize.SMALL;
    }
}

class Car extends Vehicle {           // extends = Car IS-A Vehicle; inherits fields/methods
    public Car(String licensePlate) {
        super(licensePlate, VehicleType.CAR);
    }

    @Override
    public SpotSize getRequiredSpotSize() {
        return SpotSize.MEDIUM;
    }
}

class Truck extends Vehicle {          // extends = Truck IS-A Vehicle; inherits fields/methods
    public Truck(String licensePlate) {
        super(licensePlate, VehicleType.TRUCK);
    }

    @Override
    public SpotSize getRequiredSpotSize() {
        return SpotSize.LARGE;
    }
}

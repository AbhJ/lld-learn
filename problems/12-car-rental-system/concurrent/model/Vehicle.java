/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Vehicle.java — Vehicle with type classification for fleet management

enum VehicleType { SEDAN, SUV, TRUCK, VAN, COMPACT } // enum = fixed set; used as ConcurrentHashMap key

class Vehicle {
    private final String vehicleId;  // final = immutable identity; safe to read from any thread
    private final String name;       // final = never changes after construction
    private final VehicleType type;  // final = used as map key for fleet lookup
    private final double dailyRate;  // final = price fixed at creation; thread-safe

    public Vehicle(String vehicleId, String name, VehicleType type, double dailyRate) {
        this.vehicleId = vehicleId;
        this.name = name;
        this.type = type;
        this.dailyRate = dailyRate;
    }

    public String getVehicleId() { return vehicleId; }
    public String getName() { return name; }
    public VehicleType getType() { return type; }
    public double getDailyRate() { return dailyRate; }

    @Override
    public String toString() {
        return vehicleId + " (" + name + ", " + type + ")";
    }
}

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/ParkingSpot.java — Lock-free parking spot using AtomicReference instead of synchronized

import java.util.concurrent.atomic.AtomicReference;

enum SpotSize {                       // enum = fixed set of constants; type-safe spot categories
    SMALL, MEDIUM, LARGE
}

class ParkingSpot {
    private final String spotId;      // final = immutable after construction; no sync needed
    private final SpotSize size;      // final = never changes; safe to read without locking
    private final int level;          // final = set once in constructor
    private final AtomicReference<Vehicle> parkedVehicle = new AtomicReference<>(null); // AtomicReference = lock-free CAS instead of synchronized

    public ParkingSpot(String spotId, SpotSize size, int level) {
        this.spotId = spotId;
        this.size = size;
        this.level = level;
    }

    // CAS-based park — lock-free, thread-safe
    public boolean park(Vehicle vehicle) {
        if (vehicle.getRequiredSpotSize() != size) return false;
        return parkedVehicle.compareAndSet(null, vehicle);
    }

    // Atomic unpark — returns the vehicle that was parked (or null if already empty)
    public Vehicle unpark() {
        return parkedVehicle.getAndSet(null);
    }

    public boolean isAvailable() {
        return parkedVehicle.get() == null;
    }

    public SpotSize getSize() { return size; }
    public String getSpotId() { return spotId; }
    public int getLevel() { return level; }
    public Vehicle getParkedVehicle() { return parkedVehicle.get(); }

    @Override
    public String toString() {
        return spotId + " (" + size + ", Level " + level + ")";
    }
}

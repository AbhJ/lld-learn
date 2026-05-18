/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/ParkingSpot.java — Thread-safe parking spot using AtomicReference for CAS parking

import java.util.concurrent.atomic.AtomicReference;

enum SpotSize {
    SMALL, MEDIUM, LARGE
}

class ParkingSpot {
    private final String spotId;                                                       // immutable identity — safe publication to all threads
    private final SpotSize size;                                                        // never changes after construction — no sync needed
    private final int level;                                                            // set once in ctor — final guarantees visibility
    private final AtomicReference<Vehicle> parkedVehicle = new AtomicReference<>(null); // multiple writers → need CAS for lock-free park/unpark

    public ParkingSpot(String spotId, SpotSize size, int level) {
        this.spotId = spotId;
        this.size = size;
        this.level = level;
    }

    /**
     * CAS-based park — lock-free, thread-safe.
     * Only one thread can successfully park in this spot even if multiple
     * threads attempt simultaneously. compareAndSet atomically checks that
     * the spot is null (empty) and sets the vehicle in one operation.
     */
    public boolean park(Vehicle vehicle) {
        if (vehicle.getRequiredSpotSize() != size) return false;
        return parkedVehicle.compareAndSet(null, vehicle);
    }

    /**
     * Atomic unpark — returns the vehicle that was parked (or null if already empty).
     */
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

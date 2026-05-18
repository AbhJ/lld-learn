/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/ParkingSpot.java — Represents a single parking spot with size and availability

enum SpotSize {                       // enum = fixed set of constants; only these sizes exist
    SMALL, MEDIUM, LARGE
}

class ParkingSpot {
    private String spotId;            // private = only ParkingSpot can read/write this
    private SpotSize size;            // private = encapsulated; external code uses getSize()
    private int level;                // private = hidden detail; accessed via getLevel()
    private Vehicle parkedVehicle;    // private = only park()/unpark() can modify this
    private boolean available;        // private = controlled by park()/unpark() logic

    public ParkingSpot(String spotId, SpotSize size, int level) {
        this.spotId = spotId;
        this.size = size;
        this.level = level;
        this.available = true;
        this.parkedVehicle = null;
    }

    public synchronized boolean park(Vehicle vehicle) { // synchronized = only one thread can enter at a time
        if (!available) return false;
        if (vehicle.getRequiredSpotSize() != size) return false;
        this.parkedVehicle = vehicle;
        this.available = false;
        return true;
    }

    public synchronized Vehicle unpark() { // synchronized = prevents concurrent park+unpark conflicts
        Vehicle vehicle = this.parkedVehicle;
        this.parkedVehicle = null;
        this.available = true;
        return vehicle;
    }

    public boolean isAvailable() { return available; }
    public SpotSize getSize() { return size; }
    public String getSpotId() { return spotId; }
    public int getLevel() { return level; }
    public Vehicle getParkedVehicle() { return parkedVehicle; }

    @Override
    public String toString() {
        return spotId + " (" + size + ", Level " + level + ")";
    }
}

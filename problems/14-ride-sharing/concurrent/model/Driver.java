/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Driver.java — Driver with atomic status for CAS-based assignment

import java.util.concurrent.atomic.AtomicReference;

enum DriverStatus { AVAILABLE, ASSIGNED, ON_TRIP, OFFLINE } // enum = fixed driver states for CAS transitions

class Driver {
    private final String driverId;   // final = immutable identity; safe to read from any thread
    private final String name;       // final = never changes; safe publication guaranteed
    private final AtomicReference<DriverStatus> status = new AtomicReference<>(DriverStatus.AVAILABLE); // AtomicReference = CAS-based state transitions; prevents double-assignment
    private volatile double latitude;  // volatile = location visible to all threads immediately
    private volatile double longitude; // volatile = location visible to all threads immediately

    public Driver(String driverId, String name, double latitude, double longitude) {
        this.driverId = driverId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * CAS-based assignment — only ONE rider can claim this driver.
     * Atomically transitions from AVAILABLE to ASSIGNED.
     */
    public boolean tryAssign() {
        return status.compareAndSet(DriverStatus.AVAILABLE, DriverStatus.ASSIGNED);
    }

    public boolean startTrip() {
        return status.compareAndSet(DriverStatus.ASSIGNED, DriverStatus.ON_TRIP);
    }

    public boolean completeTrip() {
        return status.compareAndSet(DriverStatus.ON_TRIP, DriverStatus.AVAILABLE);
    }

    public void release() {
        status.set(DriverStatus.AVAILABLE);
    }

    public String getDriverId() { return driverId; }
    public String getName() { return name; }
    public DriverStatus getStatus() { return status.get(); }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public void setLocation(double lat, double lng) { latitude = lat; longitude = lng; }

    public boolean isAvailable() { return status.get() == DriverStatus.AVAILABLE; }

    @Override
    public String toString() {
        return driverId + " (" + name + ") [" + status.get() + "]";
    }
}

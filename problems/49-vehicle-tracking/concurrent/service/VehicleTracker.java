/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/VehicleTracker.java — AtomicReference for latest position, CAS-based updates

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class VehicleTracker {
    private final String vehicleId;                         // final = ID immutable; safe publication
    private final AtomicReference<Location> latestLocation; // AtomicReference = CAS-based location swap; always latest
    private final AtomicInteger updateCount;                // AtomicInteger = thread-safe success counter
    private final AtomicInteger staleRejects;               // AtomicInteger = counts rejected out-of-order updates
    private final List<String> alerts;                      // CopyOnWriteArrayList = thread-safe alert log

    public VehicleTracker(String vehicleId, Location initial) {
        this.vehicleId = vehicleId;
        this.latestLocation = new AtomicReference<>(initial);
        this.updateCount = new AtomicInteger(0);
        this.staleRejects = new AtomicInteger(0);
        this.alerts = new CopyOnWriteArrayList<>();
    }

    /**
     * CAS-based location update. Only accepts updates with higher sequence number
     * to never process stale locations.
     */
    public boolean updateLocation(Location newLocation) {
        while (true) {
            Location current = latestLocation.get();
            if (newLocation.getSequenceNum() <= current.getSequenceNum()) {
                staleRejects.incrementAndGet();
                return false; // Stale update — reject
            }
            if (latestLocation.compareAndSet(current, newLocation)) { // CAS = atomic swap; retries if another thread won
                updateCount.incrementAndGet();
                return true;
            }
            // CAS failed — another thread updated. Re-check if still newer.
        }
    }

    /**
     * Check geofence against latest known position.
     * Always reads the latest position via AtomicReference.
     */
    public boolean checkGeofence(Geofence fence) {
        Location loc = latestLocation.get(); // Always gets latest
        boolean inside = fence.contains(loc);
        if (inside) {
            alerts.add("Vehicle " + vehicleId + " inside " + fence.getZoneId()
                    + " at " + loc + " (seq=" + loc.getSequenceNum() + ")");
        }
        return inside;
    }

    public Location getLatestLocation() { return latestLocation.get(); }
    public int getUpdateCount() { return updateCount.get(); }
    public int getStaleRejects() { return staleRejects.get(); }
    public List<String> getAlerts() { return alerts; }
    public String getVehicleId() { return vehicleId; }
}

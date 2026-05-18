/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Geofence.java — Circular geofence zone

public class Geofence {
    private final String zoneId;    // final = immutable; safe to read from any thread
    private final double centerLat; // final = center latitude never changes; no sync needed
    private final double centerLon; // final = center longitude never changes; no sync needed
    private final double radius;    // final = radius fixed at creation; thread-safe by design

    public Geofence(String zoneId, double centerLat, double centerLon, double radius) {
        this.zoneId = zoneId;
        this.centerLat = centerLat;
        this.centerLon = centerLon;
        this.radius = radius;
    }

    public boolean contains(Location loc) {
        double dLat = loc.getLatitude() - centerLat;
        double dLon = loc.getLongitude() - centerLon;
        return Math.sqrt(dLat * dLat + dLon * dLon) <= radius;
    }

    public String getZoneId() { return zoneId; }

    @Override
    public String toString() { return zoneId + " center=(" + centerLat + "," + centerLon + ") r=" + radius; }
}

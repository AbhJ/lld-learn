/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/GeoFence.java — Defines a geographic boundary for alerting
public class GeoFence {
    private String id;              // private = geofence ID encapsulated
    private String name;            // private = display name of this zone
    private Location center;        // private = center point of circular fence
    private double radiusMeters;    // private = how far from center the fence extends

    public GeoFence(String id, String name, Location center, double radiusMeters) {
        this.id = id; this.name = name; this.center = center; this.radiusMeters = radiusMeters;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Location getCenter() { return center; }
    public double getRadiusMeters() { return radiusMeters; }

    // O(n) — checks distance to center for every geofence check
    public boolean contains(Location location) {
        return center.distanceTo(location) <= radiusMeters;
    }

    @Override public String toString() { return name + " (radius: " + (int)radiusMeters + "m)"; }
}

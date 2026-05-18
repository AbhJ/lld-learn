/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/GeoFence.java — Geofence with grid cell pre-computation for O(1) checks
public class GeoFence {
    private String id;              // private = geofence ID encapsulated
    private String name;            // private = display name of this zone
    private Location center;        // private = center point of circular fence
    private double radiusMeters;    // private = how far from center the fence extends
    // Pre-compute bounding box for fast spatial grid indexing
    private double minLat, maxLat, minLon, maxLon; // bounding box = O(1) rejection of distant points

    public GeoFence(String id, String name, Location center, double radiusMeters) {
        this.id = id; this.name = name; this.center = center; this.radiusMeters = radiusMeters;
        computeBoundingBox();
    }

    private void computeBoundingBox() {
        // WHY: Pre-computed bounding box enables O(1) rejection of distant locations
        double latDelta = radiusMeters / 111320.0;
        double lonDelta = radiusMeters / (111320.0 * Math.cos(Math.toRadians(center.getLatitude())));
        minLat = center.getLatitude() - latDelta;
        maxLat = center.getLatitude() + latDelta;
        minLon = center.getLongitude() - lonDelta;
        maxLon = center.getLongitude() + lonDelta;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getRadiusMeters() { return radiusMeters; }

    // WHY: Fast bounding box check first (O(1)), expensive distance calc only if inside box
    public boolean contains(Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        if (lat < minLat || lat > maxLat || lon < minLon || lon > maxLon) return false;
        return center.distanceTo(location) <= radiusMeters;
    }

    public double getMinLat() { return minLat; }
    public double getMaxLat() { return maxLat; }
    public double getMinLon() { return minLon; }
    public double getMaxLon() { return maxLon; }

    @Override public String toString() { return name + " (radius: " + (int)radiusMeters + "m)"; }
}

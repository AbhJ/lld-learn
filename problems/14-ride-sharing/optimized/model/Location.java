/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Location.java — Geographic coordinate value object with distance calculation

public class Location {
    private double latitude;        // GPS coordinate; input to grid cell key calculation
    private double longitude;       // GPS coordinate; input to grid cell key calculation
    private String name;            // human-readable location name for display

    public Location(double latitude, double longitude, String name) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
    }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getName() { return name; }

    public double distanceTo(Location other) {
        // Simplified distance calculation (km units for demo)
        double dx = (this.latitude - other.latitude) * 111; // rough km per degree
        double dy = (this.longitude - other.longitude) * 111 * Math.cos(Math.toRadians(latitude));
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public String toString() { return name; }
}

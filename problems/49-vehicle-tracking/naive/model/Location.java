/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Location.java — Represents a geographic coordinate with timestamp
import java.time.LocalDateTime;

public class Location {
    private double latitude;        // private = GPS latitude coordinate
    private double longitude;       // private = GPS longitude coordinate
    private LocalDateTime timestamp; // private = when this reading was taken

    public Location(double latitude, double longitude) {
        this.latitude = latitude; this.longitude = longitude; this.timestamp = LocalDateTime.now();
    }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public double distanceTo(Location other) {
        double R = 6371000;
        double lat1 = Math.toRadians(latitude); double lat2 = Math.toRadians(other.latitude);
        double dLat = Math.toRadians(other.latitude - latitude);
        double dLon = Math.toRadians(other.longitude - longitude);
        double a = Math.sin(dLat/2)*Math.sin(dLat/2) + Math.cos(lat1)*Math.cos(lat2)*Math.sin(dLon/2)*Math.sin(dLon/2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    }

    @Override public String toString() { return String.format("(%.4f, %.4f)", latitude, longitude); }
}

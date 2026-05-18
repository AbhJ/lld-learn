/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Location.java — Immutable GPS location with timestamp

public class Location {
    private final double latitude;  // final = immutable; safe to share across threads without sync
    private final double longitude; // final = immutable; no thread can corrupt this value
    private final long timestamp;   // final = when reading was taken; immutable
    private final int sequenceNum;  // final = ordering key; used for CAS stale-detection

    public Location(double latitude, double longitude, long timestamp, int sequenceNum) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.sequenceNum = sequenceNum;
    }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public long getTimestamp() { return timestamp; }
    public int getSequenceNum() { return sequenceNum; }

    public double distanceTo(Location other) {
        double dLat = this.latitude - other.latitude;
        double dLon = this.longitude - other.longitude;
        return Math.sqrt(dLat * dLat + dLon * dLon);
    }

    @Override
    public String toString() {
        return String.format("(%,.4f, %,.4f) seq=%d", latitude, longitude, sequenceNum);
    }
}

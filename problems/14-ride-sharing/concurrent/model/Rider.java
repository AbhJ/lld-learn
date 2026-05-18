/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Rider.java — A rider requesting a trip

class Rider {
    private final String riderId;    // final = immutable; safe to pass between threads
    private final String name;       // final = never changes after construction
    private final double latitude;   // final = pickup location fixed at creation
    private final double longitude;  // final = pickup location fixed at creation

    public Rider(String riderId, String name, double latitude, double longitude) {
        this.riderId = riderId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getRiderId() { return riderId; }
    public String getName() { return name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    @Override
    public String toString() {
        return riderId + " (" + name + ")";
    }
}

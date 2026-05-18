/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Trip.java — A matched trip between rider and driver

import java.util.concurrent.atomic.AtomicInteger;

enum TripStatus { MATCHED, IN_PROGRESS, COMPLETED, CANCELLED } // enum = trip lifecycle states

class Trip {
    private static final AtomicInteger counter = new AtomicInteger(0); // AtomicInteger = thread-safe ID generator
    private final String tripId;     // final = set once; safe publication to other threads
    private final Rider rider;       // final = immutable reference; safe to read anywhere
    private final Driver driver;     // final = immutable reference; safe to read anywhere
    private volatile TripStatus status; // volatile = status changes visible to all threads immediately
    private final long matchedAt;    // final = timestamp frozen at creation

    public Trip(Rider rider, Driver driver) {
        this.tripId = "TRIP-" + counter.incrementAndGet();
        this.rider = rider;
        this.driver = driver;
        this.status = TripStatus.MATCHED;
        this.matchedAt = System.currentTimeMillis();
    }

    public String getTripId() { return tripId; }
    public Rider getRider() { return rider; }
    public Driver getDriver() { return driver; }
    public TripStatus getStatus() { return status; }
    public void setStatus(TripStatus status) { this.status = status; }

    @Override
    public String toString() {
        return tripId + " [" + rider.getName() + " -> " + driver.getName() + " | " + status + "]";
    }

    public static void resetCounter() { counter.set(0); }
}

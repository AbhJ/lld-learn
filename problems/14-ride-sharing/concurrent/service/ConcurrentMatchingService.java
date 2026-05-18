/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/ConcurrentMatchingService.java — AtomicReference per driver (CAS from AVAILABLE to ASSIGNED)

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe ride matching service.
 *
 * Race condition solved: When 20 riders request rides and only 5 drivers
 * are available, each driver's AtomicReference status ensures only ONE
 * rider can claim each driver via CAS (compareAndSet).
 */
class ConcurrentMatchingService {
    private final CopyOnWriteArrayList<Driver> drivers = new CopyOnWriteArrayList<>(); // CopyOnWriteArrayList = safe iteration while adding drivers
    private final ConcurrentLinkedQueue<Rider> waitQueue = new ConcurrentLinkedQueue<>(); // ConcurrentLinkedQueue = lock-free FIFO for waiting riders
    private final ConcurrentHashMap<String, Trip> activeTrips = new ConcurrentHashMap<>(); // ConcurrentHashMap = thread-safe trip registry
    private final AtomicInteger matchedCount = new AtomicInteger(0);    // AtomicInteger = lock-free counter via CAS
    private final AtomicInteger requeuedCount = new AtomicInteger(0);   // AtomicInteger = lock-free counter via CAS
    private final AtomicInteger failedMatchCount = new AtomicInteger(0); // AtomicInteger = lock-free counter via CAS

    public void registerDriver(Driver driver) {
        drivers.add(driver);
    }

    /**
     * Request a ride for the given rider.
     * Scans available drivers and attempts CAS assignment.
     * If no driver available, rider goes to wait queue.
     */
    public Trip requestRide(Rider rider) {
        // Try to find and claim an available driver
        for (Driver driver : drivers) {
            if (driver.tryAssign()) {
                // CAS succeeded — this rider got the driver!
                Trip trip = new Trip(rider, driver);
                activeTrips.put(trip.getTripId(), trip);
                matchedCount.incrementAndGet();
                return trip;
            }
            // CAS failed — driver was taken by another rider, try next
        }

        // No driver available — add to wait queue
        waitQueue.offer(rider);
        requeuedCount.incrementAndGet();
        return null;
    }

    /**
     * Complete a trip and release the driver back to the pool.
     * Also attempts to match a waiting rider.
     */
    public void completeTrip(String tripId) {
        Trip trip = activeTrips.remove(tripId);
        if (trip == null) return;

        trip.setStatus(TripStatus.COMPLETED);
        Driver driver = trip.getDriver();
        driver.completeTrip();

        // Try to match a waiting rider
        Rider waitingRider = waitQueue.poll();
        if (waitingRider != null) {
            if (driver.tryAssign()) {
                Trip newTrip = new Trip(waitingRider, driver);
                activeTrips.put(newTrip.getTripId(), newTrip);
                matchedCount.incrementAndGet();
            } else {
                // Driver taken by another thread — re-queue rider
                waitQueue.offer(waitingRider);
            }
        }
    }

    public int getMatchedCount() { return matchedCount.get(); }
    public int getRequeuedCount() { return requeuedCount.get(); }
    public int getWaitQueueSize() { return waitQueue.size(); }
    public int getActiveTripsCount() { return activeTrips.size(); }
    public List<Driver> getDrivers() { return Collections.unmodifiableList(drivers); }
}

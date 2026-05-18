/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Trip.java — Ride trip with pickup/dropoff, driver, fare, and state lifecycle

public class Trip {
    private String tripId;          // private = encapsulated unique trip ID
    private Rider rider;            // private = who requested the ride
    private Driver driver;          // private = matched driver; null until assigned
    private Location pickup;        // private = where rider is picked up
    private Location dropoff;       // private = where rider wants to go
    private TripState state;        // private = lifecycle state (enum)
    private double fare;            // private = computed fare; set on completion
    private double distance;        // private = computed ride distance
    private static int counter = 0; // static = shared counter for unique trip IDs

    public Trip(Rider rider, Location pickup, Location dropoff) {
        this.tripId = "TRIP-" + (++counter);
        this.rider = rider;
        this.pickup = pickup;
        this.dropoff = dropoff;
        this.state = TripState.REQUESTED;
        this.distance = pickup.distanceTo(dropoff);
    }

    public boolean transition(TripState newState) {
        if (state.canTransitionTo(newState)) {
            state = newState;
            rider.onTripUpdate(this);
            if (driver != null) driver.onTripUpdate(this);
            return true;
        }
        System.out.println("Invalid trip transition: " + state + " -> " + newState);
        return false;
    }

    public void matchDriver(Driver driver) {
        this.driver = driver;
        driver.setStatus(Driver.Status.ON_TRIP);
        transition(TripState.MATCHED);
    }

    public void startTrip() {
        transition(TripState.IN_PROGRESS);
    }

    public void completeTrip(double fare) {
        this.fare = fare;
        driver.setStatus(Driver.Status.AVAILABLE);
        driver.setCurrentLocation(dropoff);
        transition(TripState.COMPLETED);
    }

    public void cancelTrip() {
        if (driver != null) {
            driver.setStatus(Driver.Status.AVAILABLE);
        }
        transition(TripState.CANCELLED);
    }

    public String getTripId() { return tripId; }
    public Rider getRider() { return rider; }
    public Driver getDriver() { return driver; }
    public Location getPickup() { return pickup; }
    public Location getDropoff() { return dropoff; }
    public TripState getState() { return state; }
    public double getFare() { return fare; }
    public double getDistance() { return distance; }

    @Override
    public String toString() {
        return String.format("Trip[%s] %s -> %s, %.1f km, State: %s, Fare: $%.2f",
                tripId, pickup, dropoff, distance, state, fare);
    }
}

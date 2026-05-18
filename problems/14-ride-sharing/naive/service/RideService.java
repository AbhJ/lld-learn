/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/RideService.java — Facade orchestrating ride requests, matching, and trip lifecycle

import java.util.*;

public class RideService {
    private List<Driver> drivers;                  // private = registered driver pool
    private List<Trip> trips;                      // private = all trips in the system
    private MatchingStrategy matchingStrategy;     // private = strategy pattern for driver matching
    private PricingStrategy pricingStrategy;       // private = strategy pattern for fare calculation
    private PaymentProcessor paymentProcessor;     // private = proxy wrapping real payment gateway

    public RideService() {
        this.drivers = new ArrayList<>();
        this.trips = new ArrayList<>();
        this.matchingStrategy = new NearestDriverStrategy();
        this.pricingStrategy = new BasePricing();
        this.paymentProcessor = new PaymentProxy(new RealPaymentProcessor());
    }

    public void registerDriver(Driver driver) { drivers.add(driver); }

    public void setMatchingStrategy(MatchingStrategy strategy) { this.matchingStrategy = strategy; }
    public void setPricingStrategy(PricingStrategy strategy) { this.pricingStrategy = strategy; }

    public double estimateFare(Location pickup, Location dropoff, String vehicleType) {
        double distance = pickup.distanceTo(dropoff);
        return pricingStrategy.calculateFare(distance, vehicleType);
    }

    public Trip requestRide(Rider rider, Location pickup, Location dropoff) {
        Trip trip = new Trip(rider, pickup, dropoff);
        trips.add(trip);

        // Find a driver
        Driver driver = matchingStrategy.findDriver(drivers, pickup);
        if (driver == null) {
            System.out.println("No drivers available. Please try again.");
            trip.cancelTrip();
            return trip;
        }

        double distance = driver.distanceTo(pickup);
        System.out.printf("Driver matched: %s (%.1f stars, %.1f km away) [Strategy: %s]%n",
                driver.getName(), driver.getAverageRating(), distance, matchingStrategy.getName());
        trip.matchDriver(driver);
        return trip;
    }

    public void startTrip(Trip trip) {
        trip.startTrip();
    }

    public Payment completeTrip(Trip trip, Payment.Method paymentMethod) {
        double fare = pricingStrategy.calculateFare(trip.getDistance(), trip.getDriver().getVehicleType());
        trip.completeTrip(fare);

        Payment payment = new Payment(trip.getTripId(), fare, paymentMethod);
        boolean ok = paymentProcessor.process(fare, trip.getRider().getName());
        payment.markStatus(ok);
        return payment;
    }

    public void cancelTrip(Trip trip) {
        trip.cancelTrip();
    }

    public void rateDriver(Driver driver, double rating) {
        driver.addRating(rating);
    }

    public void rateRider(Rider rider, double rating) {
        rider.addRating(rating);
    }

    // Surge pricing control
    public void activateSurge(double multiplier) {
        this.pricingStrategy = new SurgePricing(new BasePricing(), multiplier);
        System.out.printf("Surge pricing activated: %.1fx%n", multiplier);
    }

    public void deactivateSurge() {
        this.pricingStrategy = new BasePricing();
        System.out.println("Surge pricing deactivated.");
    }

    public List<Driver> getAvailableDrivers() {
        List<Driver> available = new ArrayList<>();
        for (Driver d : drivers) {
            if (d.isAvailable()) available.add(d);
        }
        return available;
    }

    public List<Trip> getTrips() { return Collections.unmodifiableList(trips); }
}

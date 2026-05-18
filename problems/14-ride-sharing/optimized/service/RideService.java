/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/RideService.java — Ride service using geospatial grid for O(1) driver cell lookup

import java.util.*;

public class RideService {
    private List<Driver> drivers;              // ArrayList = O(1) append; passed to grid strategy
    private List<Trip> trips;                  // ArrayList = O(1) append for trip history
    private MatchingStrategy matchingStrategy; // Default: GeospatialGridStrategy for O(1) cell lookup
    private PricingStrategy pricingStrategy;   // Swappable pricing; supports surge decorator
    private PaymentProcessor paymentProcessor; // Proxy wrapping real payment gateway

    public RideService() {
        this.drivers = new ArrayList<>();
        this.trips = new ArrayList<>();
        this.matchingStrategy = new GeospatialGridStrategy();
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

    public void startTrip(Trip trip) { trip.startTrip(); }

    public Payment completeTrip(Trip trip, Payment.Method paymentMethod) {
        double fare = pricingStrategy.calculateFare(trip.getDistance(), trip.getDriver().getVehicleType());
        trip.completeTrip(fare);
        Payment payment = new Payment(trip.getTripId(), fare, paymentMethod);
        boolean ok = paymentProcessor.process(fare, trip.getRider().getName());
        payment.markStatus(ok);
        return payment;
    }

    public void cancelTrip(Trip trip) { trip.cancelTrip(); }
    public void rateDriver(Driver driver, double rating) { driver.addRating(rating); }
    public void rateRider(Rider rider, double rating) { rider.addRating(rating); }

    public void activateSurge(double multiplier) {
        this.pricingStrategy = new SurgePricing(new BasePricing(), multiplier);
        System.out.printf("Surge pricing activated: %.1fx%n", multiplier);
    }

    public void deactivateSurge() {
        this.pricingStrategy = new BasePricing();
        System.out.println("Surge pricing deactivated.");
    }

    public List<Trip> getTrips() { return Collections.unmodifiableList(trips); }
}

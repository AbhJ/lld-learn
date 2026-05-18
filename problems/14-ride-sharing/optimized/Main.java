/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating optimized ride sharing with geospatial grid matching

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Ride Sharing System (Optimized) ===\n");

        RideService service = new RideService();

        Location downtown = new Location(40.7128, -74.0060, "Downtown");
        Location airport = new Location(40.6413, -73.7781, "Airport");
        Location midtown = new Location(40.7549, -73.9840, "Midtown");
        Location brooklyn = new Location(40.6782, -73.9442, "Brooklyn");

        Driver d1 = new Driver("D-1", "Bob", "Economy", "ABC-111", new Location(40.7150, -74.0030, "Near Downtown"));
        Driver d2 = new Driver("D-2", "Carol", "Comfort", "DEF-222", new Location(40.7500, -73.9900, "Near Midtown"));
        Driver d3 = new Driver("D-3", "Dave", "Premium", "GHI-333", new Location(40.7000, -73.9500, "Near Brooklyn"));

        d1.addRating(4.8); d1.addRating(4.9);
        d2.addRating(4.5); d2.addRating(4.6);
        d3.addRating(4.9); d3.addRating(5.0); d3.addRating(4.8);

        service.registerDriver(d1);
        service.registerDriver(d2);
        service.registerDriver(d3);

        Rider alice = new Rider("R-1", "Alice", "555-0101", downtown);

        // --- Test 1: Geospatial Grid Matching ---
        System.out.println("--- Test 1: Geospatial Grid Matching ---");
        Trip trip1 = service.requestRide(alice, downtown, airport);
        service.startTrip(trip1);
        service.completeTrip(trip1, Payment.Method.CREDIT_CARD);
        System.out.println("Trip: " + trip1);
        System.out.println();

        // --- Test 2: Fare Estimation ---
        System.out.println("--- Test 2: Fare Estimation ---");
        double fare = service.estimateFare(downtown, airport, "Economy");
        System.out.printf("Downtown -> Airport (Economy): $%.2f%n", fare);
        System.out.println();

        // --- Test 3: Surge Pricing ---
        System.out.println("--- Test 3: Surge Pricing (1.8x) ---");
        service.activateSurge(1.8);
        Trip trip2 = service.requestRide(alice, downtown, midtown);
        service.startTrip(trip2);
        service.completeTrip(trip2, Payment.Method.WALLET);
        System.out.printf("Surge fare: $%.2f%n", trip2.getFare());
        service.deactivateSurge();
        System.out.println();

        // --- Test 4: Highest Rated Strategy ---
        System.out.println("--- Test 4: Highest Rated Strategy ---");
        service.setMatchingStrategy(new HighestRatedStrategy(20.0));
        Trip trip3 = service.requestRide(alice, midtown, brooklyn);
        service.startTrip(trip3);
        service.completeTrip(trip3, Payment.Method.DEBIT_CARD);
        System.out.println();

        System.out.println("=== Ride Sharing Demo Complete ===");
    }
}

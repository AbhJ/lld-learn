/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating the ride-sharing system

/*
 * VARIATIONS FREQUENTLY ASKED:
 * 1. Ride pooling (UberPool) - Match riders going same direction, route optimization
 * 2. Scheduled rides - Book ahead, driver pre-assignment, recurring commute
 * 3. Multi-stop trips - Add stops mid-trip, fare splitting per segment
 * 4. Driver incentives - Surge bonus, quest completion, peak hour bonuses
 * 5. Safety features - SOS button, ride sharing with contacts, driver verification
 *
 * See VARIATIONS.md for full solution approaches.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Ride Sharing System Demo ===\n");

        RideService service = new RideService();

        // Setup locations
        Location downtown = new Location(40.7128, -74.0060, "Downtown");
        Location airport = new Location(40.6413, -73.7781, "Airport");
        Location midtown = new Location(40.7549, -73.9840, "Midtown");
        Location brooklyn = new Location(40.6782, -73.9442, "Brooklyn");

        // Register drivers
        Driver d1 = new Driver("D-1", "Bob", "Economy", "ABC-111", new Location(40.7150, -74.0030, "Near Downtown"));
        Driver d2 = new Driver("D-2", "Carol", "Comfort", "DEF-222", new Location(40.7500, -73.9900, "Near Midtown"));
        Driver d3 = new Driver("D-3", "Dave", "Premium", "GHI-333", new Location(40.7000, -73.9500, "Near Brooklyn"));

        // Pre-set some ratings
        d1.addRating(4.8); d1.addRating(4.9);
        d2.addRating(4.5); d2.addRating(4.6);
        d3.addRating(4.9); d3.addRating(5.0); d3.addRating(4.8);

        service.registerDriver(d1);
        service.registerDriver(d2);
        service.registerDriver(d3);

        // Create riders
        Rider alice = new Rider("R-1", "Alice", "555-0101", downtown);
        Rider charlie = new Rider("R-2", "Charlie", "555-0102", midtown);

        // --- Test 1: Fare Estimation ---
        System.out.println("--- Test 1: Fare Estimation ---");
        double estFare = service.estimateFare(downtown, airport, "Economy");
        System.out.printf("Estimated fare (Downtown -> Airport, Economy): $%.2f%n", estFare);
        estFare = service.estimateFare(downtown, airport, "Premium");
        System.out.printf("Estimated fare (Downtown -> Airport, Premium): $%.2f%n", estFare);
        System.out.println();

        // --- Test 2: Request Ride (Nearest Driver) ---
        System.out.println("--- Test 2: Request Ride (Nearest Driver Strategy) ---");
        Trip trip1 = service.requestRide(alice, downtown, airport);
        System.out.println();

        // --- Test 3: Trip Lifecycle ---
        System.out.println("--- Test 3: Trip Lifecycle ---");
        service.startTrip(trip1);
        Payment payment1 = service.completeTrip(trip1, Payment.Method.CREDIT_CARD);
        System.out.println("Trip: " + trip1);
        System.out.println();

        // --- Test 4: Rate Driver ---
        System.out.println("--- Test 4: Rate Driver ---");
        service.rateDriver(d1, 5.0);
        System.out.printf("Bob's new rating: %.2f stars%n", d1.getAverageRating());
        System.out.println();

        // --- Test 5: Highest Rated Strategy ---
        System.out.println("--- Test 5: Highest Rated Strategy ---");
        service.setMatchingStrategy(new HighestRatedStrategy(20.0));
        Trip trip2 = service.requestRide(charlie, midtown, brooklyn);
        service.startTrip(trip2);
        service.completeTrip(trip2, Payment.Method.WALLET);
        System.out.println();

        // --- Test 6: Surge Pricing ---
        System.out.println("--- Test 6: Surge Pricing (1.8x) ---");
        service.activateSurge(1.8);
        double surgeFare = service.estimateFare(downtown, airport, "Economy");
        System.out.printf("Surge fare (Downtown -> Airport, Economy): $%.2f%n", surgeFare);
        System.out.printf("Normal fare would be: $%.2f%n", new BasePricing().calculateFare(downtown.distanceTo(airport), "Economy"));

        Trip trip3 = service.requestRide(alice, downtown, midtown);
        service.startTrip(trip3);
        Payment payment3 = service.completeTrip(trip3, Payment.Method.DEBIT_CARD);
        System.out.println("Surge fare paid: $" + String.format("%.2f", trip3.getFare()));
        service.deactivateSurge();
        System.out.println();

        // --- Test 7: Cancel Trip ---
        System.out.println("--- Test 7: Cancel Trip ---");
        Trip trip4 = service.requestRide(alice, downtown, airport);
        service.cancelTrip(trip4);
        System.out.println("Trip state: " + trip4.getState());
        System.out.println("Driver available again: " + trip4.getDriver().isAvailable());
        System.out.println();

        // --- Test 8: No Drivers Available ---
        System.out.println("--- Test 8: No Drivers Available ---");
        d1.setStatus(Driver.Status.OFFLINE);
        d2.setStatus(Driver.Status.OFFLINE);
        d3.setStatus(Driver.Status.OFFLINE);
        service.requestRide(alice, downtown, airport);
        System.out.println();

        System.out.println("=== Ride Sharing Demo Complete ===");
    }
}

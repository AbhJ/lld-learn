/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating optimized vehicle tracking
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Vehicle Tracking Demo (Optimized - Spatial Grid + Circular Buffer) ===");

        TrackingService service = new TrackingService(80.0);
        service.addListener(new LoggingAlertListener());
        Vehicle truck = service.registerVehicle("v1", "Truck-001", "Truck");
        Vehicle car = service.registerVehicle("v2", "Car-002", "Car");

        System.out.println("\n--- GPS Updates (distance-based dedup) ---");
        service.updateLocation("v1", new Location(37.7749, -122.4194), 0);
        service.updateLocation("v1", new Location(37.7755, -122.4160), 10);
        System.out.println(truck.getName() + " speed: " + String.format("%.1f", truck.getCurrentSpeedKmh()) + " km/h");
        System.out.println("History points (bounded): " + truck.getLocationHistory().size());

        System.out.println("\n--- Geofencing (O(1) spatial grid lookup) ---");
        service.addGeoFence("gf1", "Warehouse", new Location(37.7749, -122.4194), 500);
        service.updateLocation("v2", new Location(37.7748, -122.4195), 0);
        service.updateLocation("v2", new Location(37.7900, -122.4000), 30);

        System.out.println("\n--- Speeding Detection ---");
        service.updateLocation("v2", new Location(37.7749, -122.4194), 0);
        service.updateLocation("v2", new Location(37.7900, -122.4000), 3);

        System.out.println("\n--- All Alerts ---");
        for (Alert alert : service.getAlerts()) System.out.println("  " + alert);

        System.out.println("\n=== Vehicle Tracking Demo Complete ===");
    }
}

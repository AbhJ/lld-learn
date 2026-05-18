/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/TrackingService.java — Tracking with spatial grid and circular buffer
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrackingService {
    private Map<String, Vehicle> vehicles;                          // HashMap = O(1) vehicle lookup by ID
    private List<GeoFence> geoFences;                               // all registered geofences
    private List<Alert> alerts;                                     // all generated alerts
    private SpeedMonitor speedMonitor;                              // speed limit checker
    // Spatial grid for O(1) average geofence candidate lookup
    private SpatialGrid spatialGrid;                                // SpatialGrid = O(1) geofence lookup by cell
    private Map<String, Map<String, Boolean>> vehicleGeoFenceState; // tracks enter/exit per vehicle per fence
    private int alertCounter;                                       // generates unique alert IDs

    // Observer: fan-out for alerts (speeding, geofence enter/exit).
    private final List<AlertListener> alertListeners = new ArrayList<>();

    public TrackingService(double defaultSpeedLimit) {
        this.vehicles = new HashMap<>(); this.geoFences = new ArrayList<>();
        this.alerts = new ArrayList<>(); this.speedMonitor = new SpeedMonitor(defaultSpeedLimit);
        this.spatialGrid = new SpatialGrid(1000); // 1km cells
        this.vehicleGeoFenceState = new HashMap<>(); this.alertCounter = 0;
    }

    // === Observer plumbing ===
    public void addListener(AlertListener listener) { alertListeners.add(listener); }
    public void removeListener(AlertListener listener) { alertListeners.remove(listener); }

    public Vehicle registerVehicle(String id, String name, String type) {
        Vehicle vehicle = new Vehicle(id, name, type);
        vehicles.put(id, vehicle); vehicleGeoFenceState.put(id, new HashMap<>());
        return vehicle;
    }

    public void addGeoFence(String id, String name, Location center, double radiusMeters) {
        GeoFence fence = new GeoFence(id, name, center, radiusMeters);
        geoFences.add(fence);
        spatialGrid.addFence(fence);
        System.out.println("Geofence created: " + name);
    }

    public void updateLocation(String vehicleId, Location location, double timeDeltaSeconds) {
        Vehicle vehicle = vehicles.get(vehicleId);
        if (vehicle == null) return;
        Location previous = vehicle.getCurrentLocation();

        // WHY: Distance-based dedup rejects noise; returns false if too close
        boolean stored = vehicle.updateLocation(location);

        if (previous != null && timeDeltaSeconds > 0) {
            double speed = speedMonitor.calculateSpeed(previous, location, timeDeltaSeconds);
            vehicle.setCurrentSpeedKmh(speed);
            if (speed < 1) vehicle.setState(VehicleState.IDLE);
            else vehicle.setState(VehicleState.MOVING);
            if (speedMonitor.isExceedingLimit(speed)) {
                createAlert(vehicleId, Alert.AlertType.SPEEDING, vehicle.getName() + " speeding: " + String.format("%.0f", speed) + " km/h");
            }
        }

        // WHY: O(1) geofence check via spatial grid instead of O(n) scan
        checkGeoFences(vehicleId, location);
    }

    private void checkGeoFences(String vehicleId, Location location) {
        Vehicle vehicle = vehicles.get(vehicleId);
        Map<String, Boolean> fenceState = vehicleGeoFenceState.get(vehicleId);

        // WHY: Only check fences in the same spatial grid cell — typically O(1)
        List<GeoFence> candidates = spatialGrid.getCandidateFences(location);
        for (GeoFence fence : candidates) {
            boolean isInside = fence.contains(location);
            Boolean wasInside = fenceState.get(fence.getId());
            if (wasInside == null) { fenceState.put(fence.getId(), isInside); }
            else if (!wasInside && isInside) {
                fenceState.put(fence.getId(), true);
                createAlert(vehicleId, Alert.AlertType.GEOFENCE_ENTER, vehicle.getName() + " entered " + fence.getName());
            } else if (wasInside && !isInside) {
                fenceState.put(fence.getId(), false);
                createAlert(vehicleId, Alert.AlertType.GEOFENCE_EXIT, vehicle.getName() + " exited " + fence.getName());
            }
        }
    }

    private void createAlert(String vehicleId, Alert.AlertType type, String message) {
        Alert alert = new Alert("ALT-" + (++alertCounter), vehicleId, type, message);
        alerts.add(alert); System.out.println(alert);
        for (AlertListener l : alertListeners) l.onAlert(alert);
    }

    public void setVehicleState(String vehicleId, VehicleState state) {
        Vehicle v = vehicles.get(vehicleId);
        if (v != null) { v.setState(state); System.out.println(v.getName() + " -> " + state); }
    }

    public void setSpeedLimit(double kmh) { speedMonitor.setSpeedLimitKmh(kmh); }
    public List<Alert> getAlerts() { return alerts; }
}

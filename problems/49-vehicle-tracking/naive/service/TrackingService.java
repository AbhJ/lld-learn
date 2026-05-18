/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/TrackingService.java — Manages vehicle fleet tracking
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrackingService {
    private Map<String, Vehicle> vehicles;                          // private = vehicle registry by ID
    private List<GeoFence> geoFences;                               // private = all defined geofences
    private List<Alert> alerts;                                     // private = all generated alerts
    private SpeedMonitor speedMonitor;                              // private = speed limit checker
    private Map<String, Map<String, Boolean>> vehicleGeoFenceState; // private = tracks enter/exit per vehicle per fence
    private int alertCounter;                                       // private = generates unique alert IDs

    // Observer: fan-out for alerts (speeding, geofence enter/exit).
    private final List<AlertListener> alertListeners = new ArrayList<>();

    public TrackingService(double defaultSpeedLimit) {
        this.vehicles = new HashMap<>(); this.geoFences = new ArrayList<>();
        this.alerts = new ArrayList<>(); this.speedMonitor = new SpeedMonitor(defaultSpeedLimit);
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
        geoFences.add(new GeoFence(id, name, center, radiusMeters));
        System.out.println("Geofence created: " + name);
    }

    public void updateLocation(String vehicleId, Location location, double timeDeltaSeconds) {
        Vehicle vehicle = vehicles.get(vehicleId);
        if (vehicle == null) return;
        Location previous = vehicle.getCurrentLocation();
        vehicle.updateLocation(location);

        if (previous != null && timeDeltaSeconds > 0) {
            double speed = speedMonitor.calculateSpeed(previous, location, timeDeltaSeconds);
            vehicle.setCurrentSpeedKmh(speed);
            if (speed < 1) vehicle.setState(VehicleState.IDLE);
            else vehicle.setState(VehicleState.MOVING);
            if (speedMonitor.isExceedingLimit(speed)) {
                createAlert(vehicleId, Alert.AlertType.SPEEDING, vehicle.getName() + " speeding: " + String.format("%.0f", speed) + " km/h");
            }
        }
        // O(n) geofence check - checks ALL geofences
        checkGeoFences(vehicleId, location);
    }

    private void checkGeoFences(String vehicleId, Location location) {
        Vehicle vehicle = vehicles.get(vehicleId);
        Map<String, Boolean> fenceState = vehicleGeoFenceState.get(vehicleId);
        for (GeoFence fence : geoFences) {
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

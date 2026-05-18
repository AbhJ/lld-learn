/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/RentalSystem.java — Optimized rental system with HashMap<VehicleType, PriorityQueue> for fast lookup

import java.util.*;

public class RentalSystem {
    private List<Vehicle> vehicles;          // ArrayList = O(1) append for full fleet registry
    private List<Reservation> reservations;  // ArrayList = O(1) append for reservation history
    private Map<String, List<Customer>> waitlist; // HashMap = O(1) lookup of waitlist by type

    // PriorityQueue = min-heap; cheapest vehicle at top for O(1) peek, O(log n) insert/remove
    private Map<String, PriorityQueue<Vehicle>> availableByType; // HashMap+PQ = O(1) type + O(1) cheapest
    private Map<String, Map<String, PriorityQueue<Vehicle>>> availableByTypeAndLocation; // nested index

    public RentalSystem() {
        this.vehicles = new ArrayList<>();
        this.reservations = new ArrayList<>();
        this.waitlist = new HashMap<>();
        this.availableByType = new HashMap<>();
        this.availableByTypeAndLocation = new HashMap<>();
    }

    public void addVehicle(Vehicle vehicle) {
        vehicles.add(vehicle);
        if (vehicle.isAvailable()) {
            indexVehicle(vehicle);
        }
    }

    private void indexVehicle(Vehicle vehicle) {
        String type = vehicle.getType().toUpperCase();
        // Index by type (PriorityQueue sorted by daily rate - cheapest first)
        availableByType
            .computeIfAbsent(type, k -> new PriorityQueue<>(
                Comparator.comparingDouble(Vehicle::getBaseDailyRate)))
            .offer(vehicle);

        // Index by type + location
        String locId = vehicle.getCurrentLocation().getLocationId();
        availableByTypeAndLocation
            .computeIfAbsent(type, k -> new HashMap<>())
            .computeIfAbsent(locId, k -> new PriorityQueue<>(
                Comparator.comparingDouble(Vehicle::getBaseDailyRate)))
            .offer(vehicle);
    }

    private void deindexVehicle(Vehicle vehicle) {
        String type = vehicle.getType().toUpperCase();
        PriorityQueue<Vehicle> typeQueue = availableByType.get(type);
        if (typeQueue != null) typeQueue.remove(vehicle);

        String locId = vehicle.getCurrentLocation().getLocationId();
        Map<String, PriorityQueue<Vehicle>> locMap = availableByTypeAndLocation.get(type);
        if (locMap != null) {
            PriorityQueue<Vehicle> locQueue = locMap.get(locId);
            if (locQueue != null) locQueue.remove(vehicle);
        }
    }

    public Vehicle createVehicle(String type, String id, String make, String model,
                                  int year, String plate, double rate, Location loc) {
        switch (type.toUpperCase()) {
            case "CAR": return new Car(id, make, model, year, plate, rate, loc);
            case "SUV": return new SUV(id, make, model, year, plate, rate, loc);
            case "VAN": return new Van(id, make, model, year, plate, rate, loc);
            case "TRUCK": return new Truck(id, make, model, year, plate, rate, loc);
            default: throw new IllegalArgumentException("Unknown vehicle type: " + type);
        }
    }

    /**
     * O(1) type lookup + O(k) to collect results from PriorityQueue (already sorted by price).
     * Naive version does O(n) linear scan through all vehicles.
     */
    public List<Vehicle> searchAvailable(String type, Location location) {
        List<Vehicle> results = new ArrayList<>();
        String typeKey = type != null ? type.toUpperCase() : null;

        if (typeKey != null && location != null) {
            Map<String, PriorityQueue<Vehicle>> locMap = availableByTypeAndLocation.get(typeKey);
            if (locMap != null) {
                PriorityQueue<Vehicle> queue = locMap.get(location.getLocationId());
                if (queue != null) results.addAll(queue);
            }
        } else if (typeKey != null) {
            PriorityQueue<Vehicle> queue = availableByType.get(typeKey);
            if (queue != null) results.addAll(queue);
        } else {
            for (Vehicle v : vehicles) {
                if (v.isAvailable()) results.add(v);
            }
        }
        return results;
    }

    public List<Vehicle> searchAvailable(String type) {
        return searchAvailable(type, null);
    }

    /**
     * Returns the cheapest available vehicle of a given type in O(log n).
     */
    public Vehicle getCheapestAvailable(String type) {
        PriorityQueue<Vehicle> queue = availableByType.get(type.toUpperCase());
        return queue != null ? queue.peek() : null;
    }

    public Reservation makeReservation(Reservation reservation) {
        Vehicle vehicle = reservation.getVehicle();
        if (!vehicle.isAvailable()) {
            System.out.println("Vehicle is not available: " + vehicle);
            return null;
        }
        vehicle.setAvailable(false);
        deindexVehicle(vehicle);
        reservations.add(reservation);
        return reservation;
    }

    public Bill completeReservation(Reservation reservation) {
        reservation.setStatus(Reservation.Status.COMPLETED);
        Vehicle vehicle = reservation.getVehicle();
        vehicle.setAvailable(true);
        vehicle.setCurrentLocation(reservation.getDropoffLocation());
        indexVehicle(vehicle);
        notifyWaitlist(vehicle.getType(), vehicle);
        return new Bill(reservation);
    }

    public void cancelReservation(Reservation reservation) {
        reservation.setStatus(Reservation.Status.CANCELLED);
        Vehicle vehicle = reservation.getVehicle();
        vehicle.setAvailable(true);
        indexVehicle(vehicle);
        notifyWaitlist(vehicle.getType(), vehicle);
    }

    public void addToWaitlist(String vehicleType, Customer customer) {
        waitlist.computeIfAbsent(vehicleType.toUpperCase(), k -> new ArrayList<>()).add(customer);
        System.out.println(customer.getName() + " added to waitlist for " + vehicleType);
    }

    private void notifyWaitlist(String vehicleType, Vehicle vehicle) {
        List<Customer> waiting = waitlist.get(vehicleType.toUpperCase());
        if (waiting != null && !waiting.isEmpty()) {
            for (Customer c : waiting) {
                c.notify(vehicle.getMake() + " " + vehicle.getModel() + " (" + vehicleType + ") is now available!");
            }
            waiting.clear();
        }
    }

    public List<Reservation> getReservations() { return Collections.unmodifiableList(reservations); }
    public List<Vehicle> getVehicles() { return Collections.unmodifiableList(vehicles); }
}

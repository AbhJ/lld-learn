/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/RentalSystem.java — Facade orchestrating vehicle search, reservation, and billing

import java.util.*;
import java.util.stream.Collectors;

public class RentalSystem {
    private List<Vehicle> vehicles;                  // private = encapsulates fleet inventory
    private List<Reservation> reservations;          // private = encapsulates all bookings
    private Map<String, List<Customer>> waitlist;    // private = waitlist per vehicle type

    public RentalSystem() {
        this.vehicles = new ArrayList<>();
        this.reservations = new ArrayList<>();
        this.waitlist = new HashMap<>();
    }

    public void addVehicle(Vehicle vehicle) {
        vehicles.add(vehicle);
    }

    // Factory method for creating vehicles
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

    public List<Vehicle> searchAvailable(String type, Location location) {
        return vehicles.stream()
                .filter(Vehicle::isAvailable)
                .filter(v -> type == null || v.getType().equalsIgnoreCase(type))
                .filter(v -> location == null || v.getCurrentLocation().equals(location))
                .collect(Collectors.toList());
    }

    public List<Vehicle> searchAvailable(String type) {
        return searchAvailable(type, null);
    }

    public Reservation makeReservation(Reservation reservation) {
        Vehicle vehicle = reservation.getVehicle();
        if (!vehicle.isAvailable()) {
            System.out.println("Vehicle is not available: " + vehicle);
            return null;
        }
        vehicle.setAvailable(false);
        reservations.add(reservation);
        return reservation;
    }

    public Bill completeReservation(Reservation reservation) {
        reservation.setStatus(Reservation.Status.COMPLETED);
        Vehicle vehicle = reservation.getVehicle();
        vehicle.setAvailable(true);
        vehicle.setCurrentLocation(reservation.getDropoffLocation());

        // Notify waitlisted customers
        notifyWaitlist(vehicle.getType(), vehicle);

        return new Bill(reservation);
    }

    public void cancelReservation(Reservation reservation) {
        reservation.setStatus(Reservation.Status.CANCELLED);
        reservation.getVehicle().setAvailable(true);
        notifyWaitlist(reservation.getVehicle().getType(), reservation.getVehicle());
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

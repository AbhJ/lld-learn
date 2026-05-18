/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/RentalService.java — CAS-based reservation using ConcurrentLinkedQueue per type

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe rental service.
 *
 * Race condition solved: When multiple customers try to reserve the last car
 * of a type simultaneously, ConcurrentLinkedQueue.poll() is atomic — only
 * one customer gets the car, others get null (no car available).
 */
class RentalService {
    private final ConcurrentHashMap<VehicleType, ConcurrentLinkedQueue<Vehicle>> fleet = // ConcurrentHashMap = thread-safe map; no global lock
            new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<Reservation> reservations = new ConcurrentLinkedQueue<>(); // ConcurrentLinkedQueue = lock-free thread-safe queue
    private final AtomicInteger successCount = new AtomicInteger(0); // AtomicInteger = lock-free counter via CAS
    private final AtomicInteger failCount = new AtomicInteger(0);    // AtomicInteger = lock-free counter via CAS

    public void addVehicle(Vehicle vehicle) {
        fleet.computeIfAbsent(vehicle.getType(), k -> new ConcurrentLinkedQueue<>())
             .offer(vehicle);
    }

    /**
     * Attempt to reserve a vehicle of the given type.
     * poll() is atomic on ConcurrentLinkedQueue — if null, no cars available.
     * No two customers can get the same vehicle.
     */
    public Reservation reserve(String customerName, VehicleType type) {
        ConcurrentLinkedQueue<Vehicle> available = fleet.get(type);
        if (available == null) {
            failCount.incrementAndGet();
            return null;
        }

        Vehicle vehicle = available.poll(); // atomic — only one thread gets this vehicle
        if (vehicle == null) {
            failCount.incrementAndGet();
            return null; // no cars of this type available
        }

        Reservation reservation = new Reservation(customerName, vehicle);
        reservations.offer(reservation);
        successCount.incrementAndGet();
        return reservation;
    }

    /**
     * Return a vehicle back to the fleet.
     */
    public void returnVehicle(Vehicle vehicle) {
        fleet.computeIfAbsent(vehicle.getType(), k -> new ConcurrentLinkedQueue<>())
             .offer(vehicle);
    }

    public int getAvailableCount(VehicleType type) {
        ConcurrentLinkedQueue<Vehicle> q = fleet.get(type);
        return q == null ? 0 : q.size();
    }

    public int getSuccessCount() { return successCount.get(); }
    public int getFailCount() { return failCount.get(); }
    public int getTotalReservations() { return reservations.size(); }
}

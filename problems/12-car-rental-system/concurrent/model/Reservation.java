/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Reservation.java — Reservation linking a customer to a vehicle

import java.util.concurrent.atomic.AtomicLong;

class Reservation {
    private static final AtomicLong ID_GEN = new AtomicLong(1); // AtomicLong = thread-safe ID generator; no duplicates

    private final long reservationId;    // final = set once; safe publication across threads
    private final String customerName;   // final = immutable after construction; thread-safe read
    private final Vehicle vehicle;       // final = reference fixed; safe to share between threads
    private final long timestamp;        // final = creation time frozen; never changes

    public Reservation(String customerName, Vehicle vehicle) {
        this.reservationId = ID_GEN.getAndIncrement();
        this.customerName = customerName;
        this.vehicle = vehicle;
        this.timestamp = System.nanoTime();
    }

    public long getReservationId() { return reservationId; }
    public String getCustomerName() { return customerName; }
    public Vehicle getVehicle() { return vehicle; }
    public long getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "Reservation#" + reservationId + " [" + customerName + " -> " + vehicle.getName() + "]";
    }
}

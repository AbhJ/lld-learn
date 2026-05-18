/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Ticket.java — Parking session with timestamps; uses AtomicInteger for thread-safe ID generation

import java.util.concurrent.atomic.AtomicInteger;

class Ticket {
    private static final AtomicInteger counter = new AtomicInteger(0); // AtomicInteger = thread-safe ID generator without synchronized
    private final String ticketId;    // final = immutable once created
    private final Vehicle vehicle;    // final = set once; never reassigned
    private final ParkingSpot spot;   // final = set once; never reassigned
    private final long entryTime;     // final = captured at creation; never changes
    private volatile long exitTime;   // volatile = visible to all threads immediately when written

    public Ticket(Vehicle vehicle, ParkingSpot spot) {
        this.ticketId = "T-" + counter.incrementAndGet();
        this.vehicle = vehicle;
        this.spot = spot;
        this.entryTime = System.currentTimeMillis();
        this.exitTime = 0;
    }

    // Constructor for testing with custom entry time
    public Ticket(Vehicle vehicle, ParkingSpot spot, long entryTime) {
        this.ticketId = "T-" + counter.incrementAndGet();
        this.vehicle = vehicle;
        this.spot = spot;
        this.entryTime = entryTime;
        this.exitTime = 0;
    }

    public void markExit() {
        this.exitTime = System.currentTimeMillis();
    }

    public void markExit(long exitTime) {
        this.exitTime = exitTime;
    }

    public long getDurationInHours() {
        long duration = exitTime - entryTime;
        long hours = duration / (1000 * 60 * 60);
        if (duration % (1000 * 60 * 60) > 0) hours++; // Round up
        return Math.max(1, hours);
    }

    public String getTicketId() { return ticketId; }
    public Vehicle getVehicle() { return vehicle; }
    public ParkingSpot getSpot() { return spot; }
    public long getEntryTime() { return entryTime; }
    public long getExitTime() { return exitTime; }

    @Override
    public String toString() {
        return ticketId;
    }

    public static void resetCounter() { counter.set(0); }
}

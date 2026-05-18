/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Ticket.java — Parking session with thread-safe ID generation

import java.util.concurrent.atomic.AtomicInteger;

class Ticket {
    private static final AtomicInteger counter = new AtomicInteger(0); // shared ID generator across all instances — static + atomic for uniqueness
    private final String ticketId;                                     // immutable after ctor — safe to read from any thread without sync
    private final Vehicle vehicle;                                     // set once — final ensures happens-before to all readers
    private final ParkingSpot spot;                                    // set once — final ensures happens-before to all readers
    private final long entryTime;                                      // captured at creation — never changes
    private volatile long exitTime;                                    // written by exit-gate thread, read by billing — volatile sufficient

    public Ticket(Vehicle vehicle, ParkingSpot spot) {
        this.ticketId = "T-" + counter.incrementAndGet();
        this.vehicle = vehicle;
        this.spot = spot;
        this.entryTime = System.currentTimeMillis();
        this.exitTime = 0;
    }

    public void markExit() {
        this.exitTime = System.currentTimeMillis();
    }

    public long getDurationInHours() {
        long duration = exitTime - entryTime;
        long hours = duration / (1000 * 60 * 60);
        if (duration % (1000 * 60 * 60) > 0) hours++;
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

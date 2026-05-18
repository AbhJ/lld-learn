/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Ticket.java — Represents a parking session with entry/exit timestamps for billing

class Ticket {
    private static int counter = 0;   // static = shared across ALL Ticket instances; tracks next ID
    private String ticketId;          // private = only this class manages the ID
    private Vehicle vehicle;          // private = encapsulated; read via getVehicle()
    private ParkingSpot spot;         // private = encapsulated; read via getSpot()
    private long entryTime;           // private = set at creation; read via getter
    private long exitTime;            // private = set only by markExit(); read via getter

    public Ticket(Vehicle vehicle, ParkingSpot spot) {
        this.ticketId = "T-" + (++counter);
        this.vehicle = vehicle;
        this.spot = spot;
        this.entryTime = System.currentTimeMillis();
        this.exitTime = 0;
    }

    // Constructor for testing with custom entry time
    public Ticket(Vehicle vehicle, ParkingSpot spot, long entryTime) {
        this.ticketId = "T-" + (++counter);
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

    public static void resetCounter() { counter = 0; }
}

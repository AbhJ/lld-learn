/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/ParkingLot.java — Thread-safe parking with CAS-based spot assignment
// DESIGN PATTERN: Facade
//
// FACADE: Main.java talks only to this class.
// Delegates observer notification to ParkingObserver (defined in ParkingObserver.java).

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

class ParkingLot {
    private final List<ParkingLevel> levels;                            // populated at startup only — final ref, no structural mutation after init
    private final ConcurrentHashMap<String, Ticket> activeTickets;      // concurrent put/remove from many threads — CHM gives segment-level locking
    private final CopyOnWriteArrayList<ParkingObserver> observers;      // rarely written, frequently iterated — COW avoids iterator locking
    private final AtomicInteger totalParked = new AtomicInteger(0);    // multiple park threads increment — atomic avoids lost updates
    private final AtomicInteger totalRejected = new AtomicInteger(0);  // same as above — lock-free counter for stats

    public ParkingLot() {
        this.levels = new ArrayList<>();
        this.activeTickets = new ConcurrentHashMap<>();
        this.observers = new CopyOnWriteArrayList<>();
    }

    public void addLevel(int smallSpots, int mediumSpots, int largeSpots) {
        levels.add(new ParkingLevel(levels.size(), smallSpots, mediumSpots, largeSpots));
    }

    public void addObserver(ParkingObserver observer) {
        observers.add(observer);
    }

    private void notifyObservers(ParkingSpot spot, boolean available) {
        for (ParkingObserver observer : observers) {
            observer.onSpotAvailabilityChanged(spot, available);
        }
    }

    /**
     * Thread-safe parking operation.
     * ConcurrentLinkedQueue.poll() ensures each spot is handed out to exactly one thread.
     * CAS on ParkingSpot.park() provides a second safety layer.
     * ConcurrentHashMap ensures ticket storage is thread-safe.
     */
    public Ticket parkVehicle(Vehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle cannot be null");
        }
        SpotSize requiredSize = vehicle.getRequiredSpotSize();
        for (ParkingLevel level : levels) {
            ParkingSpot spot = level.findAvailableSpot(requiredSize);
            if (spot != null) {
                if (spot.park(vehicle)) {
                    Ticket ticket = new Ticket(vehicle, spot);
                    activeTickets.put(ticket.getTicketId(), ticket);
                    totalParked.incrementAndGet();
                    notifyObservers(spot, false);
                    return ticket;
                }
                // If CAS failed (another thread parked here), spot was already taken from queue
                // This shouldn't happen with our queue-based design, but is safe regardless
            }
        }
        totalRejected.incrementAndGet();
        return null; // No spot available
    }

    public double unparkVehicle(String ticketId) {
        if (ticketId == null || ticketId.isBlank()) {
            throw new IllegalArgumentException("Ticket ID cannot be null or empty");
        }
        Ticket ticket = activeTickets.remove(ticketId);
        if (ticket == null) {
            return -1;
        }
        ticket.markExit();
        ParkingSpot spot = ticket.getSpot();
        spot.unpark();

        for (ParkingLevel level : levels) {
            if (level.getLevelNumber() == spot.getLevel()) {
                level.returnSpot(spot);
                break;
            }
        }

        notifyObservers(spot, true);
        return ticket.getDurationInHours() * 2.0; // Simple $2/hour
    }

    public int getTotalParked() { return totalParked.get(); }
    public int getTotalRejected() { return totalRejected.get(); }
    public int getActiveTicketCount() { return activeTickets.size(); }

    public String getAvailabilitySummary() {
        StringBuilder sb = new StringBuilder();
        for (ParkingLevel level : levels) {
            sb.append(level.getAvailabilitySummary()).append("\n");
        }
        return sb.toString().trim();
    }
}

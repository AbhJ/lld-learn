/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/ParkingLot.java — Injectable (no singleton), uses ConcurrentHashMap for thread-safe ticket storage
// DESIGN PATTERN: Facade
//
// FACADE: Main.java talks only to this class.
// Delegates observer notification to ParkingObserver (defined in ParkingObserver.java).

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

class ParkingLot {
    private final List<ParkingLevel> levels;       // final = reference won't change after construction
    private final ConcurrentHashMap<String, Ticket> activeTickets; // ConcurrentHashMap = thread-safe O(1) put/get/remove
    private final CopyOnWriteArrayList<ParkingObserver> observers; // CopyOnWriteArrayList = safe iteration without locking
    private volatile PaymentProcessor paymentProcessor; // volatile = strategy swap visible to all threads immediately

    // No singleton — just construct it. Makes testing trivial.
    public ParkingLot() {
        this.levels = new ArrayList<>();
        this.activeTickets = new ConcurrentHashMap<>();
        this.observers = new CopyOnWriteArrayList<>();
        this.paymentProcessor = new PaymentProcessor(new HourlyPricing(2.0));
    }

    public ParkingLot(PricingStrategy strategy) {
        this.levels = new ArrayList<>();
        this.activeTickets = new ConcurrentHashMap<>();
        this.observers = new CopyOnWriteArrayList<>();
        this.paymentProcessor = new PaymentProcessor(strategy);
    }

    public void addLevel(int smallSpots, int mediumSpots, int largeSpots) {
        if (smallSpots < 0 || mediumSpots < 0 || largeSpots < 0) {
            throw new IllegalArgumentException("Spot counts cannot be negative");
        }
        levels.add(new ParkingLevel(levels.size(), smallSpots, mediumSpots, largeSpots));
    }

    public void addObserver(ParkingObserver observer) {
        if (observer == null) throw new IllegalArgumentException("Observer cannot be null");
        observers.add(observer);
    }

    public void removeObserver(ParkingObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers(ParkingSpot spot, boolean available) {
        for (ParkingObserver observer : observers) {
            observer.onSpotAvailabilityChanged(spot, available);
        }
    }

    public Ticket parkVehicle(Vehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle cannot be null");
        }
        SpotSize requiredSize = vehicle.getRequiredSpotSize();
        for (ParkingLevel level : levels) {
            ParkingSpot spot = level.findAvailableSpot(requiredSize);
            if (spot != null) {
                spot.park(vehicle);
                Ticket ticket = new Ticket(vehicle, spot);
                activeTickets.put(ticket.getTicketId(), ticket);
                notifyObservers(spot, false);
                return ticket;
            }
        }
        return null; // No spot available
    }

    public double unparkVehicle(String ticketId) {
        return unparkVehicle(ticketId, System.currentTimeMillis());
    }

    public double unparkVehicle(String ticketId, long exitTime) {
        if (ticketId == null || ticketId.isBlank()) {
            throw new IllegalArgumentException("Ticket ID cannot be null or empty");
        }
        Ticket ticket = activeTickets.get(ticketId);
        if (ticket == null) {
            System.out.println("Invalid ticket: " + ticketId);
            return -1;
        }
        ticket.markExit(exitTime);
        double fee = paymentProcessor.processPayment(ticket);
        ParkingSpot spot = ticket.getSpot();
        spot.unpark();
        activeTickets.remove(ticketId);

        // Return the spot to its level's available pool
        for (ParkingLevel level : levels) {
            if (level.getLevelNumber() == spot.getLevel()) {
                level.returnSpot(spot);
                break;
            }
        }

        notifyObservers(spot, true);
        return fee;
    }

    public void setPricingStrategy(PricingStrategy strategy) {
        if (strategy == null) throw new IllegalArgumentException("Strategy cannot be null");
        paymentProcessor.setStrategy(strategy);
    }

    public PaymentProcessor getPaymentProcessor() {
        return paymentProcessor;
    }

    public int getLevelCount() { return levels.size(); }

    public ParkingLevel getLevel(int index) {
        if (index < 0 || index >= levels.size()) {
            throw new IndexOutOfBoundsException("Level " + index + " does not exist");
        }
        return levels.get(index);
    }

    public String getAvailabilitySummary() {
        StringBuilder sb = new StringBuilder();
        for (ParkingLevel level : levels) {
            sb.append(level.getAvailabilitySummary()).append("\n");
        }
        return sb.toString().trim();
    }
}

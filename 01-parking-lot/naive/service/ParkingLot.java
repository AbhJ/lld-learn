/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/ParkingLot.java — Singleton facade that orchestrates parking operations and notifies observers
// DESIGN PATTERN: Singleton | Facade
//
// FACADE: Main.java talks only to this class.
// Delegates observer notification to ParkingObserver (defined in ParkingObserver.java).

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ParkingLot {
    private static ParkingLot instance; // static = belongs to class, not any instance; shared across all code
    private List<ParkingLevel> levels;  // private = internal state hidden from callers
    private Map<String, Ticket> activeTickets; // private = only ParkingLot manages tickets
    private List<ParkingObserver> observers;   // private = observer list managed internally
    private PaymentProcessor paymentProcessor; // private = billing logic encapsulated

    private ParkingLot() {            // private constructor = only getInstance() can create this
        levels = new ArrayList<>();
        activeTickets = new HashMap<>();
        observers = new ArrayList<>();
        paymentProcessor = new PaymentProcessor(new HourlyPricing(2.0));
    }

    public static synchronized ParkingLot getInstance() { // static+synchronized = thread-safe singleton access
        if (instance == null) {
            instance = new ParkingLot();
        }
        return instance;
    }

    public static void resetInstance() {
        instance = null;
    }

    public void addLevel(int smallSpots, int mediumSpots, int largeSpots) {
        levels.add(new ParkingLevel(levels.size(), smallSpots, mediumSpots, largeSpots));
    }

    public void addObserver(ParkingObserver observer) {
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
        notifyObservers(spot, true);
        return fee;
    }

    public void setPricingStrategy(PricingStrategy strategy) {
        paymentProcessor.setStrategy(strategy);
    }

    public PaymentProcessor getPaymentProcessor() {
        return paymentProcessor;
    }

    public int getLevelCount() { return levels.size(); }

    public ParkingLevel getLevel(int index) { return levels.get(index); }

    public String getAvailabilitySummary() {
        StringBuilder sb = new StringBuilder();
        for (ParkingLevel level : levels) {
            sb.append(level.getAvailabilitySummary()).append("\n");
        }
        return sb.toString().trim();
    }
}

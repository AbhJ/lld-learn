/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Flight.java — Scheduled flight with seat inventory
import java.util.ArrayList;
import java.util.List;

public class Flight {
    private final String flightNumber;     // final = flight number never changes
    private final String origin;           // final = departure city is fixed
    private final String destination;      // final = arrival city is fixed
    private final double basePrice;        // final = base fare set at creation
    private final List<Seat> seats = new ArrayList<>(); // private = only flight manages its seats

    public Flight(String flightNumber, String origin, String destination, double basePrice) {
        this.flightNumber = flightNumber;
        this.origin = origin;
        this.destination = destination;
        this.basePrice = basePrice;
    }

    public void addSeats(int economy, int business, int first) {
        int n = 1;
        for (int i = 0; i < first; i++) seats.add(new Seat(n++ + "F", "FIRST"));
        for (int i = 0; i < business; i++) seats.add(new Seat(n++ + "B", "BUSINESS"));
        for (int i = 0; i < economy; i++) seats.add(new Seat(n++ + "E", "ECONOMY"));
    }

    // Linear search for available seat by class
    public Seat bookSeat(String seatClass) {
        for (Seat s : seats) {
            if (s.getSeatClass().equals(seatClass) && !s.isBooked()) {
                s.book();
                return s;
            }
        }
        return null;
    }

    public int getAvailable(String seatClass) {
        int c = 0;
        for (Seat s : seats) if (s.getSeatClass().equals(seatClass) && !s.isBooked()) c++;
        return c;
    }

    public String getFlightNumber() { return flightNumber; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public double getBasePrice() { return basePrice; }
    public List<Seat> getSeats() { return seats; }
    @Override public String toString() { return flightNumber + ": " + origin + "->" + destination; }
}

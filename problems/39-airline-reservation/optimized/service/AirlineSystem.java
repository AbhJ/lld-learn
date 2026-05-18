/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/AirlineSystem.java — Reservation system using BitSet for O(1) seat operations
import java.util.ArrayList;
import java.util.List;

public class AirlineSystem {
    private final List<Flight> flights = new ArrayList<>();    // stores all registered flights
    private final List<Booking> bookings = new ArrayList<>();  // stores all bookings

    public void addFlight(Flight f) { flights.add(f); }

    public List<Flight> search(String origin, String dest) {
        List<Flight> results = new ArrayList<>();
        for (Flight f : flights) {
            if (f.getOrigin().equalsIgnoreCase(origin) && f.getDestination().equalsIgnoreCase(dest))
                results.add(f);
        }
        return results;
    }

    public Booking book(Passenger p, Flight f, String seatClass) {
        int idx = f.bookSeat(seatClass);
        if (idx < 0) {
            System.out.println("  No " + seatClass + " seats on " + f.getFlightNumber());
            return null;
        }
        double multiplier = seatClass.equals("FIRST") ? 4.0 : seatClass.equals("BUSINESS") ? 2.5 : 1.0;
        double price = f.getBasePrice() * multiplier;
        Booking b = new Booking(p, f.getFlightNumber(), idx, f.getSeatLabel(idx), price);
        bookings.add(b);
        System.out.println("  Booked: " + b);
        return b;
    }

    public void cancel(Booking b, Flight f) {
        b.cancel();
        f.releaseSeat(b.getSeatIndex());
        System.out.println("  Cancelled: " + b.getId());
    }

    public void checkIn(Booking b) {
        b.checkIn();
        System.out.println("  Checked in: " + b.getPassenger().getName());
    }
}

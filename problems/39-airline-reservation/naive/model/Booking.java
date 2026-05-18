/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Booking.java — Confirmed reservation linking passenger, flight, and seat
public class Booking {
    private static int counter = 0;        // static = shared ID counter across all bookings
    private final String id;               // final = booking ID is permanent once assigned
    private final Passenger passenger;     // final = passenger link is immutable
    private final Flight flight;           // final = flight link is immutable
    private final Seat seat;               // final = seat assignment is permanent
    private final double price;            // final = price locked at booking time
    private String state = "CONFIRMED";    // private = lifecycle state managed internally

    public Booking(Passenger p, Flight f, Seat s, double price) {
        this.id = "BK" + String.format("%04d", ++counter);
        this.passenger = p; this.flight = f; this.seat = s; this.price = price;
    }

    public void cancel() { state = "CANCELLED"; seat.release(); }
    public void checkIn() { state = "CHECKED_IN"; }

    public String getId() { return id; }
    public Passenger getPassenger() { return passenger; }
    public Flight getFlight() { return flight; }
    public Seat getSeat() { return seat; }
    public double getPrice() { return price; }
    public String getState() { return state; }
    @Override public String toString() { return id + ": " + passenger.getName() + " on " + flight.getFlightNumber() + " seat " + seat.getNumber(); }
    public static void resetCounter() { counter = 0; }
}

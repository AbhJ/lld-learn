/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Booking.java — Reservation record
public class Booking {
    private static int counter = 0;        // static = shared counter for unique booking IDs
    private final String id;               // final = booking ID permanent
    private final Passenger passenger;     // final = passenger link immutable
    private final String flightNumber;     // final = flight ref immutable
    private final int seatIndex;           // final = BitSet index for O(1) seat release
    private final String seatLabel;        // final = human-readable seat label
    private final double price;            // final = price locked at booking time
    private String state = "CONFIRMED";    // private = lifecycle managed internally

    public Booking(Passenger p, String flightNumber, int seatIndex, String seatLabel, double price) {
        this.id = "BK" + String.format("%04d", ++counter);
        this.passenger = p; this.flightNumber = flightNumber;
        this.seatIndex = seatIndex; this.seatLabel = seatLabel; this.price = price;
    }

    public void cancel() { state = "CANCELLED"; }
    public void checkIn() { state = "CHECKED_IN"; }
    public String getId() { return id; }
    public Passenger getPassenger() { return passenger; }
    public int getSeatIndex() { return seatIndex; }
    public String getSeatLabel() { return seatLabel; }
    public String getState() { return state; }
    @Override public String toString() { return id + ": " + passenger + " seat " + seatLabel + " $" + String.format("%.2f", price); }
    public static void resetCounter() { counter = 0; }
}

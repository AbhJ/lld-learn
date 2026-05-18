/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/Flight.java — Flight with BitSet-backed seat map for fast availability queries
public class Flight {
    private final String flightNumber;     // final = flight number is permanent
    private final String origin;           // final = departure city fixed
    private final String destination;      // final = arrival city fixed
    private final double basePrice;        // final = base fare set at creation
    private final SeatMap seatMap;         // SeatMap = BitSet-backed O(1) seat availability

    public Flight(String flightNumber, String origin, String destination,
                  double basePrice, int economy, int business, int first) {
        this.flightNumber = flightNumber;
        this.origin = origin;
        this.destination = destination;
        this.basePrice = basePrice;
        this.seatMap = new SeatMap(first, business, economy);
    }

    public int bookSeat(String seatClass) { return seatMap.bookFirstAvailable(seatClass); }
    public void releaseSeat(int idx) { seatMap.release(idx); }
    public int getAvailable(String seatClass) { return seatMap.getAvailableCount(seatClass); }
    public String getSeatLabel(int idx) { return seatMap.getLabel(idx); }
    public double getOccupancy() { return (double) seatMap.getBookedCount() / seatMap.getTotalSeats(); }

    public String getFlightNumber() { return flightNumber; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public double getBasePrice() { return basePrice; }
    public SeatMap getSeatMap() { return seatMap; }
    @Override public String toString() { return flightNumber + ": " + origin + "->" + destination; }
}

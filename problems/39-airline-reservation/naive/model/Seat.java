/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Seat.java — Individual seat with class and availability
public class Seat {
    private final String number;           // final = seat number is permanent (e.g., "3B")
    private final String seatClass;        // final = ECONOMY, BUSINESS, or FIRST; never changes
    private boolean booked;                // private = only this class controls booking status

    public Seat(String number, String seatClass) {
        this.number = number; this.seatClass = seatClass;
    }

    public String getNumber() { return number; }
    public String getSeatClass() { return seatClass; }
    public boolean isBooked() { return booked; }
    public void book() { booked = true; }
    public void release() { booked = false; }
    @Override public String toString() { return number + "(" + seatClass + ")" + (booked ? "[X]" : "[O]"); }
}

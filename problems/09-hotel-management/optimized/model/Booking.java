/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Booking.java — Room reservation with guest, dates, and status

import java.time.LocalDate;

class Booking {
    private static int counter = 0;
    private String bookingId;
    private Guest guest;
    private Room room;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int nights;
    private boolean checkedIn;
    private boolean checkedOut;

    public Booking(Guest guest, Room room, LocalDate checkInDate, int nights) {
        this.bookingId = "B-" + (++counter);
        this.guest = guest;
        this.room = room;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkInDate.plusDays(nights);
        this.nights = nights;
        this.checkedIn = false;
        this.checkedOut = false;
    }

    public boolean checkIn() {
        if (checkedIn) return false;
        if (!room.setState(RoomState.OCCUPIED)) return false;
        checkedIn = true;
        return true;
    }

    public boolean checkOut() {
        if (!checkedIn || checkedOut) return false;
        if (!room.setState(RoomState.AVAILABLE)) return false;
        checkedOut = true;
        return true;
    }

    public String getBookingId() { return bookingId; }
    public Guest getGuest() { return guest; }
    public Room getRoom() { return room; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public int getNights() { return nights; }
    public boolean isCheckedIn() { return checkedIn; }
    public boolean isCheckedOut() { return checkedOut; }

    @Override
    public String toString() {
        return bookingId + ": " + guest.getName() + " -> Room " + room.getRoomNumber() +
               " (" + checkInDate + " to " + checkOutDate + ", " + nights + " nights)";
    }
    public static void resetCounter() { counter = 0; }
}

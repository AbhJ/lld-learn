/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Room.java — Hotel room with ReentrantLock for booking atomicity

package model;

import java.util.concurrent.locks.ReentrantLock;

public class Room {
    private final int number;           // final = room number never changes; safe publication
    private final String type;          // final = type immutable; safe to read from any thread
    private final double pricePerNight; // final = price set once; no sync needed to read
    private final ReentrantLock bookingLock; // ReentrantLock = ensures only one thread books at a time
    private volatile boolean booked;    // volatile = booking status visible to all threads immediately
    private volatile String bookedBy;   // volatile = guest name visible to all threads when set

    public Room(int number, String type, double pricePerNight) {
        this.number = number;
        this.type = type;
        this.pricePerNight = pricePerNight;
        this.bookingLock = new ReentrantLock();
        this.booked = false;
        this.bookedBy = null;
    }

    /**
     * Attempt to book this room. Uses ReentrantLock to ensure only one guest books it.
     * Returns true if booking succeeded.
     */
    public boolean tryBook(String guestName) {
        bookingLock.lock();
        try {
            if (booked) {
                return false;
            }
            booked = true;
            bookedBy = guestName;
            return true;
        } finally {
            bookingLock.unlock();
        }
    }

    public void release() {
        bookingLock.lock();
        try {
            booked = false;
            bookedBy = null;
        } finally {
            bookingLock.unlock();
        }
    }

    public int getNumber() { return number; }
    public String getType() { return type; }
    public double getPricePerNight() { return pricePerNight; }
    public boolean isBooked() { return booked; }
    public String getBookedBy() { return bookedBy; }
    public ReentrantLock getBookingLock() { return bookingLock; }

    @Override
    public String toString() {
        return "Room-" + number + "(" + type + ", " + (booked ? "booked by " + bookedBy : "available") + ")";
    }
}

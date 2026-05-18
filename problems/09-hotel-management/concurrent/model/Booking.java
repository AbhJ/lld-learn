/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Booking.java — Immutable booking record

package model;

public class Booking {
    private final String guestName;     // final = immutable; safe to share across threads without sync
    private final Room room;            // final = room ref never changes; safe publication
    private final String date;          // final = immutable; visible to all threads after construction

    public Booking(String guestName, Room room, String date) {
        this.guestName = guestName;
        this.room = room;
        this.date = date;
    }

    public String getGuestName() { return guestName; }
    public Room getRoom() { return room; }
    public String getDate() { return date; }

    @Override
    public String toString() {
        return "Booking(" + guestName + " -> Room-" + room.getNumber() + " on " + date + ")";
    }
}

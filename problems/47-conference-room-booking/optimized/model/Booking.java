/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Booking.java — Represents a confirmed room reservation; leaf in the BookingComponent composite
import java.util.Collections;
import java.util.List;

public class Booking implements BookingComponent { // implements BookingComponent = leaf node treatable as a single-slot component
    private String id;                  // private = booking ID hidden from external modification
    private Room room;                  // private = room reference set at creation only
    private TimeSlot timeSlot;          // private = time range fixed once booked
    private User organizer;             // private = who created this booking
    private MeetingType meetingType;    // private = meeting type locked at booking time
    private String title;               // private = title encapsulated within this booking
    private boolean cancelled;          // private = only cancel() can flip this flag

    public Booking(String id, Room room, TimeSlot timeSlot, User organizer, MeetingType meetingType, String title) {
        this.id = id; this.room = room; this.timeSlot = timeSlot;
        this.organizer = organizer; this.meetingType = meetingType; this.title = title; this.cancelled = false;
    }

    public String getId() { return id; }
    public Room getRoom() { return room; }
    public TimeSlot getTimeSlot() { return timeSlot; }
    public User getOrganizer() { return organizer; }
    public String getTitle() { return title; }
    public boolean isCancelled() { return cancelled; }
    @Override public void cancel() { this.cancelled = true; }
    @Override public List<TimeSlot> getTimeSlots() { return Collections.singletonList(timeSlot); }

    @Override public String toString() { return room.getName() + ", " + timeSlot + " (" + title + ")"; }
}

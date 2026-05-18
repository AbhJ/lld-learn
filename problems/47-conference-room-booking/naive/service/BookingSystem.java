/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/BookingSystem.java — Manages room reservations with conflict prevention
// DESIGN PATTERN: Facade
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BookingSystem {
    private List<Room> rooms;                            // private = room list managed internally
    private List<Booking> bookings;                      // private = leaf bookings retained for conflict detection
    private List<BookingComponent> components;            // private = both leaf and recurring components for uniform lookup
    private ConflictDetector conflictDetector;            // private = conflict logic delegated to separate class
    private RoomFinder roomFinder;                        // private = strategy pattern for room selection
    private int bookingCounter;                           // private = generates unique booking IDs

    public BookingSystem() {
        this.rooms = new ArrayList<>(); this.bookings = new ArrayList<>();
        this.components = new ArrayList<>();
        this.conflictDetector = new ConflictDetector();
        this.roomFinder = new SmallestFittingStrategy(); this.bookingCounter = 0;
    }

    public void addRoom(Room room) { rooms.add(room); }
    public List<Room> getRooms() { return rooms; }
    public void setRoomFinder(RoomFinder finder) { this.roomFinder = finder; }

    public Room findAvailableRoom(int capacity, Set<Room.Amenity> amenities, TimeSlot timeSlot) {
        return roomFinder.findRoom(rooms, capacity, amenities, timeSlot, bookings);
    }

    // WHY: Returns BookingComponent so callers can treat single bookings and recurring series uniformly
    public BookingComponent bookRoom(Room room, TimeSlot timeSlot, User organizer, MeetingType type, String title) {
        if (conflictDetector.hasConflict(room, timeSlot, bookings)) {
            System.out.println("CONFLICT: " + room.getName() + " already booked");
            return null;
        }
        String id = "BK-" + String.format("%03d", ++bookingCounter);
        Booking booking = new Booking(id, room, timeSlot, organizer, type, title);
        bookings.add(booking); components.add(booking);
        System.out.println("Booking confirmed: " + booking);
        return booking;
    }

    // WHY: Returns BookingComponent (the recurring series) so callers can call cancel() once to cancel all leaves
    public BookingComponent createRecurringBooking(Room room, TimeSlot firstSlot, User organizer,
            MeetingType type, String title, RecurringBooking.Frequency freq, int occurrences) {
        String seriesId = "SER-" + (bookingCounter + 1);
        RecurringBooking recurring = new RecurringBooking(seriesId, freq, occurrences);
        int booked = 0;
        for (TimeSlot slot : recurring.generateTimeSlots(firstSlot)) {
            if (!conflictDetector.hasConflict(room, slot, bookings)) {
                String id = "BK-" + String.format("%03d", ++bookingCounter);
                Booking booking = new Booking(id, room, slot, organizer, type, title);
                bookings.add(booking); recurring.addBooking(booking); booked++;
            }
        }
        components.add(recurring);
        System.out.println("Recurring: " + booked + "/" + occurrences + " booked in " + room.getName());
        return recurring;
    }

    // WHY: Composite cancel — id may be a leaf booking id (BK-*) or a series id (SER-*); both handled the same way
    public boolean cancelBooking(String id) {
        for (BookingComponent c : components) {
            if (c.getId().equals(id)) { c.cancel(); System.out.println("Cancelled: " + id); return true; }
        }
        return false;
    }
}

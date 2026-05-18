/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/BookingSystem.java — Booking system with O(log n) conflict detection per room
// DESIGN PATTERN: Facade
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BookingSystem {
    private List<Room> rooms;                            // private = room registry managed internally
    private List<Booking> bookings;                      // private = leaf bookings retained for conflict detection
    private List<BookingComponent> components;            // private = both leaf and recurring components for uniform lookup
    private ConflictDetector conflictDetector;            // private = O(log n) TreeMap-based conflict checker
    private RoomFinder roomFinder;                        // private = capacity-indexed room finder strategy
    private int bookingCounter;                           // private = generates unique booking IDs

    public BookingSystem() {
        this.rooms = new ArrayList<>(); this.bookings = new ArrayList<>();
        this.components = new ArrayList<>();
        this.conflictDetector = new ConflictDetector(); this.bookingCounter = 0;
    }

    public void addRoom(Room room) {
        rooms.add(room);
        conflictDetector.registerRoom(room.getId());
    }

    public List<Room> getRooms() { return rooms; }

    public void initRoomFinder() {
        // WHY: Initialize capacity-indexed finder after all rooms are added
        this.roomFinder = new SmallestFittingStrategy(rooms);
    }

    public Room findAvailableRoom(int capacity, Set<Room.Amenity> amenities, TimeSlot timeSlot) {
        return roomFinder.findRoom(rooms, capacity, amenities, timeSlot, conflictDetector);
    }

    // WHY: Returns BookingComponent so callers can treat single bookings and recurring series uniformly
    public BookingComponent bookRoom(Room room, TimeSlot timeSlot, User organizer, MeetingType type, String title) {
        // WHY: O(log n) conflict check using room-specific TreeMap
        if (conflictDetector.hasConflict(room, timeSlot)) {
            System.out.println("CONFLICT: " + room.getName() + " already booked");
            return null;
        }
        String id = "BK-" + String.format("%03d", ++bookingCounter);
        Booking booking = new Booking(id, room, timeSlot, organizer, type, title);
        bookings.add(booking); components.add(booking);
        conflictDetector.addBooking(booking);
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
            if (!conflictDetector.hasConflict(room, slot)) {
                String id = "BK-" + String.format("%03d", ++bookingCounter);
                Booking booking = new Booking(id, room, slot, organizer, type, title);
                bookings.add(booking); conflictDetector.addBooking(booking);
                recurring.addBooking(booking); booked++;
            }
        }
        components.add(recurring);
        System.out.println("Recurring: " + booked + "/" + occurrences + " booked in " + room.getName());
        return recurring;
    }

    // WHY: Composite cancel — id may be a leaf booking id (BK-*) or a series id (SER-*); both handled the same way
    public boolean cancelBooking(String id) {
        for (BookingComponent c : components) {
            if (c.getId().equals(id)) {
                c.cancel();
                // Sync the conflict detector with whichever leaves were just cancelled
                if (c instanceof Booking) {
                    conflictDetector.removeBooking((Booking) c);
                } else if (c instanceof RecurringBooking) {
                    for (Booking leaf : ((RecurringBooking) c).getBookings()) conflictDetector.removeBooking(leaf);
                }
                System.out.println("Cancelled: " + id);
                return true;
            }
        }
        return false;
    }
}

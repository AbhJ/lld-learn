/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating the conference room booking system
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Conference Room Booking Demo (Naive) ===");

        BookingSystem system = new BookingSystem();
        Room roomA = new Room("r1", "Room A", 4, 1, Room.Amenity.WHITEBOARD);
        Room roomB = new Room("r2", "Room B", 8, 2, Room.Amenity.PROJECTOR, Room.Amenity.WHITEBOARD);
        Room roomC = new Room("r3", "Room C", 20, 3, Room.Amenity.PROJECTOR, Room.Amenity.VIDEO_CONF);
        system.addRoom(roomA); system.addRoom(roomB); system.addRoom(roomC);

        User alice = new User("u1", "Alice", "Engineering", 2);
        User bob = new User("u2", "Bob", "Product", 1);

        LocalDateTime today = LocalDateTime.of(2026, 5, 13, 0, 0);
        TimeSlot slot1 = new TimeSlot(today.withHour(9), today.withHour(10));

        System.out.println("\n--- Book Room ---");
        Set<Room.Amenity> need = new HashSet<>(); need.add(Room.Amenity.PROJECTOR);
        Room found = system.findAvailableRoom(6, need, slot1);
        if (found != null) system.bookRoom(found, slot1, alice, MeetingType.REVIEW, "Sprint Review");

        System.out.println("\n--- Conflict ---");
        system.bookRoom(found, slot1, bob, MeetingType.REVIEW, "Another Meeting");

        System.out.println("\n--- Recurring ---");
        TimeSlot standupSlot = new TimeSlot(today.plusDays(1).withHour(9), today.plusDays(1).withHour(9).plusMinutes(15));
        // BookingComponent = Composite; same type whether single or recurring
        BookingComponent recurring = system.createRecurringBooking(roomA, standupSlot, alice,
            MeetingType.STANDUP, "Standup", RecurringBooking.Frequency.WEEKLY, 4);
        System.out.println("Series " + recurring.getId() + " has " + recurring.getTimeSlots().size() + " slot(s)");

        System.out.println("\n--- Cancel (composite cancel via series id) ---");
        // Composite cancel: cancelling the series id cancels every leaf in one call
        system.cancelBooking(recurring.getId());

        System.out.println("\n=== Conference Room Booking Demo Complete ===");
    }
}

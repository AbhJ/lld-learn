/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/Hotel.java — Optimized with TreeMap for O(log n) room availability lookup
// DESIGN PATTERN: Facade
//
// FACADE: Main.java talks only to this class.
// Uses BookingObserver (defined in BookingObserver.java) to notify on booking events.

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;
import java.util.HashSet;

/**
 * Optimized: Uses TreeMap<roomType, Set<Room>> indexed by availability.
 * findAvailableRoom() is O(1) amortized via pre-indexed sets instead of
 * O(n) linear scan through all rooms.
 */
class Hotel {
    private String name;
    private List<Room> rooms;                       // ArrayList = stores all rooms for iteration
    private List<Booking> bookings;                 // ArrayList = append-only booking history
    private Map<String, RoomService> activeServices; // HashMap = O(1) lookup by room number
    private PricingStrategy pricingStrategy;
    private List<BookingObserver> observers;
    // HashMap<type, HashSet<Room>> = O(1) find available room by type
    private Map<String, Set<Room>> availableByType; // HashSet = O(1) add/remove/contains

    public Hotel(String name) {
        this.name = name;
        this.rooms = new ArrayList<>();
        this.bookings = new ArrayList<>();
        this.activeServices = new HashMap<>();
        this.pricingStrategy = new StandardPricing();
        this.observers = new ArrayList<>();
        this.availableByType = new HashMap<>();
    }

    public void addObserver(BookingObserver observer) { observers.add(observer); }

    public void addRoom(Room room) {
        rooms.add(room);
        String type = room.getRoomType().toLowerCase();
        availableByType.computeIfAbsent(type, k -> new HashSet<>()).add(room);
    }

    public void addRoom(String type, String roomNumber) {
        addRoom(RoomFactory.createRoom(type, roomNumber));
    }

    public void setPricingStrategy(PricingStrategy strategy) { this.pricingStrategy = strategy; }

    /**
     * O(1): Get from pre-indexed set instead of scanning all rooms.
     */
    public Room findAvailableRoom(String type) {
        Set<Room> available = availableByType.get(type.toLowerCase());
        if (available == null || available.isEmpty()) return null;
        return available.iterator().next(); // O(1) pick any available
    }

    public Booking bookRoom(Guest guest, String roomType, LocalDate checkIn, int nights) {
        Room room = findAvailableRoom(roomType);
        if (room == null) { System.out.println("  No " + roomType + " room available."); return null; }
        room.setState(RoomState.BOOKED);
        // Remove from available index
        Set<Room> available = availableByType.get(roomType.toLowerCase());
        if (available != null) available.remove(room);

        Booking booking = new Booking(guest, room, checkIn, nights);
        bookings.add(booking);
        for (BookingObserver obs : observers) obs.onBookingConfirmed(booking);
        return booking;
    }

    public boolean checkIn(Booking booking) {
        if (booking.checkIn()) {
            activeServices.put(booking.getRoom().getRoomNumber(), new RoomService(booking.getRoom().getRoomNumber()));
            for (BookingObserver obs : observers) obs.onCheckIn(booking);
            return true;
        }
        return false;
    }

    public Bill checkOut(Booking booking) {
        RoomService service = activeServices.get(booking.getRoom().getRoomNumber());
        if (booking.checkOut()) {
            activeServices.remove(booking.getRoom().getRoomNumber());
            // Add back to available index
            String type = booking.getRoom().getRoomType().toLowerCase();
            availableByType.computeIfAbsent(type, k -> new HashSet<>()).add(booking.getRoom());
            for (BookingObserver obs : observers) obs.onCheckOut(booking);
            return new Bill(booking, service, pricingStrategy);
        }
        return null;
    }

    public void orderRoomService(String roomNumber, String item, double price) {
        RoomService service = activeServices.get(roomNumber);
        if (service != null) service.addOrder(item, price);
    }

    public void setRoomMaintenance(String roomNumber, boolean maintenance) {
        for (Room room : rooms) {
            if (room.getRoomNumber().equals(roomNumber)) {
                if (maintenance) {
                    room.setState(RoomState.MAINTENANCE);
                    Set<Room> avail = availableByType.get(room.getRoomType().toLowerCase());
                    if (avail != null) avail.remove(room);
                } else {
                    room.setState(RoomState.AVAILABLE);
                    availableByType.computeIfAbsent(room.getRoomType().toLowerCase(), k -> new HashSet<>()).add(room);
                }
                break;
            }
        }
    }

    public String getName() { return name; }
    public PricingStrategy getPricingStrategy() { return pricingStrategy; }

    public String getRoomStatus() {
        StringBuilder sb = new StringBuilder();
        for (Room room : rooms) sb.append("  ").append(room).append("\n");
        return sb.toString().trim();
    }
}

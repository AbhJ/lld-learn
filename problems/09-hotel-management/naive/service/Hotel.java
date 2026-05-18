/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/Hotel.java — Facade managing rooms, bookings, and check-in/out (linear room search)
// DESIGN PATTERN: Facade
//
// FACADE: Main.java talks only to this class.
// Uses BookingObserver (defined in BookingObserver.java) to notify on booking events.

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Hotel {
    private String name;                // private = hotel name encapsulated
    private List<Room> rooms;           // private = room list managed internally
    private List<Booking> bookings;     // private = booking history encapsulated
    private Map<String, RoomService> activeServices; // private = maps room# to active services
    private PricingStrategy pricingStrategy; // private = strategy swappable at runtime
    private List<BookingObserver> observers; // private = observer list for event notifications

    public Hotel(String name) {
        this.name = name;
        this.rooms = new ArrayList<>();
        this.bookings = new ArrayList<>();
        this.activeServices = new HashMap<>();
        this.pricingStrategy = new StandardPricing();
        this.observers = new ArrayList<>();
    }

    public void addObserver(BookingObserver observer) { observers.add(observer); }
    public void addRoom(Room room) { rooms.add(room); }
    public void addRoom(String type, String roomNumber) { rooms.add(RoomFactory.createRoom(type, roomNumber)); }
    public void setPricingStrategy(PricingStrategy strategy) { this.pricingStrategy = strategy; }

    // Linear scan to find available room
    public Room findAvailableRoom(String type) {
        for (Room room : rooms) {
            if (room.getRoomType().equalsIgnoreCase(type) && room.getState() == RoomState.AVAILABLE) return room;
        }
        return null;
    }

    public Booking bookRoom(Guest guest, String roomType, LocalDate checkIn, int nights) {
        Room room = findAvailableRoom(roomType);
        if (room == null) { System.out.println("  No " + roomType + " room available."); return null; }
        room.setState(RoomState.BOOKED);
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
                room.setState(maintenance ? RoomState.MAINTENANCE : RoomState.AVAILABLE);
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

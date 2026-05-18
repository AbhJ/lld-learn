/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/HotelService.java — Hotel with ConcurrentHashMap for availability and per-room locking

package service;

import model.Booking;
import model.Room;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class HotelService {
    private final List<Room> rooms;                  // final = room list ref stable; rooms added at setup
    private final ConcurrentHashMap<String, List<Booking>> bookingsByDate; // ConcurrentHashMap = thread-safe O(1) lookup by date
    private final List<String> bookingLog;           // synchronizedList = thread-safe append from any thread

    public HotelService() {
        this.rooms = new ArrayList<>();
        this.bookingsByDate = new ConcurrentHashMap<>();
        this.bookingLog = Collections.synchronizedList(new ArrayList<>());
    }

    public void addRoom(Room room) {
        rooms.add(room);
    }

    /**
     * Attempt to book any available room for a guest on a given date.
     * Uses per-room ReentrantLock to prevent two guests booking the last room.
     * Returns the Booking if successful, null otherwise.
     */
    public Booking bookRoom(String guestName, String date) {
        for (Room room : rooms) {
            if (room.tryBook(guestName)) {
                Booking booking = new Booking(guestName, room, date);
                bookingsByDate.computeIfAbsent(date, k ->
                    Collections.synchronizedList(new ArrayList<>())).add(booking);
                bookingLog.add(guestName + " booked Room-" + room.getNumber());
                return booking;
            }
        }
        bookingLog.add(guestName + " REJECTED — no rooms available");
        return null;
    }

    /**
     * Try to book a specific room.
     */
    public Booking bookSpecificRoom(String guestName, Room room, String date) {
        if (room.tryBook(guestName)) {
            Booking booking = new Booking(guestName, room, date);
            bookingsByDate.computeIfAbsent(date, k ->
                Collections.synchronizedList(new ArrayList<>())).add(booking);
            bookingLog.add(guestName + " booked Room-" + room.getNumber());
            return booking;
        }
        bookingLog.add(guestName + " REJECTED for Room-" + room.getNumber() + " — already booked");
        return null;
    }

    public List<Room> getRooms() { return rooms; }
    public List<String> getBookingLog() { return bookingLog; }

    public int getBookedCount() {
        int count = 0;
        for (Room r : rooms) {
            if (r.isBooked()) count++;
        }
        return count;
    }

    public int getAvailableCount() {
        return rooms.size() - getBookedCount();
    }
}

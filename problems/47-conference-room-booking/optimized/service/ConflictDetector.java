/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/ConflictDetector.java — O(log n) conflict detection using TreeMap per room
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ConflictDetector {
    // TreeMap<startTime, Booking> per room gives O(log n) conflict check
    // instead of scanning all bookings across all rooms
    private Map<String, TreeMap<LocalDateTime, Booking>> roomSchedules; // HashMap<roomId, TreeMap> = O(1) room lookup + O(log n) time search

    public ConflictDetector() {
        this.roomSchedules = new HashMap<>();
    }

    public void registerRoom(String roomId) {
        roomSchedules.putIfAbsent(roomId, new TreeMap<>());
    }

    // WHY: O(log n) — check floor/ceiling entries in the room's TreeMap
    public boolean hasConflict(Room room, TimeSlot timeSlot) {
        TreeMap<LocalDateTime, Booking> schedule = roomSchedules.get(room.getId());
        if (schedule == null || schedule.isEmpty()) return false;

        // Check the booking starting just before our start time
        Map.Entry<LocalDateTime, Booking> floor = schedule.floorEntry(timeSlot.getStart());
        if (floor != null && !floor.getValue().isCancelled() &&
            floor.getValue().getTimeSlot().overlapsWith(timeSlot)) {
            return true;
        }

        // Check the booking starting at or just after our start time
        Map.Entry<LocalDateTime, Booking> ceiling = schedule.ceilingEntry(timeSlot.getStart());
        if (ceiling != null && !ceiling.getValue().isCancelled() &&
            ceiling.getValue().getTimeSlot().overlapsWith(timeSlot)) {
            return true;
        }

        return false;
    }

    public void addBooking(Booking booking) {
        roomSchedules.computeIfAbsent(booking.getRoom().getId(), k -> new TreeMap<>())
            .put(booking.getTimeSlot().getStart(), booking);
    }

    public void removeBooking(Booking booking) {
        TreeMap<LocalDateTime, Booking> schedule = roomSchedules.get(booking.getRoom().getId());
        if (schedule != null) {
            schedule.remove(booking.getTimeSlot().getStart());
        }
    }
}

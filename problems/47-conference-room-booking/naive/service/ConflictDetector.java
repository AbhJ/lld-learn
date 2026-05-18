/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/ConflictDetector.java — O(n*m) conflict detection scanning all bookings
import java.util.List;

public class ConflictDetector {
    // O(n) scan through all bookings for a room
    public boolean hasConflict(Room room, TimeSlot timeSlot, List<Booking> existingBookings) {
        for (Booking booking : existingBookings) {
            if (!booking.isCancelled() &&
                booking.getRoom().getId().equals(room.getId()) &&
                booking.getTimeSlot().overlapsWith(timeSlot)) {
                return true;
            }
        }
        return false;
    }
}

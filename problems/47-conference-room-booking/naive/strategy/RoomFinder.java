/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/RoomFinder.java — Finds available rooms matching criteria
import java.util.List;
import java.util.Set;

public interface RoomFinder { // interface = contract; room finding strategies must implement this
    Room findRoom(List<Room> rooms, int requiredCapacity, Set<Room.Amenity> requiredAmenities,
                  TimeSlot timeSlot, List<Booking> existingBookings);
}

class SmallestFittingStrategy implements RoomFinder { // implements = finds smallest room that fits
    private ConflictDetector conflictDetector = new ConflictDetector();

    @Override
    public Room findRoom(List<Room> rooms, int requiredCapacity, Set<Room.Amenity> requiredAmenities,
                         TimeSlot timeSlot, List<Booking> existingBookings) {
        Room best = null;
        for (Room room : rooms) {
            if (room.fitsCapacity(requiredCapacity) && room.hasAllAmenities(requiredAmenities) &&
                !conflictDetector.hasConflict(room, timeSlot, existingBookings)) {
                if (best == null || room.getCapacity() < best.getCapacity()) best = room;
            }
        }
        return best;
    }
}

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/RoomFinder.java — Finds rooms with capacity index for fast filtering
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Map;

public interface RoomFinder { // interface = contract for room-finding strategies
    Room findRoom(List<Room> rooms, int requiredCapacity, Set<Room.Amenity> requiredAmenities,
                  TimeSlot timeSlot, ConflictDetector conflictDetector);
}

class SmallestFittingStrategy implements RoomFinder { // implements = finds smallest fitting room
    // TreeMap<capacity, List<Room>> indexes rooms by capacity
    // so we only check rooms >= required capacity via tailMap — O(log n) to find starting point
    private TreeMap<Integer, List<Room>> capacityIndex; // TreeMap = sorted by capacity; tailMap skips too-small rooms

    public SmallestFittingStrategy(List<Room> allRooms) {
        this.capacityIndex = new TreeMap<>();
        for (Room room : allRooms) {
            capacityIndex.computeIfAbsent(room.getCapacity(), k -> new ArrayList<>()).add(room);
        }
    }

    @Override
    public Room findRoom(List<Room> rooms, int requiredCapacity, Set<Room.Amenity> requiredAmenities,
                         TimeSlot timeSlot, ConflictDetector conflictDetector) {
        // WHY: tailMap gives only rooms with capacity >= required, smallest first
        for (Map.Entry<Integer, List<Room>> entry : capacityIndex.tailMap(requiredCapacity).entrySet()) {
            for (Room room : entry.getValue()) {
                if (room.hasAllAmenities(requiredAmenities) && !conflictDetector.hasConflict(room, timeSlot)) {
                    return room;
                }
            }
        }
        return null;
    }
}

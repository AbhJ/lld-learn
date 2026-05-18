/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/MeetingType.java — Enumerates meeting types
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum MeetingType { // enum = fixed set of meeting categories with predefined needs
    STANDUP(15, 5, new HashSet<>(Arrays.asList(Room.Amenity.WHITEBOARD))),
    REVIEW(60, 10, new HashSet<>(Arrays.asList(Room.Amenity.PROJECTOR))),
    INTERVIEW(45, 4, new HashSet<>(Arrays.asList(Room.Amenity.WHITEBOARD, Room.Amenity.VIDEO_CONF)));

    private int durationMinutes;                // private = duration per meeting type
    private int typicalAttendees;               // private = expected attendee count
    private Set<Room.Amenity> requiredAmenities; // private = amenities this meeting type needs

    MeetingType(int durationMinutes, int typicalAttendees, Set<Room.Amenity> requiredAmenities) {
        this.durationMinutes = durationMinutes; this.typicalAttendees = typicalAttendees;
        this.requiredAmenities = requiredAmenities;
    }

    public int getDurationMinutes() { return durationMinutes; }
    public Set<Room.Amenity> getRequiredAmenities() { return requiredAmenities; }
}

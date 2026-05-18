/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Room.java — Represents a conference room with capacity and amenities
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Room {
    public enum Amenity { PROJECTOR, WHITEBOARD, VIDEO_CONF, PHONE, TV_SCREEN } // enum = fixed amenity types

    private String id;              // private = room ID encapsulated
    private String name;            // private = display name of room
    private int capacity;           // private = max people this room holds
    private int floor;              // private = which floor this room is on
    private Set<Amenity> amenities; // private = what equipment this room has

    public Room(String id, String name, int capacity, int floor, Amenity... amenities) {
        this.id = id; this.name = name; this.capacity = capacity; this.floor = floor;
        this.amenities = new HashSet<>(Arrays.asList(amenities));
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getCapacity() { return capacity; }
    public int getFloor() { return floor; }
    public Set<Amenity> getAmenities() { return amenities; }
    public boolean hasAllAmenities(Set<Amenity> required) { return amenities.containsAll(required); }
    public boolean fitsCapacity(int required) { return capacity >= required; }

    @Override public String toString() { return name + " (cap:" + capacity + ", " + amenities + ")"; }
}

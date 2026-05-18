/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Room.java — Hotel room with type, state, and pricing

abstract class Room {                   // abstract = can't create Room directly; must be Single/Double/Suite
    protected String roomNumber;        // protected = subclasses can access room number
    protected RoomState state;          // protected = subclasses can read state directly
    protected double basePrice;         // protected = subclasses set their own base price

    public Room(String roomNumber, double basePrice) {
        this.roomNumber = roomNumber;
        this.basePrice = basePrice;
        this.state = RoomState.AVAILABLE;
    }

    public boolean setState(RoomState newState) {
        if (state.canTransitionTo(newState)) { state = newState; return true; }
        return false;
    }

    public abstract String getRoomType(); // abstract = each subclass provides its own room type name
    public String getRoomNumber() { return roomNumber; }
    public RoomState getState() { return state; }
    public double getBasePrice() { return basePrice; }

    @Override
    public String toString() {
        return "Room " + roomNumber + " (" + getRoomType() + "): " + state + " - $" + String.format("%.0f", basePrice) + "/night";
    }
}

class SingleRoom extends Room {         // extends = inherits from Room; sets single-room price
    public SingleRoom(String roomNumber) { super(roomNumber, 100.0); }
    @Override public String getRoomType() { return "Single"; }
}

class DoubleRoom extends Room {         // extends = inherits from Room; sets double-room price
    public DoubleRoom(String roomNumber) { super(roomNumber, 150.0); }
    @Override public String getRoomType() { return "Double"; }
}

class Suite extends Room {              // extends = inherits from Room; sets suite price
    public Suite(String roomNumber) { super(roomNumber, 300.0); }
    @Override public String getRoomType() { return "Suite"; }
}

class RoomFactory {
    public static Room createRoom(String type, String roomNumber) { // static = factory; no instance needed
        switch (type.toUpperCase()) {
            case "SINGLE": return new SingleRoom(roomNumber);
            case "DOUBLE": return new DoubleRoom(roomNumber);
            case "SUITE": return new Suite(roomNumber);
            default: throw new IllegalArgumentException("Unknown room type: " + type);
        }
    }
}

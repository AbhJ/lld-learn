/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Room.java — Hotel room with type, state, and pricing

abstract class Room {
    protected String roomNumber;
    protected RoomState state;
    protected double basePrice;

    public Room(String roomNumber, double basePrice) {
        this.roomNumber = roomNumber;
        this.basePrice = basePrice;
        this.state = RoomState.AVAILABLE;
    }

    public boolean setState(RoomState newState) {
        if (state.canTransitionTo(newState)) { state = newState; return true; }
        return false;
    }

    public abstract String getRoomType();
    public String getRoomNumber() { return roomNumber; }
    public RoomState getState() { return state; }
    public double getBasePrice() { return basePrice; }

    @Override
    public String toString() {
        return "Room " + roomNumber + " (" + getRoomType() + "): " + state + " - $" + String.format("%.0f", basePrice) + "/night";
    }
}

class SingleRoom extends Room {
    public SingleRoom(String roomNumber) { super(roomNumber, 100.0); }
    @Override public String getRoomType() { return "Single"; }
}

class DoubleRoom extends Room {
    public DoubleRoom(String roomNumber) { super(roomNumber, 150.0); }
    @Override public String getRoomType() { return "Double"; }
}

class Suite extends Room {
    public Suite(String roomNumber) { super(roomNumber, 300.0); }
    @Override public String getRoomType() { return "Suite"; }
}

class RoomFactory {
    public static Room createRoom(String type, String roomNumber) {
        switch (type.toUpperCase()) {
            case "SINGLE": return new SingleRoom(roomNumber);
            case "DOUBLE": return new DoubleRoom(roomNumber);
            case "SUITE": return new Suite(roomNumber);
            default: throw new IllegalArgumentException("Unknown room type: " + type);
        }
    }
}

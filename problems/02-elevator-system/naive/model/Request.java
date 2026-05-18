/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Request.java — Elevator request with source floor, destination, and direction

enum Direction {                      // enum = fixed set of directions; type-safe alternatives
    UP, DOWN, NONE
}

class Request {
    private int sourceFloor;          // private = only this class manages floor data
    private int destinationFloor;     // private = hidden from outside; accessed via getter
    private Direction direction;      // private = computed internally from floors

    public Request(int sourceFloor, int destinationFloor) {
        this.sourceFloor = sourceFloor;
        this.destinationFloor = destinationFloor;
        this.direction = destinationFloor > sourceFloor ? Direction.UP : Direction.DOWN;
    }

    public int getSourceFloor() { return sourceFloor; }
    public int getDestinationFloor() { return destinationFloor; }
    public Direction getDirection() { return direction; }

    @Override
    public String toString() {
        return "Request(Floor " + sourceFloor + " -> Floor " + destinationFloor + " " + direction + ")";
    }
}

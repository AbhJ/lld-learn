/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Request.java — Immutable elevator request with source and destination floor

package model;

public class Request {
    private final int sourceFloor;    // final = immutable; safe publication to all threads
    private final int destinationFloor; // final = set once; no synchronization needed
    private final String passengerId; // final = immutable identity; safe to share

    public Request(int sourceFloor, int destinationFloor, String passengerId) {
        this.sourceFloor = sourceFloor;
        this.destinationFloor = destinationFloor;
        this.passengerId = passengerId;
    }

    public int getSourceFloor() { return sourceFloor; }
    public int getDestinationFloor() { return destinationFloor; }
    public String getPassengerId() { return passengerId; }

    @Override
    public String toString() {
        return "Request(" + passengerId + ": Floor " + sourceFloor + " -> " + destinationFloor + ")";
    }
}

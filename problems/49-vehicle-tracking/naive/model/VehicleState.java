/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/VehicleState.java — Enumerates vehicle states
public enum VehicleState { // enum = fixed vehicle states; type-safe
    MOVING("Moving"), IDLE("Idle"), PARKED("Parked"), MAINTENANCE("In Maintenance");
    private String displayName; // private = friendly name for each state
    VehicleState(String displayName) { this.displayName = displayName; }
    @Override public String toString() { return displayName; }
}

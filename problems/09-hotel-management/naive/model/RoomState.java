/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/RoomState.java — Room lifecycle states with valid transitions

enum RoomState {                        // enum = fixed lifecycle states; prevents invalid string values
    AVAILABLE, BOOKED, OCCUPIED, MAINTENANCE;

    public boolean canTransitionTo(RoomState target) {
        switch (this) {
            case AVAILABLE: return target == BOOKED || target == MAINTENANCE;
            case BOOKED: return target == OCCUPIED || target == AVAILABLE;
            case OCCUPIED: return target == AVAILABLE;
            case MAINTENANCE: return target == AVAILABLE;
            default: return false;
        }
    }
}

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/ElevatorState.java — Enum defining possible elevator states with availability checks

enum ElevatorState {                  // enum = fixed set of states; type-safe state machine
    IDLE,
    MOVING_UP,
    MOVING_DOWN,
    MAINTENANCE;

    public boolean isAvailable() {
        return this != MAINTENANCE;
    }
}

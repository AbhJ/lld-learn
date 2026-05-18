/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/TripState.java — Trip lifecycle states with valid transitions

public enum TripState { // enum = fixed trip lifecycle states; enforces valid transitions
    REQUESTED,
    MATCHED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    public boolean canTransitionTo(TripState next) {
        switch (this) {
            case REQUESTED: return next == MATCHED || next == CANCELLED;
            case MATCHED: return next == IN_PROGRESS || next == CANCELLED;
            case IN_PROGRESS: return next == COMPLETED || next == CANCELLED;
            case COMPLETED: return false;
            case CANCELLED: return false;
            default: return false;
        }
    }
}

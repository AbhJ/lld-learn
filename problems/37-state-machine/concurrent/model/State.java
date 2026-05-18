/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/State.java — Enum representing states in an order lifecycle

public enum State {                     // enum = fixed set of states; type-safe, no invalid values
    CREATED,
    VALIDATED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    public boolean canTransitionTo(State target) {
        switch (this) {
            case CREATED: return target == VALIDATED || target == CANCELLED;
            case VALIDATED: return target == PROCESSING || target == CANCELLED;
            case PROCESSING: return target == SHIPPED || target == CANCELLED;
            case SHIPPED: return target == DELIVERED;
            case DELIVERED: return false;
            case CANCELLED: return false;
            default: return false;
        }
    }
}

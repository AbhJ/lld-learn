/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/OrderState.java — Order lifecycle states with valid transition rules

public enum OrderState { // enum = fixed set of order lifecycle states; safer than strings
    PLACED,
    CONFIRMED,
    PREPARING,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED;

    public boolean canTransitionTo(OrderState next) {
        switch (this) {
            case PLACED: return next == CONFIRMED || next == CANCELLED;
            case CONFIRMED: return next == PREPARING || next == CANCELLED;
            case PREPARING: return next == OUT_FOR_DELIVERY || next == CANCELLED;
            case OUT_FOR_DELIVERY: return next == DELIVERED;
            case DELIVERED: return false;
            case CANCELLED: return false;
            default: return false;
        }
    }
}

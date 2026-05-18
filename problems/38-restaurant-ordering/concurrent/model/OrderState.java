/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/OrderState.java — Order lifecycle states

public enum OrderState {                // enum = fixed set of lifecycle states; type-safe transitions
    BUILDING,    // waiter is adding items
    SUBMITTED,   // order complete, ready for kitchen
    PREPARING,   // kitchen picked it up
    READY,       // food is ready
    SERVED       // delivered to table
}

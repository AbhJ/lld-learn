/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/DeliveryState.java — Pizza order lifecycle states

public enum DeliveryState { // enum = fixed delivery lifecycle; used as CAS expected/next values
    PLACED,
    PREPARING,
    BAKING,
    READY,
    OUT_FOR_DELIVERY,
    DELIVERED
}

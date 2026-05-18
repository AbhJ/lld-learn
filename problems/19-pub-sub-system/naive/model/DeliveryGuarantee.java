/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/DeliveryGuarantee.java — Message delivery semantics (at-most-once, at-least-once)

public enum DeliveryGuarantee {                  // enum = fixed set of delivery modes; type-safe
    AT_MOST_ONCE,   // Fire and forget
    AT_LEAST_ONCE;  // Require acknowledgment, retry on failure
}

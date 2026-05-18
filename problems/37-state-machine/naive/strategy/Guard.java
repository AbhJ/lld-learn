/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/Guard.java — Condition that must be true for a transition to fire
public interface Guard {                // interface = contract for transition conditions
    boolean evaluate(Event event);      // returns true if transition is allowed
    String getDescription();
}

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/EventFilter.java — Determines which events a subscription receives
public interface EventFilter { // interface = contract for filtering logic; swappable per subscription
    boolean accept(Event event);
}

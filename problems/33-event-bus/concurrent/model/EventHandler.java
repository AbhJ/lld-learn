/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/EventHandler.java — Functional interface for event handlers

public interface EventHandler { // interface with single method = can be used as lambda
    void handle(Event event);
}

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Guard.java — Condition evaluated before allowing a transition
public interface Guard {                // interface = contract; can be a lambda predicate
    boolean evaluate(Event event);
}

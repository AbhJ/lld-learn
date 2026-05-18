/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/RepeatMode.java — Enumerates repeat modes (none, one, all)
public enum RepeatMode {                      // enum = fixed set of repeat modes; no invalid values
    NONE, ONE, ALL;
    @Override public String toString() { return name(); }
}

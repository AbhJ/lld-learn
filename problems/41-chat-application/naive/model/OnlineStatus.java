/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/OnlineStatus.java — Enumerates user presence states
public enum OnlineStatus {                    // enum = fixed set of statuses; no invalid values possible
    ONLINE, OFFLINE, AWAY;

    @Override
    public String toString() {
        return name();
    }
}

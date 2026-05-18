/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Priority.java — Notification priority levels for delivery ordering

public enum Priority {                           // enum = fixed priority constants with associated config
    LOW(1, 0),
    MEDIUM(2, 1),
    HIGH(3, 2),
    CRITICAL(4, 3);

    private final int level;                     // final = immutable after construction
    private final int maxRetries;                // final = set once; never changes

    Priority(int level, int maxRetries) {
        this.level = level;
        this.maxRetries = maxRetries;
    }

    public int getLevel() { return level; }
    public int getMaxRetries() { return maxRetries; }
}

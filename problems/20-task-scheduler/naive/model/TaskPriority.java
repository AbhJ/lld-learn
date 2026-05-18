/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/TaskPriority.java — Task priority levels for scheduling order decisions

public enum TaskPriority {                       // enum = fixed set of priority constants; type-safe
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    CRITICAL(4);

    private final int level;                     // final = set once in constructor, never changes

    TaskPriority(int level) {
        this.level = level;
    }

    public int getLevel() { return level; }
}

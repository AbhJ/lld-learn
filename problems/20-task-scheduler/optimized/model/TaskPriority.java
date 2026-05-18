/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/TaskPriority.java — Task priority levels for scheduling order decisions

public enum TaskPriority {                       // enum = fixed priority levels; used for queue ordering
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    CRITICAL(4);

    private final int level;                     // final = immutable priority value

    TaskPriority(int level) { this.level = level; }
    public int getLevel() { return level; }
}

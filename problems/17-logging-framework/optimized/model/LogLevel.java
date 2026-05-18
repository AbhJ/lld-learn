/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/LogLevel.java — Log severity levels (DEBUG through FATAL) with ordering

public enum LogLevel {                           // enum = fixed set of severity levels; ordered by int value
    DEBUG(0),
    INFO(1),
    WARN(2),
    ERROR(3),
    FATAL(4);

    private final int severity;                  // final = set once in constructor; immutable

    LogLevel(int severity) {
        this.severity = severity;
    }

    public int getSeverity() { return severity; }

    public boolean isAtLeast(LogLevel other) {
        return this.severity >= other.severity;
    }
}

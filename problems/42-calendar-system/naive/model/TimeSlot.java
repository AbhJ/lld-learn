/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/TimeSlot.java — Represents a time range with start and end
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeSlot {
    private LocalDateTime start;              // private = enforced by constructor validation
    private LocalDateTime end;                // private = must be after start

    public TimeSlot(LocalDateTime start, LocalDateTime end) {
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        this.start = start;
        this.end = end;
    }

    public LocalDateTime getStart() { return start; }
    public LocalDateTime getEnd() { return end; }

    public boolean overlapsWith(TimeSlot other) {
        return start.isBefore(other.end) && other.start.isBefore(end);
    }

    public long getDurationMinutes() {
        return java.time.Duration.between(start, end).toMinutes();
    }

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return start.format(fmt) + " - " + end.format(fmt);
    }
}

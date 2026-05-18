/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/TimeSlot.java — Time slot with start/end, implements Comparable for ConcurrentSkipListMap

public class TimeSlot implements Comparable<TimeSlot> { // Comparable = required for ConcurrentSkipListMap ordering
    private final long startTime; // final = immutable; safe to read from any thread without sync
    private final long endTime;   // final = immutable; no race conditions on time boundaries

    public TimeSlot(long startTime, long endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; }

    public boolean overlaps(TimeSlot other) {
        return this.startTime < other.endTime && other.startTime < this.endTime;
    }

    @Override
    public int compareTo(TimeSlot other) {
        int cmp = Long.compare(this.startTime, other.startTime);
        if (cmp != 0) return cmp;
        return Long.compare(this.endTime, other.endTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeSlot)) return false;
        TimeSlot ts = (TimeSlot) o;
        return startTime == ts.startTime && endTime == ts.endTime;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(startTime) * 31 + Long.hashCode(endTime);
    }

    @Override
    public String toString() {
        return "[" + startTime + "-" + endTime + "]";
    }
}

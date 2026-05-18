/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Event.java — Calendar event with start/end times

public class Event {
    private final String eventId;             // final = immutable after construction; safe to share across threads
    private final String title;               // final = no thread can change title after creation
    private final long startTime;             // final = time boundaries are fixed once created
    private final long endTime;               // final = guarantees safe publication to all threads
    private final String bookedBy;            // final = ownership is immutable

    public Event(String eventId, String title, long startTime, long endTime, String bookedBy) {
        this.eventId = eventId;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.bookedBy = bookedBy;
    }

    public String getEventId() { return eventId; }
    public String getTitle() { return title; }
    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; }
    public String getBookedBy() { return bookedBy; }

    public boolean overlaps(long otherStart, long otherEnd) {
        return startTime < otherEnd && otherStart < endTime;
    }

    @Override
    public String toString() {
        return title + " [" + startTime + "-" + endTime + "] by " + bookedBy;
    }
}

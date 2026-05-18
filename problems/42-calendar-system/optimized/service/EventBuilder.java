/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/EventBuilder.java — Fluent builder for constructing complex events
import java.time.LocalDateTime;

public class EventBuilder {
    private String id;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String organizerId;
    private RecurrenceRule recurrenceRule;
    private int reminderMinutes = -1;

    public EventBuilder(String id) {
        this.id = id;
    }

    public EventBuilder title(String title) { this.title = title; return this; }
    public EventBuilder description(String description) { this.description = description; return this; }
    public EventBuilder from(LocalDateTime start) { this.startTime = start; return this; }
    public EventBuilder to(LocalDateTime end) { this.endTime = end; return this; }
    public EventBuilder organizer(String organizerId) { this.organizerId = organizerId; return this; }
    public EventBuilder recurring(RecurrenceRule rule) { this.recurrenceRule = rule; return this; }
    public EventBuilder reminder(int minutesBefore) { this.reminderMinutes = minutesBefore; return this; }

    public Event build() {
        if (title == null || startTime == null || endTime == null || organizerId == null) {
            throw new IllegalStateException("Title, start, end, and organizer are required");
        }
        TimeSlot slot = new TimeSlot(startTime, endTime);
        Event event = new Event(id, title, slot, organizerId);
        if (description != null) event.setDescription(description);
        if (recurrenceRule != null) event.setRecurrenceRule(recurrenceRule);
        if (reminderMinutes >= 0) event.addReminder(new Reminder(reminderMinutes, title));
        return event;
    }
}

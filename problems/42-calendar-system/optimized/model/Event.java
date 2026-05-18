/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Event.java — Represents a scheduled event with time, location, and attendees
import java.util.ArrayList;
import java.util.List;

public class Event {
    private String id;
    private String title;
    private String description;
    private TimeSlot timeSlot;
    private String organizerId;
    private RecurrenceRule recurrenceRule;
    private List<Reminder> reminders;
    private List<Invitation> invitations;
    private List<String> attendeeIds;

    public Event(String id, String title, TimeSlot timeSlot, String organizerId) {
        this.id = id;
        this.title = title;
        this.timeSlot = timeSlot;
        this.organizerId = organizerId;
        this.reminders = new ArrayList<>();
        this.invitations = new ArrayList<>();
        this.attendeeIds = new ArrayList<>();
        this.attendeeIds.add(organizerId);
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public TimeSlot getTimeSlot() { return timeSlot; }
    public String getOrganizerId() { return organizerId; }
    public RecurrenceRule getRecurrenceRule() { return recurrenceRule; }
    public List<Reminder> getReminders() { return reminders; }
    public List<Invitation> getInvitations() { return invitations; }
    public List<String> getAttendeeIds() { return attendeeIds; }

    public void setDescription(String description) { this.description = description; }
    public void setRecurrenceRule(RecurrenceRule rule) { this.recurrenceRule = rule; }

    public void addReminder(Reminder reminder) { reminders.add(reminder); }
    public void addInvitation(Invitation invitation) { invitations.add(invitation); }

    public void addAttendee(String userId) {
        if (!attendeeIds.contains(userId)) {
            attendeeIds.add(userId);
        }
    }

    public boolean isRecurring() { return recurrenceRule != null; }

    public List<TimeSlot> getOccurrences(int count) {
        if (recurrenceRule != null) {
            return recurrenceRule.generateOccurrences(timeSlot, count);
        }
        List<TimeSlot> single = new ArrayList<>();
        single.add(timeSlot);
        return single;
    }

    @Override
    public String toString() {
        return title + " (" + timeSlot + ")";
    }
}

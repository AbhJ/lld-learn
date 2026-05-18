/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/Calendar.java — Represents a named calendar containing events
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Calendar {
    private String userId;                    // private = owner identity encapsulated
    private String userName;                  // private = display name hidden from direct access
    private List<Event> events;               // private = events managed through add/remove methods
    private ConflictDetector conflictDetector; // private = detection logic is implementation detail

    public Calendar(String userId, String userName) {
        this.userId = userId;
        this.userName = userName;
        this.events = new ArrayList<>();
        this.conflictDetector = new ConflictDetector();
    }

    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public List<Event> getEvents() { return new ArrayList<>(events); }

    public boolean addEvent(Event event) {
        if (conflictDetector.hasConflict(event, events)) {
            System.out.println("Warning: Event '" + event.getTitle() + "' conflicts with existing event!");
        }
        events.add(event);
        return true;
    }

    public void removeEvent(String eventId) {
        events.removeIf(e -> e.getId().equals(eventId));
    }

    public List<Event> getEventsForDate(LocalDate date) {
        return events.stream()
            .filter(e -> e.getTimeSlot().getStart().toLocalDate().equals(date))
            .collect(Collectors.toList());
    }

    public List<String> checkConflicts() {
        return conflictDetector.findConflicts(events);
    }
}

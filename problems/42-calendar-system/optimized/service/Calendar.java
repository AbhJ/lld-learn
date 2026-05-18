/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/Calendar.java — Calendar with TreeMap-indexed events for O(log n) operations
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Calendar {
    private String userId;
    private String userName;
    private TreeMap<LocalDateTime, Event> eventsByStartTime; // TreeMap = O(log n) range queries by date
    private ConflictDetector conflictDetector;

    public Calendar(String userId, String userName) {
        this.userId = userId;
        this.userName = userName;
        this.eventsByStartTime = new TreeMap<>();
        this.conflictDetector = new ConflictDetector();
    }

    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public List<Event> getEvents() { return new ArrayList<>(eventsByStartTime.values()); }

    public boolean addEvent(Event event) {
        if (conflictDetector.hasConflict(event, getEvents())) {
            System.out.println("Warning: Event '" + event.getTitle() + "' conflicts with existing event!");
        }
        eventsByStartTime.put(event.getTimeSlot().getStart(), event);
        conflictDetector.indexEvent(event);
        return true;
    }

    public void removeEvent(String eventId) {
        eventsByStartTime.values().removeIf(e -> {
            if (e.getId().equals(eventId)) {
                conflictDetector.removeEvent(e);
                return true;
            }
            return false;
        });
    }

    // WHY: O(log n) range query using TreeMap subMap instead of O(n) filter
    public List<Event> getEventsForDate(LocalDate date) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
        return new ArrayList<>(eventsByStartTime.subMap(dayStart, dayEnd).values());
    }

    public List<String> checkConflicts() {
        return conflictDetector.findConflicts(getEvents());
    }
}

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/ConflictDetector.java — O(log n) overlap detection using TreeMap
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ConflictDetector {
    private TreeMap<LocalDateTime, Event> eventIndex; // TreeMap = O(log n) floor/ceiling for conflict check

    public ConflictDetector() {
        this.eventIndex = new TreeMap<>();
    }

    public void indexEvent(Event event) {
        eventIndex.put(event.getTimeSlot().getStart(), event);
    }

    public void removeEvent(Event event) {
        eventIndex.remove(event.getTimeSlot().getStart());
    }

    public boolean hasConflict(Event newEvent, List<Event> existingEvents) {
        TimeSlot newSlot = newEvent.getTimeSlot();

        // WHY: O(log n) — only check the event starting just before and just after
        Map.Entry<LocalDateTime, Event> floor = eventIndex.floorEntry(newSlot.getStart());
        Map.Entry<LocalDateTime, Event> ceiling = eventIndex.ceilingEntry(newSlot.getStart());

        if (floor != null && !floor.getValue().getId().equals(newEvent.getId())) {
            if (floor.getValue().getTimeSlot().overlapsWith(newSlot)) {
                return true;
            }
        }
        if (ceiling != null && !ceiling.getValue().getId().equals(newEvent.getId())) {
            if (ceiling.getValue().getTimeSlot().overlapsWith(newSlot)) {
                return true;
            }
        }

        // Also check entries between floor and ceiling that might overlap
        LocalDateTime searchEnd = newSlot.getEnd();
        Map.Entry<LocalDateTime, Event> entry = eventIndex.higherEntry(newSlot.getStart());
        while (entry != null && entry.getKey().isBefore(searchEnd)) {
            if (!entry.getValue().getId().equals(newEvent.getId()) &&
                entry.getValue().getTimeSlot().overlapsWith(newSlot)) {
                return true;
            }
            entry = eventIndex.higherEntry(entry.getKey());
        }

        return false;
    }

    public List<String> findConflicts(List<Event> events) {
        List<String> conflicts = new ArrayList<>();
        TreeMap<LocalDateTime, Event> sorted = new TreeMap<>();
        for (Event e : events) {
            sorted.put(e.getTimeSlot().getStart(), e);
        }

        // WHY: Only compare adjacent events in sorted order — O(n) instead of O(n^2)
        Map.Entry<LocalDateTime, Event> prev = null;
        for (Map.Entry<LocalDateTime, Event> curr : sorted.entrySet()) {
            if (prev != null && prev.getValue().getTimeSlot().overlapsWith(curr.getValue().getTimeSlot())) {
                conflicts.add("CONFLICT: " + prev.getValue().getTitle() +
                    " overlaps with " + curr.getValue().getTitle());
            }
            prev = curr;
        }
        return conflicts;
    }
}

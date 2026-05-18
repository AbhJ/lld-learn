/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/ConflictDetector.java — O(n) scan to detect overlapping events
import java.util.ArrayList;
import java.util.List;

public class ConflictDetector {

    public List<String> findConflicts(List<Event> events) {
        List<String> conflicts = new ArrayList<>();
        // O(n^2) pairwise comparison of all events
        for (int i = 0; i < events.size(); i++) {
            for (int j = i + 1; j < events.size(); j++) {
                Event e1 = events.get(i);
                Event e2 = events.get(j);
                if (e1.getTimeSlot().overlapsWith(e2.getTimeSlot())) {
                    conflicts.add("CONFLICT: " + e1.getTitle() + " overlaps with " + e2.getTitle());
                }
            }
        }
        return conflicts;
    }

    public boolean hasConflict(Event newEvent, List<Event> existingEvents) {
        // O(n) linear scan through all existing events
        for (Event existing : existingEvents) {
            if (!existing.getId().equals(newEvent.getId()) &&
                existing.getTimeSlot().overlapsWith(newEvent.getTimeSlot())) {
                return true;
            }
        }
        return false;
    }
}

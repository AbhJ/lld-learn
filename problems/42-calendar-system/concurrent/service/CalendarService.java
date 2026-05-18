/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/CalendarService.java — ReentrantLock per calendar for atomic overlap-check-then-book

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class CalendarService {
    private final TreeMap<Long, Event> events; // final = reference won't change; lock guards contents
    private final ReentrantLock lock;         // ReentrantLock = atomic check-then-book; prevents double-booking
    private int rejectedCount;                // guarded by lock; only read/written while holding lock

    public CalendarService() {
        this.events = new TreeMap<>();
        this.lock = new ReentrantLock();
        this.rejectedCount = 0;
    }

    /**
     * Attempt to book. Uses ReentrantLock to atomically check overlap + insert.
     * Returns true if booking succeeded, false if conflict.
     */
    public boolean book(Event event) {
        lock.lock();
        try {
            // Check for overlapping events using TreeMap range queries
            // Look at events that could possibly overlap
            Long floorKey = events.floorKey(event.getEndTime() - 1);
            if (floorKey != null) {
                // Check all events from earliest possible conflicting to the end of our range
                NavigableMap<Long, Event> candidates = events.headMap(event.getEndTime(), false);
                for (Event existing : candidates.values()) {
                    if (existing.overlaps(event.getStartTime(), event.getEndTime())) {
                        rejectedCount++;
                        return false;
                    }
                }
            }
            events.put(event.getStartTime(), event);
            return true;
        } finally {
            lock.unlock();
        }
    }

    public int getBookedCount() {
        lock.lock();
        try {
            return events.size();
        } finally {
            lock.unlock();
        }
    }

    public int getRejectedCount() {
        return rejectedCount;
    }

    public List<Event> getAllEvents() {
        lock.lock();
        try {
            return new ArrayList<>(events.values());
        } finally {
            lock.unlock();
        }
    }
}

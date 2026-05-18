/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/RecurrenceRule.java — Defines how an event repeats (daily, weekly, custom)
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public interface RecurrenceRule {             // interface = pluggable recurrence strategies
    List<TimeSlot> generateOccurrences(TimeSlot original, int count);
    String getDescription();
}

class DailyRecurrence implements RecurrenceRule {
    @Override
    public List<TimeSlot> generateOccurrences(TimeSlot original, int count) {
        List<TimeSlot> occurrences = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            LocalDateTime newStart = original.getStart().plusDays(i);
            LocalDateTime newEnd = original.getEnd().plusDays(i);
            occurrences.add(new TimeSlot(newStart, newEnd));
        }
        return occurrences;
    }

    @Override
    public String getDescription() { return "Daily"; }
}

class WeeklyRecurrence implements RecurrenceRule {
    @Override
    public List<TimeSlot> generateOccurrences(TimeSlot original, int count) {
        List<TimeSlot> occurrences = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            LocalDateTime newStart = original.getStart().plusWeeks(i);
            LocalDateTime newEnd = original.getEnd().plusWeeks(i);
            occurrences.add(new TimeSlot(newStart, newEnd));
        }
        return occurrences;
    }

    @Override
    public String getDescription() { return "Weekly"; }
}

class MonthlyRecurrence implements RecurrenceRule {
    @Override
    public List<TimeSlot> generateOccurrences(TimeSlot original, int count) {
        List<TimeSlot> occurrences = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            LocalDateTime newStart = original.getStart().plusMonths(i);
            LocalDateTime newEnd = original.getEnd().plusMonths(i);
            occurrences.add(new TimeSlot(newStart, newEnd));
        }
        return occurrences;
    }

    @Override
    public String getDescription() { return "Monthly"; }
}

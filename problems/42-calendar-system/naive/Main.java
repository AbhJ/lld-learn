/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating the calendar system
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Calendar System Demo (Naive) ===");

        CalendarService service = new CalendarService();

        Calendar aliceCal = service.createCalendar("alice", "Alice");
        Calendar bobCal = service.createCalendar("bob", "Bob");
        System.out.println("Created calendars for Alice and Bob");

        System.out.println("\n--- Creating Events ---");
        LocalDateTime today = LocalDateTime.of(2026, 5, 13, 0, 0);

        Event standup = new EventBuilder(service.nextEventId())
            .title("Team Standup")
            .description("Daily standup meeting")
            .from(today.withHour(9).withMinute(0))
            .to(today.withHour(9).withMinute(30))
            .organizer("alice")
            .reminder(15)
            .build();
        service.addEvent("alice", standup);

        Event review = new EventBuilder(service.nextEventId())
            .title("Project Review")
            .from(today.withHour(14).withMinute(0))
            .to(today.withHour(15).withMinute(0))
            .organizer("alice")
            .reminder(30)
            .build();
        service.addEvent("alice", review);

        System.out.println("\n--- Recurring Events ---");
        Event dailyStandup = new EventBuilder(service.nextEventId())
            .title("Daily Standup")
            .from(today.withHour(9).withMinute(0))
            .to(today.withHour(9).withMinute(15))
            .organizer("alice")
            .recurring(new DailyRecurrence())
            .build();

        List<TimeSlot> dailySlots = dailyStandup.getOccurrences(5);
        System.out.println("Daily standup recurrences for " + dailySlots.size() + " days");

        System.out.println("\n--- Conflict Detection ---");
        Event morningSync = new EventBuilder(service.nextEventId())
            .title("Morning Sync")
            .from(today.withHour(9).withMinute(15))
            .to(today.withHour(9).withMinute(45))
            .organizer("alice")
            .build();
        service.addEvent("alice", morningSync);

        List<String> conflicts = service.checkConflicts("alice");
        for (String conflict : conflicts) {
            System.out.println(conflict);
        }

        System.out.println("\n--- Invitations ---");
        Invitation inv = service.inviteUser(review.getId(), "alice", "bob", review);
        service.acceptInvitation(inv, review);

        System.out.println("\n--- Reminders ---");
        for (Reminder r : standup.getReminders()) { r.trigger(); }
        for (Reminder r : review.getReminders()) { r.trigger(); }

        System.out.println("\n=== Calendar System Demo Complete ===");
    }
}

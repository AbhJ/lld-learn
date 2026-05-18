/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Reminder.java — Triggers notifications before an event starts
public class Reminder {
    private int minutesBefore;                // private = how far before event to fire
    private String eventTitle;                // private = which event this reminder belongs to
    private boolean triggered;                // private = internal flag prevents double-firing

    public Reminder(int minutesBefore, String eventTitle) {
        this.minutesBefore = minutesBefore;
        this.eventTitle = eventTitle;
        this.triggered = false;
    }

    public int getMinutesBefore() { return minutesBefore; }
    public String getEventTitle() { return eventTitle; }
    public boolean isTriggered() { return triggered; }

    public void trigger() {
        if (!triggered) {
            triggered = true;
            System.out.println("Reminder: " + eventTitle + " starts in " + minutesBefore + " minutes");
        }
    }

    @Override
    public String toString() {
        return "Remind " + minutesBefore + " min before " + eventTitle;
    }
}

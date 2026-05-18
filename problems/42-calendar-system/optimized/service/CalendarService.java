/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/CalendarService.java — Orchestrates event creation and conflict detection
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarService {
    private Map<String, Calendar> calendars;
    private int eventCounter;

    public CalendarService() {
        this.calendars = new HashMap<>();
        this.eventCounter = 0;
    }

    public Calendar createCalendar(String userId, String userName) {
        Calendar calendar = new Calendar(userId, userName);
        calendars.put(userId, calendar);
        return calendar;
    }

    public Calendar getCalendar(String userId) { return calendars.get(userId); }

    public String nextEventId() { return "evt_" + (++eventCounter); }

    public Event addEvent(String userId, Event event) {
        Calendar calendar = calendars.get(userId);
        if (calendar != null) {
            calendar.addEvent(event);
            System.out.println("Event created: " + event);
        }
        return event;
    }

    public Invitation inviteUser(String eventId, String inviterId, String inviteeId, Event event) {
        Invitation invitation = new Invitation(eventId, inviterId, inviteeId);
        event.addInvitation(invitation);
        Calendar inviterCal = calendars.get(inviterId);
        Calendar inviteeCal = calendars.get(inviteeId);
        String inviterName = inviterCal != null ? inviterCal.getUserName() : inviterId;
        String inviteeName = inviteeCal != null ? inviteeCal.getUserName() : inviteeId;
        System.out.println(inviterName + " invited " + inviteeName + " to " + event.getTitle());
        return invitation;
    }

    public void acceptInvitation(Invitation invitation, Event event) {
        invitation.accept();
        event.addAttendee(invitation.getInviteeId());
        Calendar inviteeCal = calendars.get(invitation.getInviteeId());
        if (inviteeCal != null) {
            inviteeCal.addEvent(event);
            System.out.println(inviteeCal.getUserName() + " accepted invitation to " + event.getTitle());
        }
    }

    public List<String> checkConflicts(String userId) {
        Calendar calendar = calendars.get(userId);
        if (calendar != null) {
            return calendar.checkConflicts();
        }
        return List.of();
    }
}

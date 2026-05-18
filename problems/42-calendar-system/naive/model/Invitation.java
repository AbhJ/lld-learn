/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Invitation.java — Represents an event invitation with RSVP status
public class Invitation {
    public enum Status { PENDING, ACCEPTED, DECLINED, TENTATIVE } // enum = fixed RSVP states; type-safe

    private String eventId;                   // private = tracks which event this invitation is for
    private String inviterId;                 // private = who sent the invitation
    private String inviteeId;                 // private = who received the invitation
    private Status status;                    // private = controlled through accept/decline methods

    public Invitation(String eventId, String inviterId, String inviteeId) {
        this.eventId = eventId;
        this.inviterId = inviterId;
        this.inviteeId = inviteeId;
        this.status = Status.PENDING;
    }

    public String getEventId() { return eventId; }
    public String getInviterId() { return inviterId; }
    public String getInviteeId() { return inviteeId; }
    public Status getStatus() { return status; }

    public void accept() { this.status = Status.ACCEPTED; }
    public void decline() { this.status = Status.DECLINED; }
    public void tentative() { this.status = Status.TENTATIVE; }

    @Override
    public String toString() {
        return "Invitation[event=" + eventId + ", status=" + status + "]";
    }
}

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Notification.java — Notification message with recipient, channel, priority, and status

import java.time.LocalDateTime;

public class Notification {
    public enum Status { PENDING, SENT, FAILED, THROTTLED } // enum = fixed set of states; safer than strings

    private String notificationId;                // private = only this class can access; encapsulates data
    private String userId;                        // private = hidden from outside; accessed via getters
    private String recipient; // email/phone/deviceId
    private String channelType;
    private String subject;
    private String body;
    private Priority priority;
    private Status status;
    private LocalDateTime createdAt;
    private int attempts;
    private static int counter = 0;              // static = shared across all instances; class-level ID generator

    public Notification(String userId, String recipient, String channelType,
                        String subject, String body, Priority priority) {
        this.notificationId = "NOTIF-" + (++counter);
        this.userId = userId;
        this.recipient = recipient;
        this.channelType = channelType;
        this.subject = subject;
        this.body = body;
        this.priority = priority;
        this.status = Status.PENDING;
        this.createdAt = LocalDateTime.now();
        this.attempts = 0;
    }

    public void incrementAttempts() { attempts++; }

    public String getNotificationId() { return notificationId; }
    public String getUserId() { return userId; }
    public String getRecipient() { return recipient; }
    public String getChannelType() { return channelType; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public Priority getPriority() { return priority; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public int getAttempts() { return attempts; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override                                     // @Override = compiler checks this matches a parent method
    public String toString() {
        return String.format("[%s][%s] %s -> %s: \"%s\" (%s)",
                channelType, priority, userId, recipient, subject, status);
    }
}

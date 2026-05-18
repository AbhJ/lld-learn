/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Notification.java — Notification event (like, comment, follow) with read status

import java.time.LocalDateTime;

public class Notification {
    public enum Type { NEW_POST, LIKE, COMMENT, FOLLOW } // enum = fixed notification event types

    private String notificationId;  // private = encapsulated unique notification ID
    private String targetUserId;    // private = who receives this notification
    private String sourceUserId;    // private = who triggered this notification
    private String sourceName;      // private = name of the user who triggered it
    private Type type;              // private = what kind of event occurred (enum)
    private String message;         // private = human-readable notification text
    private LocalDateTime createdAt; // private = when notification was generated
    private boolean read;           // private = read/unread status managed internally
    private static int counter = 0; // static = shared counter for unique IDs

    public Notification(String targetUserId, String sourceUserId, String sourceName, Type type, String message) {
        this.notificationId = "NOTIF-" + (++counter);
        this.targetUserId = targetUserId;
        this.sourceUserId = sourceUserId;
        this.sourceName = sourceName;
        this.type = type;
        this.message = message;
        this.createdAt = LocalDateTime.now();
        this.read = false;
    }

    public void markRead() { this.read = true; }

    public String getNotificationId() { return notificationId; }
    public String getTargetUserId() { return targetUserId; }
    public Type getType() { return type; }
    public String getMessage() { return message; }
    public boolean isRead() { return read; }

    @Override
    public String toString() {
        return String.format("[%s] %s%s", type, message, read ? "" : " (NEW)");
    }
}

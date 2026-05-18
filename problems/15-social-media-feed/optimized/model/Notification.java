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

    private String notificationId;  // unique notification ID
    private String targetUserId;    // who receives this notification
    private String sourceUserId;    // who triggered it
    private String sourceName;      // triggerer's name for display
    private Type type;              // event kind (enum)
    private String message;         // human-readable text
    private LocalDateTime createdAt; // when generated
    private boolean read;           // read/unread status
    private static int counter = 0; // shared ID generator

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

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Notification.java — Notification with delivery tracking

import java.util.concurrent.atomic.AtomicLong;

class Notification {
    private static final AtomicLong ID_GEN = new AtomicLong(1); // AtomicLong = thread-safe ID generator; no locks needed

    private final long notificationId;           // final = set once, never changes; safe for threads to read
    private final String userId;                 // final = immutable after construction; safe publication
    private final String message;                // final = guaranteed visible to all threads after constructor
    private final long timestamp;                // final = set once in constructor; thread-safe
    private volatile boolean delivered;          // volatile = writes visible to all threads immediately

    public Notification(String userId, String message) {
        this.notificationId = ID_GEN.getAndIncrement();
        this.userId = userId;
        this.message = message;
        this.timestamp = System.nanoTime();
        this.delivered = false;
    }

    public void markDelivered() { this.delivered = true; }

    public long getNotificationId() { return notificationId; }
    public String getUserId() { return userId; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
    public boolean isDelivered() { return delivered; }

    @Override
    public String toString() {
        return "Notification#" + notificationId + " [" + userId + "] " + message +
                (delivered ? " (DELIVERED)" : " (THROTTLED)");
    }
}

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/NotificationService.java — Orchestrates delivery with priority handling and throttling

import java.util.*;

public class NotificationService {
    private Map<String, Channel> channels;       // private = internal state hidden from outside classes
    private Throttler throttler;                  // private = encapsulates rate-limiting logic
    private List<Notification> history;           // private = only this class manages history

    public NotificationService(int throttleLimit) {
        this.channels = new HashMap<>();
        this.throttler = new Throttler(throttleLimit);
        this.history = new ArrayList<>();
    }

    public void registerChannel(Channel channel) {
        channels.put(channel.getType(), channel);
    }

    public boolean send(Notification notification) {
        history.add(notification);

        // Check throttle
        if (!throttler.allowSend(notification.getUserId(), notification.getChannelType())) {
            notification.setStatus(Notification.Status.THROTTLED);
            System.out.printf("  [THROTTLED] %s rate limit exceeded for user %s on %s%n",
                    notification.getChannelType(), notification.getUserId(), notification.getChannelType());
            return false;
        }

        // Find channel
        Channel channel = channels.get(notification.getChannelType());
        if (channel == null) {
            notification.setStatus(Notification.Status.FAILED);
            System.out.println("  [ERROR] No channel registered for: " + notification.getChannelType());
            return false;
        }

        // Send
        boolean success = channel.send(notification);
        notification.setStatus(success ? Notification.Status.SENT : Notification.Status.FAILED);
        return success;
    }

    public void sendBatch(List<Notification> notifications) {
        // Sort by priority (critical first)
        notifications.sort((a, b) -> b.getPriority().getLevel() - a.getPriority().getLevel());
        for (Notification n : notifications) {
            send(n);
        }
    }

    public Throttler getThrottler() { return throttler; }

    public List<Notification> getHistory() { return Collections.unmodifiableList(history); }

    public List<Notification> getHistoryByStatus(Notification.Status status) {
        List<Notification> result = new ArrayList<>();
        for (Notification n : history) {
            if (n.getStatus() == status) result.add(n);
        }
        return result;
    }

    public long getSentCount() { return getHistoryByStatus(Notification.Status.SENT).size(); }
    public long getFailedCount() { return getHistoryByStatus(Notification.Status.FAILED).size(); }
    public long getThrottledCount() { return getHistoryByStatus(Notification.Status.THROTTLED).size(); }
}

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/NotificationManager.java — Delivers push notifications via per-user MessageListeners
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationManager {
    // Observer: per-user listener registry. Many listeners can subscribe per user
    // (e.g. a console renderer, a mobile push hook, a desktop toast).
    private final Map<String, List<MessageListener>> listeners = new HashMap<>();

    public void addListener(String userId, MessageListener listener) {
        listeners.computeIfAbsent(userId, k -> new ArrayList<>()).add(listener);
    }

    public void removeListener(String userId, MessageListener listener) {
        List<MessageListener> ls = listeners.get(userId);
        if (ls != null) ls.remove(listener);
    }

    public void notifyNewMessage(ChatRoom room, Message message, Map<String, User> users) {
        for (String participantId : room.getParticipantIds()) {
            if (participantId.equals(message.getSenderId())) continue;
            User participant = users.get(participantId);
            if (participant == null || participant.getStatus() == OnlineStatus.OFFLINE) continue;
            List<MessageListener> ls = listeners.get(participantId);
            if (ls == null) continue;
            for (MessageListener l : ls) l.onMessage(message, room);
        }
    }

    public void notifyStatusChange(User user, OnlineStatus oldStatus, OnlineStatus newStatus,
                                    Map<String, User> allUsers) {
        System.out.println(user.getName() + " is now " + newStatus);
    }
}

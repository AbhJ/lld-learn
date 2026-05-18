/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/NotificationManager.java — Async notification delivery via per-user MessageListeners
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationManager {
    private ConcurrentHashMap<String, OnlineStatus> statusCache; // ConcurrentHashMap = O(1) thread-safe status cache

    // Observer: per-user listener registry. CopyOnWriteArrayList tolerates concurrent
    // listener add/remove vs notify without external locking.
    private final ConcurrentHashMap<String, List<MessageListener>> listeners = new ConcurrentHashMap<>();

    public NotificationManager() {
        this.statusCache = new ConcurrentHashMap<>();
    }

    public void addListener(String userId, MessageListener listener) {
        listeners.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    public void removeListener(String userId, MessageListener listener) {
        List<MessageListener> ls = listeners.get(userId);
        if (ls != null) ls.remove(listener);
    }

    public void updateStatusCache(String userId, OnlineStatus status) {
        statusCache.put(userId, status);
    }

    // WHY: O(1) status check from cache avoids traversing user objects
    public boolean isOnline(String userId) {
        OnlineStatus status = statusCache.get(userId);
        return status != null && status != OnlineStatus.OFFLINE;
    }

    public void notifyNewMessage(ChatRoom room, Message message, Map<String, User> users) {
        for (String participantId : room.getParticipantIds()) {
            if (participantId.equals(message.getSenderId())) continue;
            // WHY: CompletableFuture enables async delivery without blocking sender
            CompletableFuture.runAsync(() -> {
                if (!isOnline(participantId)) return;
                List<MessageListener> ls = listeners.get(participantId);
                if (ls == null) return;
                for (MessageListener l : ls) l.onMessage(message, room);
            }).join(); // join() for demo output ordering only
        }
    }

    public void notifyStatusChange(User user, OnlineStatus oldStatus, OnlineStatus newStatus,
                                    Map<String, User> allUsers) {
        updateStatusCache(user.getId(), newStatus);
        System.out.println(user.getName() + " is now " + newStatus);
    }
}

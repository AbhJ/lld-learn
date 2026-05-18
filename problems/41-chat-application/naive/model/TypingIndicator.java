/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/TypingIndicator.java — Tracks which users are currently typing in a room
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TypingIndicator {
    private Map<String, Set<String>> typingUsers; // private = hides internal tracking from outside

    public TypingIndicator() {
        this.typingUsers = new HashMap<>();
    }

    public void startTyping(String roomId, String userId, String userName) {
        typingUsers.computeIfAbsent(roomId, k -> new HashSet<>()).add(userId);
        System.out.println(userName + " is typing in " + roomId + "...");
    }

    public void stopTyping(String roomId, String userId, String userName) {
        Set<String> users = typingUsers.get(roomId);
        if (users != null) {
            users.remove(userId);
            System.out.println(userName + " stopped typing in " + roomId);
        }
    }

    public boolean isTyping(String roomId, String userId) {
        Set<String> users = typingUsers.get(roomId);
        return users != null && users.contains(userId);
    }

    public Set<String> getTypingUsers(String roomId) {
        return typingUsers.getOrDefault(roomId, new HashSet<>());
    }
}

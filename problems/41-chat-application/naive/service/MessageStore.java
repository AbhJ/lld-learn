/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/MessageStore.java — Persists and retrieves message history
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageStore {
    private Map<String, List<Message>> messagesByRoom; // private = storage internals hidden

    public MessageStore() {
        this.messagesByRoom = new HashMap<>();
    }

    public void storeMessage(String roomId, Message message) {
        messagesByRoom.computeIfAbsent(roomId, k -> new ArrayList<>()).add(message);
    }

    public List<Message> getMessages(String roomId) {
        return messagesByRoom.getOrDefault(roomId, new ArrayList<>());
    }

    public List<Message> getMessages(String roomId, int limit) {
        List<Message> messages = getMessages(roomId);
        int start = Math.max(0, messages.size() - limit);
        return messages.subList(start, messages.size());
    }

    public int getMessageCount(String roomId) {
        return messagesByRoom.getOrDefault(roomId, new ArrayList<>()).size();
    }
}

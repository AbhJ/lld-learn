/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/MessageStore.java — Indexed message store with O(1) lookups by user and timestamp
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class MessageStore {
    private Map<String, List<Message>> messagesByRoom;              // primary store: append-only list
    private Map<String, TreeMap<LocalDateTime, Message>> messagesByTimestamp; // TreeMap = O(log n) time-range queries
    private Map<String, Map<String, List<Message>>> messagesByRoomAndSender; // HashMap index = O(1) per-sender lookup

    public MessageStore() {
        this.messagesByRoom = new HashMap<>();
        this.messagesByTimestamp = new HashMap<>();
        this.messagesByRoomAndSender = new HashMap<>();
    }

    public void storeMessage(String roomId, Message message) {
        // Store in primary list
        messagesByRoom.computeIfAbsent(roomId, k -> new ArrayList<>()).add(message);

        // Index by timestamp for range queries
        messagesByTimestamp.computeIfAbsent(roomId, k -> new TreeMap<>())
            .put(message.getTimestamp(), message);

        // Index by sender for user-specific queries
        messagesByRoomAndSender
            .computeIfAbsent(roomId, k -> new HashMap<>())
            .computeIfAbsent(message.getSenderId(), k -> new ArrayList<>())
            .add(message);
    }

    public List<Message> getMessages(String roomId) {
        return messagesByRoom.getOrDefault(roomId, new ArrayList<>());
    }

    public List<Message> getMessages(String roomId, int limit) {
        List<Message> messages = getMessages(roomId);
        int start = Math.max(0, messages.size() - limit);
        return messages.subList(start, messages.size());
    }

    // WHY: O(log n) range query instead of scanning all messages
    public List<Message> getMessagesBetween(String roomId, LocalDateTime from, LocalDateTime to) {
        TreeMap<LocalDateTime, Message> timeIndex = messagesByTimestamp.get(roomId);
        if (timeIndex == null) return new ArrayList<>();
        return new ArrayList<>(timeIndex.subMap(from, true, to, true).values());
    }

    // WHY: O(1) lookup for messages by a specific sender
    public List<Message> getMessagesBySender(String roomId, String senderId) {
        Map<String, List<Message>> senderIndex = messagesByRoomAndSender.get(roomId);
        if (senderIndex == null) return new ArrayList<>();
        return senderIndex.getOrDefault(senderId, new ArrayList<>());
    }

    public int getMessageCount(String roomId) {
        return messagesByRoom.getOrDefault(roomId, new ArrayList<>()).size();
    }
}

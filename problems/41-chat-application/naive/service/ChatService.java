/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/ChatService.java — Manages user sessions, rooms, and message routing
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatService {
    private Map<String, User> users;              // private = internal registry hidden from outside
    private Map<String, ChatRoom> chatRooms;      // private = rooms managed only through service methods
    private MessageStore messageStore;            // private = storage is an implementation detail
    private NotificationManager notificationManager; // private = notification logic encapsulated
    private TypingIndicator typingIndicator;      // private = typing state is internal concern
    private int messageCounter;                   // private = auto-increment counter for unique IDs
    private int roomCounter;                      // private = auto-increment counter for room IDs

    public ChatService() {
        this.users = new HashMap<>();
        this.chatRooms = new HashMap<>();
        this.messageStore = new MessageStore();
        this.notificationManager = new NotificationManager();
        this.typingIndicator = new TypingIndicator();
        this.messageCounter = 0;
        this.roomCounter = 0;
    }

    public User registerUser(String name) {
        String id = "user_" + (users.size() + 1);
        User user = new User(id, name);
        users.put(id, user);
        // Register a default console listener so the demo shows event delivery.
        notificationManager.addListener(id, new ConsoleMessageListener(name));
        return user;
    }

    /** Register an additional listener for a user (push, toast, email, etc.). */
    public void addMessageListener(String userId, MessageListener listener) {
        notificationManager.addListener(userId, listener);
    }

    public void setUserStatus(String userId, OnlineStatus status) {
        User user = users.get(userId);
        if (user != null) {
            OnlineStatus oldStatus = user.getStatus();
            user.setStatus(status);
            notificationManager.notifyStatusChange(user, oldStatus, status, users);
        }
    }

    public ChatRoom createDirectChat(String user1Id, String user2Id) {
        for (ChatRoom room : chatRooms.values()) {
            if (room instanceof DirectChat &&
                room.hasParticipant(user1Id) && room.hasParticipant(user2Id)) {
                return room;
            }
        }
        String roomId = "room_" + (++roomCounter);
        DirectChat chat = new DirectChat(roomId, user1Id, user2Id);
        chatRooms.put(roomId, chat);
        users.get(user1Id).joinChatRoom(roomId);
        users.get(user2Id).joinChatRoom(roomId);
        return chat;
    }

    public ChatRoom createGroupChat(String name, String creatorId, List<String> memberIds) {
        String roomId = "room_" + (++roomCounter);
        GroupChat group = new GroupChat(roomId, name, creatorId);
        for (String memberId : memberIds) {
            group.addParticipant(memberId);
            users.get(memberId).joinChatRoom(roomId);
        }
        chatRooms.put(roomId, group);
        System.out.println("Group '" + name + "' created with " +
            String.join(", ", memberIds.stream()
                .map(id -> users.get(id).getName())
                .toArray(String[]::new)));
        return group;
    }

    public Message sendMessage(String roomId, String senderId, String content) {
        return sendMessage(roomId, senderId, content, Message.MessageType.TEXT);
    }

    public Message sendMessage(String roomId, String senderId, String content, Message.MessageType type) {
        ChatRoom room = chatRooms.get(roomId);
        if (room == null || !room.hasParticipant(senderId)) {
            throw new IllegalArgumentException("Invalid room or user not a participant");
        }

        String msgId = "msg_" + (++messageCounter);
        Message message;
        switch (type) {
            case IMAGE:
                message = Message.createImageMessage(msgId, senderId, content);
                break;
            case FILE:
                message = Message.createFileMessage(msgId, senderId, content);
                break;
            default:
                message = Message.createTextMessage(msgId, senderId, content);
        }

        messageStore.storeMessage(roomId, message);
        String senderName = users.get(senderId).getName();

        if (room instanceof GroupChat) {
            System.out.println(senderName + " sends to group: " + message.getDisplayText());
        } else {
            System.out.println(senderName + " sends: " + message.getDisplayText());
        }

        notificationManager.notifyNewMessage(room, message, users);
        return message;
    }

    public List<Message> getMessageHistory(String roomId) {
        return messageStore.getMessages(roomId);
    }

    public void startTyping(String roomId, String userId) {
        User user = users.get(userId);
        ChatRoom room = chatRooms.get(roomId);
        if (user != null && room != null) {
            typingIndicator.startTyping(room.getName(), userId, user.getName());
        }
    }

    public void stopTyping(String roomId, String userId) {
        User user = users.get(userId);
        ChatRoom room = chatRooms.get(roomId);
        if (user != null && room != null) {
            typingIndicator.stopTyping(room.getName(), userId, user.getName());
        }
    }

    public User getUser(String userId) { return users.get(userId); }
    public ChatRoom getChatRoom(String roomId) { return chatRooms.get(roomId); }
}

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/User.java — Represents a chat user with profile and status
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class User {
    private String id;
    private String name;
    private volatile OnlineStatus status;      // volatile = status reads always see latest write
    private List<String> chatRoomIds;
    // CopyOnWriteArrayList = safe iteration during notification delivery; rare writes
    private CopyOnWriteArrayList<String> notifications;

    public User(String id, String name) {
        this.id = id;
        this.name = name;
        this.status = OnlineStatus.OFFLINE;
        this.chatRoomIds = new ArrayList<>();
        this.notifications = new CopyOnWriteArrayList<>();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public OnlineStatus getStatus() { return status; }
    public List<String> getChatRoomIds() { return chatRoomIds; }

    public void setStatus(OnlineStatus status) {
        this.status = status;
    }

    public void joinChatRoom(String roomId) {
        if (!chatRoomIds.contains(roomId)) {
            chatRoomIds.add(roomId);
        }
    }

    public void leaveChatRoom(String roomId) {
        chatRoomIds.remove(roomId);
    }

    public void receiveNotification(String notification) {
        notifications.add(notification);
        System.out.println(name + " receives notification: " + notification);
    }

    public List<String> getNotifications() {
        return new ArrayList<>(notifications);
    }

    public void clearNotifications() {
        notifications.clear();
    }

    @Override
    public String toString() {
        return name + " (" + status + ")";
    }
}

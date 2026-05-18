/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/ChatRoom.java — Manages participants and message broadcast within a room
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class ChatRoom {
    protected String id;
    protected String name;
    protected Set<String> participantIds;      // HashSet = O(1) contains/add vs O(n) ArrayList scan

    public ChatRoom(String id, String name) {
        this.id = id;
        this.name = name;
        this.participantIds = new HashSet<>();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public List<String> getParticipantIds() { return new ArrayList<>(participantIds); }

    public void addParticipant(String userId) {
        participantIds.add(userId);
    }

    public void removeParticipant(String userId) {
        participantIds.remove(userId);
    }

    public boolean hasParticipant(String userId) {
        return participantIds.contains(userId);
    }

    public abstract String getRoomType();
}

class DirectChat extends ChatRoom {
    public DirectChat(String id, String user1Id, String user2Id) {
        super(id, "Direct:" + user1Id + "-" + user2Id);
        addParticipant(user1Id);
        addParticipant(user2Id);
    }

    @Override
    public void addParticipant(String userId) {
        if (participantIds.size() < 2) {
            super.addParticipant(userId);
        }
    }

    @Override
    public String getRoomType() {
        return "DIRECT";
    }
}

class GroupChat extends ChatRoom {
    private String creatorId;

    public GroupChat(String id, String name, String creatorId) {
        super(id, name);
        this.creatorId = creatorId;
        addParticipant(creatorId);
    }

    public String getCreatorId() { return creatorId; }

    @Override
    public String getRoomType() {
        return "GROUP";
    }
}

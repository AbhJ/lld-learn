/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/ChatRoom.java — Manages participants and message broadcast within a room
import java.util.ArrayList;
import java.util.List;

public abstract class ChatRoom {              // abstract = can't create generic room; must be Direct or Group
    protected String id;                      // protected = subclasses (DirectChat, GroupChat) can access
    protected String name;                    // protected = shared with subclasses but hidden from others
    protected List<String> participantIds;    // protected = subclasses manage their own participants

    public ChatRoom(String id, String name) {
        this.id = id;
        this.name = name;
        this.participantIds = new ArrayList<>();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public List<String> getParticipantIds() { return new ArrayList<>(participantIds); }

    public void addParticipant(String userId) {
        if (!participantIds.contains(userId)) {
            participantIds.add(userId);
        }
    }

    public void removeParticipant(String userId) {
        participantIds.remove(userId);
    }

    public boolean hasParticipant(String userId) {
        return participantIds.contains(userId);
    }

    public abstract String getRoomType();      // abstract = each room subclass identifies itself
}

class DirectChat extends ChatRoom {           // extends = inherits ChatRoom; adds 2-person limit
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

class GroupChat extends ChatRoom {            // extends = inherits ChatRoom; allows many participants
    private String creatorId;                 // private = only GroupChat knows who created it

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

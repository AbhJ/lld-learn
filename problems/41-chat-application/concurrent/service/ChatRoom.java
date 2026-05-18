/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/ChatRoom.java — Thread-safe chat room with ConcurrentLinkedQueue and AtomicLong sequencing

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

public class ChatRoom {
    private final String roomId;              // final = safe publication; all threads see this after construction
    private final ConcurrentLinkedQueue<Message> messageBuffer; // ConcurrentLinkedQueue = lock-free adds from many threads
    private final AtomicLong sequenceGenerator; // AtomicLong = CAS-based increment; unique sequence without locks

    public ChatRoom(String roomId) {
        this.roomId = roomId;
        this.messageBuffer = new ConcurrentLinkedQueue<>();
        this.sequenceGenerator = new AtomicLong(0);
    }

    /**
     * Send a message to this chat room. Thread-safe via AtomicLong for sequence
     * and ConcurrentLinkedQueue for buffer.
     */
    public Message sendMessage(String senderId, String content) {
        long seq = sequenceGenerator.incrementAndGet();
        Message msg = new Message(senderId, content, seq);
        messageBuffer.add(msg);
        return msg;
    }

    /**
     * Get all messages sorted by sequence number (total ordering).
     */
    public List<Message> getMessagesInOrder() {
        List<Message> messages = new ArrayList<>(messageBuffer);
        messages.sort(Comparator.comparingLong(Message::getSequenceNumber));
        return messages;
    }

    public int getMessageCount() {
        return messageBuffer.size();
    }

    public String getRoomId() { return roomId; }
}

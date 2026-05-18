/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Message.java — Immutable message with sequence number for total ordering

public class Message {
    private final String senderId;            // final = immutable after construction; safe to share across threads
    private final String content;             // final = no thread can modify after creation
    private final long sequenceNumber;        // final = assigned once by AtomicLong; globally unique ordering
    private final long timestamp;             // final = captured at creation; safe publication guaranteed

    public Message(String senderId, String content, long sequenceNumber) {
        this.senderId = senderId;
        this.content = content;
        this.sequenceNumber = sequenceNumber;
        this.timestamp = System.nanoTime();
    }

    public String getSenderId() { return senderId; }
    public String getContent() { return content; }
    public long getSequenceNumber() { return sequenceNumber; }
    public long getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "[#" + sequenceNumber + " from " + senderId + "]: " + content;
    }
}

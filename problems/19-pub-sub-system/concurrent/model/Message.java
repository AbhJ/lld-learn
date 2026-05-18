/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Message.java — Immutable message with atomic sequence number

import java.util.concurrent.atomic.AtomicLong;

class Message {
    private static final AtomicLong SEQ_GEN = new AtomicLong(1); // AtomicLong = thread-safe unique sequence generator

    private final long sequenceNumber;           // final = immutable after construction; safe publication
    private final String topicName;              // final = set once; threads can read without synchronization
    private final String payload;                // final = immutable; safe to share across threads
    private final String publisherId;            // final = safe publication to subscriber threads
    private final long timestamp;                // final = set once in constructor

    public Message(String topicName, String payload, String publisherId) {
        this.sequenceNumber = SEQ_GEN.getAndIncrement();
        this.topicName = topicName;
        this.payload = payload;
        this.publisherId = publisherId;
        this.timestamp = System.nanoTime();
    }

    public long getSequenceNumber() { return sequenceNumber; }
    public String getTopicName() { return topicName; }
    public String getPayload() { return payload; }
    public String getPublisherId() { return publisherId; }
    public long getTimestamp() { return timestamp; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;
        return sequenceNumber == ((Message) o).sequenceNumber;
    }

    @Override
    public int hashCode() { return Long.hashCode(sequenceNumber); }

    @Override
    public String toString() {
        return "Msg#" + sequenceNumber + " [" + topicName + "] from " + publisherId + ": " + payload;
    }
}

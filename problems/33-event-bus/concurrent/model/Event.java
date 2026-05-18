/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Event.java — Simple event with topic and payload

public class Event {
    private final String topic;    // final = immutable; safe to read from any thread without sync
    private final String payload;  // final = set once at construction; guarantees safe publication
    private final long timestamp;

    public Event(String topic, String payload) {
        this.topic = topic;
        this.payload = payload;
        this.timestamp = System.nanoTime();
    }

    public String getTopic() { return topic; }
    public String getPayload() { return payload; }
    public long getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "Event[" + topic + ": " + payload + "]";
    }
}

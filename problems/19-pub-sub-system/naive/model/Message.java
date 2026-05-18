/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Message.java — Message with payload, headers, and topic assignment

import java.time.LocalDateTime;
import java.util.*;

public class Message {
    private String messageId;                    // private = only this class manages message identity
    private String topic;
    private String payload;
    private Map<String, String> headers;
    private LocalDateTime timestamp;
    private static int counter = 0;              // static = shared ID counter across all Message instances

    public Message(String topic, String payload) {
        this.messageId = "MSG-" + (++counter);
        this.topic = topic;
        this.payload = payload;
        this.headers = new HashMap<>();
        this.timestamp = LocalDateTime.now();
    }

    public Message(String topic, String payload, Map<String, String> headers) {
        this(topic, payload);
        if (headers != null) this.headers.putAll(headers);
    }

    public String getMessageId() { return messageId; }
    public String getTopic() { return topic; }
    public String getPayload() { return payload; }
    public Map<String, String> getHeaders() { return Collections.unmodifiableMap(headers); }
    public LocalDateTime getTimestamp() { return timestamp; }

    public void addHeader(String key, String value) { headers.put(key, value); }
    public String getHeader(String key) { return headers.get(key); }

    @Override
    public String toString() {
        return String.format("Message[%s] topic=%s: %s", messageId, topic, payload);
    }
}

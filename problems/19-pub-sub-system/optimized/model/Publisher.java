/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Publisher.java — Message producer that sends to topics through the broker

import java.util.Map;

public class Publisher {
    private String publisherId;
    private String name;
    private MessageBroker broker;

    public Publisher(String publisherId, String name, MessageBroker broker) {
        this.publisherId = publisherId;
        this.name = name;
        this.broker = broker;
    }

    public Message publish(String topic, String payload) {
        Message message = new Message(topic, payload);
        message.addHeader("publisher", publisherId);
        broker.publish(topic, message);
        return message;
    }

    public Message publish(String topic, String payload, Map<String, String> headers) {
        Message message = new Message(topic, payload, headers);
        message.addHeader("publisher", publisherId);
        broker.publish(topic, message);
        return message;
    }

    public String getPublisherId() { return publisherId; }
    public String getName() { return name; }

    @Override
    public String toString() { return "Publisher[" + name + "]"; }
}

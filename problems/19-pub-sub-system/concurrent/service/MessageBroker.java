/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/MessageBroker.java — Thread-safe message broker with CopyOnWriteArrayList subscribers

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Thread-safe message broker.
 *
 * Race condition solved: Publisher publishes while new subscriber is being added.
 * CopyOnWriteArrayList for subscriber list means publish iterates a snapshot —
 * new subscribers added mid-publish get the NEXT message, not a partial state.
 * AtomicLong for sequence numbers guarantees ordering.
 */
class MessageBroker {
    private final ConcurrentHashMap<String, Topic> topics = new ConcurrentHashMap<>(); // ConcurrentHashMap = thread-safe topic registry; lock-free reads
    private final AtomicLong publishedCount = new AtomicLong(0); // AtomicLong = thread-safe counter; no locks needed

    public Topic getOrCreateTopic(String topicName) {
        return topics.computeIfAbsent(topicName, Topic::new);
    }

    public void subscribe(String topicName, Consumer<Message> subscriber) {
        getOrCreateTopic(topicName).subscribe(subscriber);
    }

    public void unsubscribe(String topicName, Consumer<Message> subscriber) {
        Topic topic = topics.get(topicName);
        if (topic != null) {
            topic.unsubscribe(subscriber);
        }
    }

    /**
     * Publish a message to a topic. Thread-safe.
     * Message gets an atomic sequence number for ordering guarantee.
     */
    public Message publish(String topicName, String payload, String publisherId) {
        Topic topic = getOrCreateTopic(topicName);
        Message message = new Message(topicName, payload, publisherId);
        topic.publish(message);
        publishedCount.incrementAndGet();
        return message;
    }

    public long getPublishedCount() { return publishedCount.get(); }
    public Topic getTopic(String topicName) { return topics.get(topicName); }
    public int getTopicCount() { return topics.size(); }
}

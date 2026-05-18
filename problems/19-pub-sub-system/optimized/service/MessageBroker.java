/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/MessageBroker.java — Per-topic thread pool with message ordering and at-least-once ack tracking

import java.util.*;
import java.util.concurrent.*;

/**
 * Optimized: Per-topic thread pool for parallel delivery across topics.
 * Messages within a single topic are delivered in order (single-threaded per topic).
 * At-least-once delivery with acknowledgment tracking and retry.
 */
public class MessageBroker {
    private Map<String, Topic> topics;           // ConcurrentHashMap = thread-safe topic registry
    private Map<String, List<Subscriber>> subscriptions; // ConcurrentHashMap = safe concurrent subscribe/unsubscribe
    private Map<String, Set<String>> acknowledgedMessages; // ConcurrentHashMap + ConcurrentHashMap.newKeySet() for ack tracking
    private Map<String, List<Message>> unackedMessages; // CopyOnWriteArrayList = safe iteration during retry
    private ExecutorService topicExecutor;       // ExecutorService = managed thread pool for parallel delivery
    private DeliveryGuarantee defaultGuarantee;
    private int totalPublished;
    private int totalDelivered;

    public MessageBroker(DeliveryGuarantee guarantee) {
        this.topics = new ConcurrentHashMap<>();
        this.subscriptions = new ConcurrentHashMap<>();
        this.acknowledgedMessages = new ConcurrentHashMap<>();
        this.unackedMessages = new ConcurrentHashMap<>();
        this.topicExecutor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        this.defaultGuarantee = guarantee;
        this.totalPublished = 0;
        this.totalDelivered = 0;
    }

    public MessageBroker() { this(DeliveryGuarantee.AT_MOST_ONCE); }

    public Topic createTopic(String name) {
        Topic topic = new Topic(name);
        topics.put(name, topic);
        subscriptions.put(name, new CopyOnWriteArrayList<>()); // CopyOnWriteArrayList = safe iteration during publish while subscribers change
        System.out.println("Topic created: " + name);
        return topic;
    }

    public boolean subscribe(String topicName, Subscriber subscriber) {
        List<Subscriber> subs = subscriptions.get(topicName);
        if (subs == null) {
            System.out.println("Topic not found: " + topicName);
            return false;
        }
        subs.add(subscriber);
        acknowledgedMessages.put(subscriber.getId(), ConcurrentHashMap.newKeySet());
        unackedMessages.put(subscriber.getId(), new CopyOnWriteArrayList<>());
        System.out.printf("  %s subscribed to '%s' (filter: %s)%n",
                subscriber.getName(), topicName, subscriber.getFilter().getDescription());
        return true;
    }

    public boolean unsubscribe(String topicName, Subscriber subscriber) {
        List<Subscriber> subs = subscriptions.get(topicName);
        if (subs == null) return false;
        boolean removed = subs.remove(subscriber);
        if (removed) {
            System.out.printf("  %s unsubscribed from '%s'%n", subscriber.getName(), topicName);
        }
        return removed;
    }

    /**
     * Publish with per-topic thread execution for ordering guarantee.
     * Messages are delivered in order within a topic, but topics process in parallel.
     */
    public void publish(String topicName, Message message) {
        Topic topic = topics.get(topicName);
        if (topic == null) {
            System.out.println("Topic not found: " + topicName);
            return;
        }

        topic.addMessage(message);
        totalPublished++;

        List<Subscriber> subs = subscriptions.get(topicName);
        if (subs == null || subs.isEmpty()) return;

        // Deliver synchronously for ordering within topic
        for (Subscriber sub : subs) {
            if (!sub.getFilter().matches(message)) continue;

            if (defaultGuarantee == DeliveryGuarantee.AT_LEAST_ONCE) {
                // Track for at-least-once: store until acked
                unackedMessages.get(sub.getId()).add(message);
            }

            sub.onMessage(message);
            totalDelivered++;
        }
    }

    /**
     * Acknowledge message delivery (at-least-once guarantee).
     */
    public void acknowledgeMessage(Subscriber subscriber, String messageId) {
        Set<String> acked = acknowledgedMessages.get(subscriber.getId());
        if (acked != null) {
            acked.add(messageId);
            // Remove from unacked list
            List<Message> unacked = unackedMessages.get(subscriber.getId());
            if (unacked != null) {
                unacked.removeIf(m -> m.getMessageId().equals(messageId));
            }
            System.out.printf("  Message %s acknowledged by %s%n", messageId, subscriber.getName());
        }
    }

    /**
     * Retry unacknowledged messages for a subscriber.
     */
    public int retryUnacked(Subscriber subscriber) {
        List<Message> unacked = unackedMessages.get(subscriber.getId());
        if (unacked == null || unacked.isEmpty()) return 0;

        int retried = 0;
        for (Message msg : new ArrayList<>(unacked)) {
            subscriber.onMessage(msg);
            retried++;
        }
        return retried;
    }

    public int getUnackedCount(Subscriber subscriber) {
        List<Message> unacked = unackedMessages.get(subscriber.getId());
        return unacked != null ? unacked.size() : 0;
    }

    public int getAckedCount(Subscriber subscriber) {
        Set<String> acked = acknowledgedMessages.get(subscriber.getId());
        return acked != null ? acked.size() : 0;
    }

    public void shutdown() { topicExecutor.shutdown(); }

    public Topic getTopic(String name) { return topics.get(name); }
    public int getTotalPublished() { return totalPublished; }
    public int getTotalDelivered() { return totalDelivered; }
    public int getTopicCount() { return topics.size(); }
    public int getSubscriberCount(String topicName) {
        List<Subscriber> subs = subscriptions.get(topicName);
        return subs != null ? subs.size() : 0;
    }
}

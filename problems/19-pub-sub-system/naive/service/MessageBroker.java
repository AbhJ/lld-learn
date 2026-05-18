/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/MessageBroker.java — Central message router managing topics, subscriptions, and delivery

import java.util.*;

public class MessageBroker {
    private Map<String, Topic> topics;           // private = internal topic registry
    private Map<String, List<Subscriber>> subscriptions; // topicName -> subscribers
    private Map<String, MessageQueue> queues; // subscriberId -> queue
    private DeliveryGuarantee defaultGuarantee;   // private = delivery mode encapsulated in broker
    private int totalPublished;
    private int totalDelivered;

    public MessageBroker(DeliveryGuarantee guarantee) {
        this.topics = new HashMap<>();
        this.subscriptions = new HashMap<>();
        this.queues = new HashMap<>();
        this.defaultGuarantee = guarantee;
        this.totalPublished = 0;
        this.totalDelivered = 0;
    }

    public MessageBroker() {
        this(DeliveryGuarantee.AT_MOST_ONCE);
    }

    public Topic createTopic(String name) {
        Topic topic = new Topic(name);
        topics.put(name, topic);
        subscriptions.put(name, new ArrayList<>());
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
        queues.put(subscriber.getId(), new MessageQueue(subscriber.getId(), 1000));
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

        for (Subscriber sub : subs) {
            // Apply filter
            if (!sub.getFilter().matches(message)) continue;

            // Deliver based on guarantee
            if (defaultGuarantee == DeliveryGuarantee.AT_LEAST_ONCE) {
                MessageQueue queue = queues.get(sub.getId());
                if (queue != null) {
                    queue.enqueue(message);
                }
            }

            sub.onMessage(message);
            totalDelivered++;
        }
    }

    public void acknowledgeMessage(Subscriber subscriber, String messageId) {
        MessageQueue queue = queues.get(subscriber.getId());
        if (queue != null) {
            queue.acknowledge(messageId);
            System.out.printf("  Message %s acknowledged by %s%n", messageId, subscriber.getName());
        }
    }

    public MessageQueue getQueue(Subscriber subscriber) {
        return queues.get(subscriber.getId());
    }

    public Topic getTopic(String name) { return topics.get(name); }
    public int getTotalPublished() { return totalPublished; }
    public int getTotalDelivered() { return totalDelivered; }
    public int getTopicCount() { return topics.size(); }
    public int getSubscriberCount(String topicName) {
        List<Subscriber> subs = subscriptions.get(topicName);
        return subs != null ? subs.size() : 0;
    }
}

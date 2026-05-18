/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Topic.java — Topic with thread-safe subscriber list and message history

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 * Thread-safe topic using CopyOnWriteArrayList for subscriber list.
 * Safe to publish while subscribers are being added/removed.
 */
class Topic {
    private final String name;                   // final = immutable; safe for threads to read
    private final CopyOnWriteArrayList<Consumer<Message>> subscribers = new CopyOnWriteArrayList<>(); // CopyOnWriteArrayList = snapshot iteration; safe during concurrent add/remove
    private final ConcurrentLinkedQueue<Message> messageHistory = new ConcurrentLinkedQueue<>(); // ConcurrentLinkedQueue = lock-free append; no contention between publishers

    public Topic(String name) {
        this.name = name;
    }

    /**
     * Subscribe — thread-safe addition to subscriber list.
     * Does NOT affect in-progress publish iterations (copy-on-write).
     */
    public void subscribe(Consumer<Message> subscriber) {
        subscribers.add(subscriber);
    }

    /**
     * Unsubscribe — thread-safe removal from subscriber list.
     */
    public void unsubscribe(Consumer<Message> subscriber) {
        subscribers.remove(subscriber);
    }

    /**
     * Publish message to all current subscribers.
     * CopyOnWriteArrayList ensures iteration is safe even during concurrent subscribe/unsubscribe.
     */
    public void publish(Message message) {
        messageHistory.offer(message);
        // Iteration over CopyOnWriteArrayList is snapshot-based
        for (Consumer<Message> subscriber : subscribers) {
            subscriber.accept(message);
        }
    }

    public String getName() { return name; }
    public int getSubscriberCount() { return subscribers.size(); }
    public int getMessageCount() { return messageHistory.size(); }
    public ConcurrentLinkedQueue<Message> getMessageHistory() { return messageHistory; }
}

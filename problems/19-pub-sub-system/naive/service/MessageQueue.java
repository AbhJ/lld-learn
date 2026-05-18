/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/MessageQueue.java — Per-subscriber buffer with ordered delivery and ack tracking

import java.util.*;

public class MessageQueue {
    private String subscriberId;                 // private = internal subscriber identity
    private Queue<Message> queue;                // private = FIFO delivery order maintained internally
    private Set<String> acknowledgedIds;         // private = tracks which messages were acked
    private int maxSize;

    public MessageQueue(String subscriberId, int maxSize) {
        this.subscriberId = subscriberId;
        this.queue = new LinkedList<>();
        this.acknowledgedIds = new HashSet<>();
        this.maxSize = maxSize;
    }

    public boolean enqueue(Message message) {
        if (queue.size() >= maxSize) {
            return false; // Queue full, message dropped
        }
        queue.offer(message);
        return true;
    }

    public Message dequeue() {
        return queue.poll();
    }

    public Message peek() {
        return queue.peek();
    }

    public void acknowledge(String messageId) {
        acknowledgedIds.add(messageId);
    }

    public boolean isAcknowledged(String messageId) {
        return acknowledgedIds.contains(messageId);
    }

    public int size() { return queue.size(); }
    public boolean isEmpty() { return queue.isEmpty(); }
    public String getSubscriberId() { return subscriberId; }
    public int getAcknowledgedCount() { return acknowledgedIds.size(); }
}

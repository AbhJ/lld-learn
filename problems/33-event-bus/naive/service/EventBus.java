/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/EventBus.java — Central hub with ArrayList of handlers, synchronous dispatch
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventBus {
    private final List<Subscription> subscriptions = new ArrayList<>(); // private = internal registry of handlers
    private final DeadLetterQueue deadLetterQueue = new DeadLetterQueue(); // stores events with no matching handler

    public void subscribe(EventHandler handler, Class<? extends Event> eventType) {
        subscriptions.add(new Subscription(handler, eventType));
    }

    public void subscribe(EventHandler handler, Class<? extends Event> eventType, int priority) {
        subscriptions.add(new Subscription(handler, eventType, priority));
    }

    public void subscribe(EventHandler handler, Class<? extends Event> eventType, int priority, EventFilter filter) {
        subscriptions.add(new Subscription(handler, eventType, priority, filter));
    }

    public void unsubscribe(EventHandler handler) {
        subscriptions.removeIf(s -> s.getHandler() == handler);
    }

    public void publish(Event event) {
        List<Subscription> matching = new ArrayList<>();
        for (Subscription sub : subscriptions) {
            if (sub.matches(event)) matching.add(sub);
        }
        if (matching.isEmpty()) {
            deadLetterQueue.add(event);
            return;
        }
        Collections.sort(matching);
        for (Subscription sub : matching) {
            sub.getHandler().handle(event);
        }
    }

    public DeadLetterQueue getDeadLetterQueue() { return deadLetterQueue; }
    public int getSubscriptionCount() { return subscriptions.size(); }
}

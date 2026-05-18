/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/ConcurrentEventBus.java — CopyOnWriteArrayList for handler list (snapshot iteration)

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentEventBus {
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<EventHandler>> handlers = new ConcurrentHashMap<>(); // ConcurrentHashMap = lock-free reads; CopyOnWriteArrayList = no ConcurrentModificationException during iteration
    private final AtomicInteger eventsPublished = new AtomicInteger(0); // AtomicInteger = thread-safe counter without locks
    private final AtomicInteger handlersInvoked = new AtomicInteger(0);

    /**
     * Subscribe a handler to a topic. Uses CopyOnWriteArrayList so that
     * publishing (iteration) never sees a partially-constructed list.
     */
    public void subscribe(String topic, EventHandler handler) {
        handlers.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>()).add(handler);
    }

    /**
     * Publish an event to all current subscribers. CopyOnWriteArrayList
     * guarantees snapshot iteration — no ConcurrentModificationException
     * even if new handlers are being registered simultaneously.
     */
    public void publish(Event event) {
        eventsPublished.incrementAndGet();
        CopyOnWriteArrayList<EventHandler> topicHandlers = handlers.get(event.getTopic());
        if (topicHandlers != null) {
            for (EventHandler handler : topicHandlers) {
                handler.handle(event);
                handlersInvoked.incrementAndGet();
            }
        }
    }

    public int getEventsPublished() { return eventsPublished.get(); }
    public int getHandlersInvoked() { return handlersInvoked.get(); }
    public int getSubscriberCount(String topic) {
        CopyOnWriteArrayList<EventHandler> list = handlers.get(topic);
        return list == null ? 0 : list.size();
    }
}

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Subscription.java — Tracks a handler's registration with filter and priority
public class Subscription implements Comparable<Subscription> { // implements Comparable = enables sorting by priority
    private final EventHandler handler;                 // private final = bound at creation, never swapped
    private final int priority;
    private final EventFilter filter;                   // filter may be null = optional narrowing of events
    private final Class<? extends Event> eventType;     // Class<? extends Event> = wildcard; matches Event or any subclass

    public Subscription(EventHandler handler, Class<? extends Event> eventType, int priority, EventFilter filter) {
        this.handler = handler;
        this.eventType = eventType;
        this.priority = priority;
        this.filter = filter;
    }

    public Subscription(EventHandler handler, Class<? extends Event> eventType, int priority) {
        this(handler, eventType, priority, null);
    }

    public Subscription(EventHandler handler, Class<? extends Event> eventType) {
        this(handler, eventType, 0, null);
    }

    public EventHandler getHandler() { return handler; }
    public int getPriority() { return priority; }
    public EventFilter getFilter() { return filter; }
    public Class<? extends Event> getEventType() { return eventType; }

    public boolean matches(Event event) {
        if (!eventType.isAssignableFrom(event.getClass())) return false;
        if (filter != null && !filter.accept(event)) return false;
        return true;
    }

    @Override
    public int compareTo(Subscription other) {
        return Integer.compare(other.priority, this.priority);
    }
}

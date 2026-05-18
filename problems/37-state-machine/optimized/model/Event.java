/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Event.java — Trigger that causes state transitions
public class Event {
    private final String name;             // final = event name immutable after creation
    private final Object payload;          // final = extra data attached to event

    public Event(String name) { this(name, null); }
    public Event(String name, Object payload) { this.name = name; this.payload = payload; }

    public String getName() { return name; }
    public Object getPayload() { return payload; }
    @Override public String toString() { return name; }
}

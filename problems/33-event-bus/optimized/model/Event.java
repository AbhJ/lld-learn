/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Event.java — Base event with type hierarchy for type-safe dispatch
public class Event {
    private final String type;      // final = immutable; safe to dispatch to multiple handlers
    private final long timestamp;

    public Event(String type) {
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

    public String getType() { return type; }
    public long getTimestamp() { return timestamp; }

    @Override
    public String toString() { return getClass().getSimpleName() + "[" + type + "]"; }
}

class UserEvent extends Event { // extends = subtype enables type-safe dispatch via class hierarchy
    private final String username;
    public UserEvent(String type, String username) { super(type); this.username = username; }
    public String getUsername() { return username; }
    @Override public String toString() { return "UserEvent[" + getType() + ", user=" + username + "]"; }
}

class SystemEvent extends Event { // extends = another subtype; handlers can subscribe per-type
    private final String message;
    public SystemEvent(String type, String message) { super(type); this.message = message; }
    public String getMessage() { return message; }
    @Override public String toString() { return "SystemEvent[" + getType() + ": " + message + "]"; }
}

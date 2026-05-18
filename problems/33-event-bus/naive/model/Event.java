/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Event.java — Base event with type, timestamp, and optional source
public class Event {
    private final String type;       // private final = immutable once created; safe to pass around
    private final long timestamp;
    private final Object source;     // Object = any type can be the event source (flexible)

    public Event(String type, Object source) {
        this.type = type;
        this.timestamp = System.currentTimeMillis();
        this.source = source;
    }

    public Event(String type) { this(type, null); }

    public String getType() { return type; }
    public long getTimestamp() { return timestamp; }
    public Object getSource() { return source; }

    @Override
    public String toString() { return getClass().getSimpleName() + "[" + type + "]"; }
}

class UserEvent extends Event { // extends = inherits from Event; IS-A relationship
    private final String username;

    public UserEvent(String type, String username) {
        super(type); // super = calls parent Event constructor
        this.username = username;
    }

    public String getUsername() { return username; }

    @Override
    public String toString() { return "UserEvent[" + getType() + ", user=" + username + "]"; }
}

class SystemEvent extends Event { // extends = another Event subtype for system-level events
    private final String message;

    public SystemEvent(String type, String message) {
        super(type);
        this.message = message;
    }

    public String getMessage() { return message; }

    @Override
    public String toString() { return "SystemEvent[" + getType() + ": " + message + "]"; }
}

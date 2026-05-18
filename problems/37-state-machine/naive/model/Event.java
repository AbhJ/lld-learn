/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Event.java — Trigger that may cause a state transition
public class Event {
    private final String name;             // final = event name is immutable once created
    private final Object payload;          // final = payload is immutable; carries extra data

    public Event(String name) { this(name, null); }
    public Event(String name, Object payload) { this.name = name; this.payload = payload; }

    public String getName() { return name; }
    public Object getPayload() { return payload; }
    @Override public String toString() { return name; }
    @Override public int hashCode() { return name.hashCode(); }
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return name.equals(((Event) o).name);
    }
}

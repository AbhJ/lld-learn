/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/DeadLetterQueue.java — Captures undelivered events for debugging
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeadLetterQueue {
    private final List<Event> deadLetters = new ArrayList<>(); // private = internal store; synchronized methods guard access

    public synchronized void add(Event event) { deadLetters.add(event); } // synchronized = thread-safe access
    public synchronized List<Event> getAll() { return Collections.unmodifiableList(new ArrayList<>(deadLetters)); }
    public synchronized int size() { return deadLetters.size(); }
}

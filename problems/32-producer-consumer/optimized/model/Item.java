/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Item.java — Represents a single item flowing through the ring buffer
public class Item {
    private final String id;        // final = immutable; safe to pass between producer and consumer threads
    private final String data;
    private final long timestamp;

    public Item(String id, String data) {
        this.id = id;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public String getData() { return data; }
    public long getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "Item[" + id + ": " + data + "]";
    }
}

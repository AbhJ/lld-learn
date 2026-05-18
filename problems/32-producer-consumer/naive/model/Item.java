/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Item.java — Represents a single item flowing through the buffer
public class Item {
    private final String id;        // private final = immutable after creation; safe to share between threads
    private final String data;      // final = once set, no thread can change it
    private final long timestamp;   // private = only accessible via getters

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

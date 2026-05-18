/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/CircularBuffer.java — Bounded circular buffer for recent locations (O(1) add)
public class CircularBuffer {
    // WHY: Circular buffer provides bounded memory usage — never grows beyond maxSize
    // Old locations are overwritten, preventing memory leaks for long-running vehicles
    private Location[] buffer; // fixed-size array = bounded memory; overwrites old entries
    private int head;          // index where next location will be written
    private int size;          // how many slots are currently filled
    private int maxSize;       // capacity limit; prevents unbounded growth

    public CircularBuffer(int maxSize) {
        this.maxSize = maxSize;
        this.buffer = new Location[maxSize];
        this.head = 0;
        this.size = 0;
    }

    // WHY: O(1) add — just overwrite at head position
    public void add(Location location) {
        buffer[head] = location;
        head = (head + 1) % maxSize;
        if (size < maxSize) size++;
    }

    public Location getLatest() {
        if (size == 0) return null;
        int idx = (head - 1 + maxSize) % maxSize;
        return buffer[idx];
    }

    public Location getPrevious() {
        if (size < 2) return null;
        int idx = (head - 2 + maxSize) % maxSize;
        return buffer[idx];
    }

    public int size() { return size; }

    public double getTotalDistanceKm() {
        double total = 0;
        int start = (size < maxSize) ? 0 : head;
        for (int i = 1; i < size; i++) {
            int prev = (start + i - 1) % maxSize;
            int curr = (start + i) % maxSize;
            total += buffer[prev].distanceTo(buffer[curr]);
        }
        return total / 1000.0;
    }
}

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/BoundedBuffer.java — Thread-safe bounded buffer using wait/notify
public class BoundedBuffer {
    private final Item[] buffer;   // private final = fixed-size array; reference never changes
    private final int capacity;
    private int head = 0;          // private = internal pointer; protected by synchronized methods
    private int tail = 0;
    private int count = 0;

    public BoundedBuffer(int capacity) {
        this.capacity = capacity;
        this.buffer = new Item[capacity];
    }

    public synchronized void put(Item item) throws InterruptedException { // synchronized = only one thread can put/take at a time
        while (count == capacity) {
            wait();
        }
        buffer[tail] = item;
        tail = (tail + 1) % capacity;
        count++;
        notifyAll();
    }

    public synchronized Item take() throws InterruptedException { // synchronized = blocks until lock available
        while (count == 0) {
            wait();
        }
        Item item = buffer[head];
        buffer[head] = null;
        head = (head + 1) % capacity;
        count--;
        notifyAll();
        return item;
    }

    public synchronized int size() { return count; }
    public int capacity() { return capacity; }
    public synchronized boolean isFull() { return count == capacity; }
    public synchronized boolean isEmpty() { return count == 0; }
}

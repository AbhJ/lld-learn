/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/Producer.java — Generates items and places them into the buffer
public class Producer implements Runnable { // implements Runnable = can be executed by a Thread
    private final String name;
    private final BoundedBuffer buffer;       // private final = shared buffer reference, never reassigned
    private final int itemCount;
    private volatile boolean running = true;  // volatile = stop() from coordinator thread visible here

    public Producer(String name, BoundedBuffer buffer, int itemCount) {
        this.name = name;
        this.buffer = buffer;
        this.itemCount = itemCount;
    }

    @Override
    public void run() {
        try {
            for (int i = 1; i <= itemCount && running; i++) {
                Item item = new Item(name + "-Item-" + i, "Data from " + name);
                buffer.put(item);
                System.out.println("  " + name + " produced: " + item);
                Thread.sleep(20);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("  " + name + " finished producing.");
    }

    public void stop() { running = false; }
    public String getName() { return name; }
}

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/Consumer.java — Takes items from the buffer and processes them
public class Consumer implements Runnable { // implements Runnable = can be executed by a Thread
    private final String name;
    private final BoundedBuffer buffer;       // private final = reference never reassigned
    private final int itemCount;
    private volatile boolean running = true;  // volatile = stop() from another thread is seen immediately
    private int consumed = 0;

    public Consumer(String name, BoundedBuffer buffer, int itemCount) {
        this.name = name;
        this.buffer = buffer;
        this.itemCount = itemCount;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < itemCount && running; i++) {
                Item item = buffer.take();
                consumed++;
                System.out.println("  " + name + " consumed: " + item);
                Thread.sleep(30);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("  " + name + " finished consuming. Total: " + consumed);
    }

    public void stop() { running = false; }
    public String getName() { return name; }
    public int getConsumed() { return consumed; }
}

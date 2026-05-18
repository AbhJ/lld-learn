/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Item.java — An item produced and consumed

import java.util.concurrent.atomic.AtomicInteger;

class Item {
    private static final AtomicInteger counter = new AtomicInteger(0); // static final = shared ID generator; AtomicInteger = thread-safe
    private final int itemId;              // final = safe publication; readable by consumer without sync
    private final String producedBy;       // final = set by producer, safely visible to consumer
    private final long producedAt;
    private volatile String consumedBy;    // volatile = written by consumer, readable by main thread immediately
    private volatile long consumedAt;      // volatile = ensures cross-thread visibility of consumption time

    public Item(String producedBy) {
        this.itemId = counter.incrementAndGet();
        this.producedBy = producedBy;
        this.producedAt = System.nanoTime();
    }

    public void markConsumed(String consumer) {
        this.consumedBy = consumer;
        this.consumedAt = System.nanoTime();
    }

    public int getItemId() { return itemId; }
    public String getProducedBy() { return producedBy; }
    public String getConsumedBy() { return consumedBy; }
    public long getProducedAt() { return producedAt; }
    public long getConsumedAt() { return consumedAt; }

    @Override
    public String toString() {
        return "Item-" + itemId + " [by " + producedBy + "]";
    }

    public static void resetCounter() { counter.set(0); }
}

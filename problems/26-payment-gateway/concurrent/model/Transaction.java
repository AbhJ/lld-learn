/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Transaction.java — Immutable transaction record

public class Transaction {
    private final String idempotencyKey; // final = set once in constructor, never changes; safe for threads to read
    private final String orderId;        // final = immutable after construction; no synchronization needed
    private final long amountCents;      // final = guarantees safe publication to other threads
    private final long processedAt;      // final = records when created; immutable
    private final String threadName;     // final = captures which thread processed this; immutable

    public Transaction(String idempotencyKey, String orderId, long amountCents) {
        this.idempotencyKey = idempotencyKey;
        this.orderId = orderId;
        this.amountCents = amountCents;
        this.processedAt = System.nanoTime();
        this.threadName = Thread.currentThread().getName();
    }

    public String getIdempotencyKey() { return idempotencyKey; }
    public String getOrderId() { return orderId; }
    public long getAmountCents() { return amountCents; }
    public long getProcessedAt() { return processedAt; }
    public String getThreadName() { return threadName; }

    @Override
    public String toString() {
        return "Transaction[order=" + orderId + ", amount=$" + (amountCents / 100) +
               "." + String.format("%02d", amountCents % 100) + ", by=" + threadName + "]";
    }
}

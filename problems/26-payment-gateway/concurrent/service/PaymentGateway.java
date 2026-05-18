/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/PaymentGateway.java — ConcurrentHashMap with putIfAbsent for idempotent payments

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PaymentGateway {
    private final ConcurrentHashMap<String, Transaction> processedPayments = new ConcurrentHashMap<>(); // ConcurrentHashMap = lock-free reads; putIfAbsent is atomic
    private final AtomicInteger totalAttempts = new AtomicInteger(0);       // AtomicInteger = thread-safe counter; no locks needed
    private final AtomicInteger duplicatesRejected = new AtomicInteger(0);  // AtomicInteger = incrementAndGet is atomic via CAS

    /**
     * Process a payment. Uses putIfAbsent to atomically prevent double-processing.
     * Returns the transaction if this call actually processed it, null if duplicate.
     */
    public Transaction processPayment(String idempotencyKey, String orderId, long amountCents) {
        totalAttempts.incrementAndGet();

        Transaction newTxn = new Transaction(idempotencyKey, orderId, amountCents);
        Transaction existing = processedPayments.putIfAbsent(idempotencyKey, newTxn);

        if (existing != null) {
            duplicatesRejected.incrementAndGet();
            return null; // duplicate — already processed
        }

        // Simulate payment processing work
        simulateProcessing();
        return newTxn;
    }

    private void simulateProcessing() {
        try {
            Thread.sleep(1); // simulate network call
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public Transaction getTransaction(String idempotencyKey) {
        return processedPayments.get(idempotencyKey);
    }

    public int getProcessedCount() { return processedPayments.size(); }
    public int getTotalAttempts() { return totalAttempts.get(); }
    public int getDuplicatesRejected() { return duplicatesRejected.get(); }
}

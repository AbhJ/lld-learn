/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Payment.java — Payment transaction with atomic counter

import java.util.concurrent.atomic.AtomicInteger;

enum PaymentStatus { PENDING, SUCCESS, FAILED, REFUNDED }

class Payment {
    private static AtomicInteger counter = new AtomicInteger(0); // AtomicInteger = thread-safe ID generation
    private String paymentId;
    private double amount;
    private volatile PaymentStatus status; // volatile = status visible across threads immediately
    private String userId;

    public Payment(double amount, String userId) {
        this.paymentId = "PAY-" + counter.incrementAndGet(); this.amount = amount; this.userId = userId; this.status = PaymentStatus.PENDING;
    }

    public boolean process() { this.status = PaymentStatus.SUCCESS; return true; }
    public boolean refund() { if (status == PaymentStatus.SUCCESS) { this.status = PaymentStatus.REFUNDED; return true; } return false; }

    public String getPaymentId() { return paymentId; }
    public double getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }

    @Override
    public String toString() { return paymentId + ": $" + String.format("%.2f", amount) + " (" + status + ")"; }
    public static void resetCounter() { counter.set(0); }
}

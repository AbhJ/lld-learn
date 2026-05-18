/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Payment.java — Payment transaction with status tracking

enum PaymentStatus { PENDING, SUCCESS, FAILED, REFUNDED } // enum = fixed payment lifecycle states

class Payment {
    private static int counter = 0;     // static = shared counter for unique payment IDs
    private String paymentId;           // private = auto-generated ID encapsulated
    private double amount;              // private = amount set at construction
    private PaymentStatus status;       // private = state machine controlled by process/refund
    private String userId;              // private = payer identity encapsulated

    public Payment(double amount, String userId) {
        this.paymentId = "PAY-" + (++counter); this.amount = amount; this.userId = userId; this.status = PaymentStatus.PENDING;
    }

    public boolean process() { this.status = PaymentStatus.SUCCESS; return true; }
    public boolean refund() { if (status == PaymentStatus.SUCCESS) { this.status = PaymentStatus.REFUNDED; return true; } return false; }

    public String getPaymentId() { return paymentId; }
    public double getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }

    @Override
    public String toString() { return paymentId + ": $" + String.format("%.2f", amount) + " (" + status + ")"; }
    public static void resetCounter() { counter = 0; }
}

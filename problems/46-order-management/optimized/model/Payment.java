/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Payment.java — Represents payment details and transaction status
import java.time.LocalDateTime;

public class Payment {
    public enum PaymentMethod { CREDIT_CARD, DEBIT_CARD, PAYPAL, BANK_TRANSFER } // enum = fixed set of payment methods; type-safe
    public enum PaymentStatus { PENDING, COMPLETED, FAILED, REFUNDED }           // enum = constrains status to valid values only

    private String id;                  // private = encapsulates payment identifier
    private double amount;              // private = only this class controls the amount
    private PaymentMethod method;       // private = payment method set once at creation
    private PaymentStatus status;       // private = status changes only through controlled methods
    private LocalDateTime timestamp;    // private = creation time hidden from outside

    public Payment(String id, double amount, PaymentMethod method) {
        this.id = id; this.amount = amount; this.method = method;
        this.status = PaymentStatus.PENDING; this.timestamp = LocalDateTime.now();
    }

    public String getId() { return id; }
    public double getAmount() { return amount; }
    public PaymentMethod getMethod() { return method; }
    public PaymentStatus getStatus() { return status; }
    public void complete() { this.status = PaymentStatus.COMPLETED; }
    public void fail() { this.status = PaymentStatus.FAILED; }
    public void refund() { this.status = PaymentStatus.REFUNDED; }

    @Override public String toString() { return "$" + String.format("%.2f", amount) + " via " + method + " (" + status + ")"; }
}

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Payment.java — Ride payment with amount, method, and status

public class Payment {
    public enum Method { CREDIT_CARD, DEBIT_CARD, WALLET, CASH } // enum = fixed payment types
    public enum Status { PENDING, COMPLETED, FAILED, REFUNDED }  // enum = fixed payment states

    private String paymentId;       // private = encapsulated unique payment ID
    private String tripId;          // private = links payment to a trip
    private double amount;          // private = payment amount encapsulated
    private Method method;          // private = how the rider pays
    private Status status;          // private = payment lifecycle state
    private static int counter = 0; // static = shared counter for unique payment IDs

    public Payment(String tripId, double amount, Method method) {
        this.paymentId = "PAY-" + (++counter);
        this.tripId = tripId;
        this.amount = amount;
        this.method = method;
        this.status = Status.PENDING;
    }

    /** Status setter used by RideService after the PaymentProxy returns. */
    public void markStatus(boolean success) {
        this.status = success ? Status.COMPLETED : Status.FAILED;
    }

    public boolean refund() {
        if (status != Status.COMPLETED) return false;
        status = Status.REFUNDED;
        System.out.printf("  Refund processed: $%.2f [%s]%n", amount, paymentId);
        return true;
    }

    public String getPaymentId() { return paymentId; }
    public double getAmount() { return amount; }
    public Method getMethod() { return method; }
    public Status getStatus() { return status; }

    @Override
    public String toString() {
        return String.format("Payment[%s] $%.2f via %s - %s", paymentId, amount, method, status);
    }
}

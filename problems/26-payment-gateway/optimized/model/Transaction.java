/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Transaction.java — Payment transaction record
public class Transaction {
    private String id;
    private double amount;
    private String currency;
    private String processorType;
    private TransactionStatus status;
    private String description;
    private String failureReason;
    private double refundedAmount;

    public Transaction(String id, double amount, String currency, String processorType, String description) {
        this.id = id; this.amount = amount; this.currency = currency;
        this.processorType = processorType; this.status = TransactionStatus.PENDING;
        this.description = description; this.refundedAmount = 0;
    }

    public String getId() { return id; }
    public double getAmount() { return amount; }
    public String getProcessorType() { return processorType; }
    public TransactionStatus getStatus() { return status; }
    public String getDescription() { return description; }
    public double getRefundedAmount() { return refundedAmount; }
    public void setStatus(TransactionStatus status) { this.status = status; }
    public void setFailureReason(String reason) { this.failureReason = reason; }
    public void addRefund(double amount) {
        this.refundedAmount += amount;
        if (this.refundedAmount >= this.amount) this.status = TransactionStatus.REFUNDED;
    }
    @Override public String toString() { return String.format("[%s] $%.2f %s -> %s", id, amount, processorType, status); }
}

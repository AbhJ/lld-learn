/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Refund.java — Processes refunds for returned or cancelled orders
import java.time.LocalDateTime;

public class Refund {
    public enum RefundStatus { PENDING, PROCESSED, FAILED } // enum = fixed refund lifecycle states

    private String id;                              // private = encapsulates refund identifier
    private String orderId;                         // private = links refund to its order
    private String returnId;                        // private = links refund to a return request
    private double amount;                          // private = refund amount set at creation only
    private Payment.PaymentMethod refundMethod;     // private = refund goes back same payment method
    private RefundStatus status;                    // private = status managed through process()

    public Refund(String id, String orderId, String returnId, double amount, Payment.PaymentMethod refundMethod) {
        this.id = id; this.orderId = orderId; this.returnId = returnId;
        this.amount = amount; this.refundMethod = refundMethod; this.status = RefundStatus.PENDING;
    }

    public String getId() { return id; }
    public double getAmount() { return amount; }
    public RefundStatus getStatus() { return status; }
    public Payment.PaymentMethod getRefundMethod() { return refundMethod; }
    public void process() { this.status = RefundStatus.PROCESSED; }
}

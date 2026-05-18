/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Return.java — Represents a return request with reason and items
import java.time.LocalDateTime;

public class Return {
    public enum ReturnStatus { REQUESTED, APPROVED, REJECTED, COMPLETED } // enum = fixed return lifecycle states
    public enum ReturnReason { DEFECTIVE, WRONG_ITEM, NOT_AS_DESCRIBED, CHANGED_MIND } // enum = predefined reasons; prevents free-text errors

    private String id;              // private = encapsulates return identifier
    private String orderId;         // private = links return to its order
    private OrderItem item;         // private = which item is being returned
    private ReturnReason reason;    // private = reason set once at creation
    private ReturnStatus status;    // private = status only changes via approve()/complete()

    public Return(String id, String orderId, OrderItem item, ReturnReason reason) {
        this.id = id; this.orderId = orderId; this.item = item; this.reason = reason;
        this.status = ReturnStatus.REQUESTED;
    }

    public String getId() { return id; }
    public OrderItem getItem() { return item; }
    public ReturnReason getReason() { return reason; }
    public ReturnStatus getStatus() { return status; }
    public void approve() { this.status = ReturnStatus.APPROVED; }
    public void complete() { this.status = ReturnStatus.COMPLETED; }
    public double getRefundAmount() { return item.getSubtotal(); }
}

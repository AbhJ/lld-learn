/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/Order.java — Order with O(1) transition validation via EnumMap
import java.util.ArrayList;
import java.util.List;

public class Order {
    private String id;                              // private = order ID accessed via getId()
    private List<OrderItem> items;                  // private = items managed via addItem()
    private OrderState state;                       // private = enum state validated via EnumMap
    private Payment payment;                        // private = payment set only through pay()
    private ShippingStrategy shippingStrategy;      // private = set during ship()
    private String trackingNumber;                  // private = set during ship()
    private OrderHistory history;                   // private = event-sourced history, internal only
    private List<Return> returns;                   // private = managed via initiateReturn()
    private List<Refund> refunds;                   // private = managed via processRefund()

    public Order(String id) {
        this.id = id; this.items = new ArrayList<>(); this.state = OrderState.CREATED;
        this.history = new OrderHistory(); this.returns = new ArrayList<>(); this.refunds = new ArrayList<>();
        history.recordEvent("CREATED", "Order placed");
    }

    public String getId() { return id; }
    public List<OrderItem> getItems() { return items; }
    public OrderState getState() { return state; }
    public Payment getPayment() { return payment; }
    public OrderHistory getHistory() { return history; }

    // WHY: True event sourcing — recompute state by replaying the event log and assert it matches the live field.
    // This is the property that distinguishes event sourcing from a plain audit log: state is *derivable* from events.
    public OrderState getCurrentState() {
        OrderState rebuilt = OrderState.rebuildFromEvents(history.getEvents());
        if (rebuilt != state) {
            throw new IllegalStateException("Event log out of sync: live=" + state + " rebuilt=" + rebuilt);
        }
        return rebuilt;
    }

    public void addItem(OrderItem item) { items.add(item); }
    public double getItemsTotal() { return items.stream().mapToDouble(OrderItem::getSubtotal).sum(); }

    // WHY: Single transition method with O(1) validation instead of per-action boolean checks
    private boolean transition(OrderState target, String description) {
        if (!state.canTransitionTo(target)) {
            System.out.println("Cannot transition from " + state + " to " + target);
            return false;
        }
        OrderState oldState = state;
        state = target;
        history.recordEvent(oldState, target, description);
        return true;
    }

    public boolean pay(Payment payment) {
        if (!transition(OrderState.PAID, "Payment received")) return false;
        this.payment = payment; payment.complete();
        System.out.println("Payment processed: " + payment);
        return true;
    }

    public boolean ship(ShippingStrategy shipping, String trackingNumber) {
        if (!transition(OrderState.SHIPPED, "Shipped via " + shipping.getName())) return false;
        this.shippingStrategy = shipping; this.trackingNumber = trackingNumber;
        System.out.println("Shipped via " + shipping.getName() + ", tracking: " + trackingNumber);
        return true;
    }

    public boolean deliver() {
        if (!transition(OrderState.DELIVERED, "Package delivered")) return false;
        System.out.println("Order delivered");
        return true;
    }

    public Return initiateReturn(OrderItem item, Return.ReturnReason reason) {
        if (!state.canTransitionTo(OrderState.RETURNED)) {
            System.out.println("Cannot return in state: " + state);
            return null;
        }
        String returnId = "RET-" + (returns.size() + 1);
        Return ret = new Return(returnId, id, item, reason);
        returns.add(ret);
        history.recordEvent("RETURN_INITIATED", "Return for " + item.getProductName());
        System.out.println("Return requested: " + item.getProductName() + " (" + reason + ")");
        return ret;
    }

    public Refund processRefund(Return ret) {
        ret.approve(); ret.complete();
        String refundId = "REF-" + (refunds.size() + 1);
        Refund refund = new Refund(refundId, id, ret.getId(), ret.getRefundAmount(), payment.getMethod());
        refund.process(); refunds.add(refund);
        history.recordEvent("REFUND_PROCESSED", "$" + String.format("%.2f", ret.getRefundAmount()) + " refunded");
        System.out.println("Refund: $" + String.format("%.2f", ret.getRefundAmount()) + " to " + payment.getMethod());
        return refund;
    }
}

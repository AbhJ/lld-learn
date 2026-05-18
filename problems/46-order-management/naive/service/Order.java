/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/Order.java — Represents a customer order with linear state transition validation
import java.util.ArrayList;
import java.util.List;

public class Order {
    private String id;                              // private = order ID hidden; accessed via getId()
    private List<OrderItem> items;                  // private = item list managed via addItem()
    private OrderState state;                       // private = state changes only through validated transitions
    private Payment payment;                        // private = payment set only when pay() succeeds
    private ShippingStrategy shippingStrategy;      // private = shipping method set during ship()
    private String trackingNumber;                  // private = tracking number set during ship()
    private OrderHistory history;                   // private = history written internally on transitions
    private List<Return> returns;                   // private = returns managed through initiateReturn()
    private List<Refund> refunds;                   // private = refunds managed through processRefund()

    public Order(String id) {
        this.id = id; this.items = new ArrayList<>(); this.state = new CreatedState();
        this.history = new OrderHistory(); this.returns = new ArrayList<>(); this.refunds = new ArrayList<>();
        this.history.addEntry("CREATED", "Order placed");
    }

    public String getId() { return id; }
    public List<OrderItem> getItems() { return items; }
    public OrderState getState() { return state; }
    public Payment getPayment() { return payment; }
    public OrderHistory getHistory() { return history; }

    public void addItem(OrderItem item) { items.add(item); }
    public double getItemsTotal() { return items.stream().mapToDouble(OrderItem::getSubtotal).sum(); }

    public boolean pay(Payment payment) {
        if (!state.canPay()) { System.out.println("Cannot pay in state: " + state.getStateName()); return false; }
        this.payment = payment; payment.complete(); this.state = new PaidState();
        history.addEntry("PAID", "Payment received");
        System.out.println("Payment processed: " + payment);
        return true;
    }

    public boolean ship(ShippingStrategy shipping, String trackingNumber) {
        if (!state.canShip()) { System.out.println("Cannot ship in state: " + state.getStateName()); return false; }
        this.shippingStrategy = shipping; this.trackingNumber = trackingNumber; this.state = new ShippedState();
        history.addEntry("SHIPPED", "Shipped via " + shipping.getName());
        System.out.println("Shipped via " + shipping.getName() + ", tracking: " + trackingNumber);
        return true;
    }

    public boolean deliver() {
        if (!state.canDeliver()) { System.out.println("Cannot deliver in state: " + state.getStateName()); return false; }
        this.state = new DeliveredState();
        history.addEntry("DELIVERED", "Package delivered");
        System.out.println("Order delivered");
        return true;
    }

    public Return initiateReturn(OrderItem item, Return.ReturnReason reason) {
        if (!state.canReturn()) { System.out.println("Cannot return in state: " + state.getStateName()); return null; }
        String returnId = "RET-" + (returns.size() + 1);
        Return ret = new Return(returnId, id, item, reason);
        returns.add(ret);
        history.addEntry("RETURN_INITIATED", "Return for " + item.getProductName());
        System.out.println("Return requested: " + item.getProductName() + " (" + reason + ")");
        return ret;
    }

    public Refund processRefund(Return ret) {
        ret.approve(); ret.complete();
        String refundId = "REF-" + (refunds.size() + 1);
        Refund refund = new Refund(refundId, id, ret.getId(), ret.getRefundAmount(), payment.getMethod());
        refund.process(); refunds.add(refund);
        history.addEntry("REFUND_PROCESSED", "$" + String.format("%.2f", ret.getRefundAmount()) + " refunded");
        System.out.println("Refund: $" + String.format("%.2f", ret.getRefundAmount()) + " to " + payment.getMethod());
        return refund;
    }
}

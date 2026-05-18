# Order Management - Variations

## Variation 1: Partial Fulfillment
**Learning Value:** Teaches partial shipment handling, split fulfillment, and per-item lifecycle tracking.

### Additional Requirements
- Ship available items immediately
- Backorder remaining items
- Partial refund for cancelled backorder items
- Track fulfillment status per item

### Design Changes
- Add `Shipment` class tracking items per shipment
- Add `FulfillmentService` splitting orders
- Add `BackorderManager` for delayed items
- Modify `OrderItem` to have individual fulfillment status

### Solution Approach
When an order is placed, `FulfillmentService` checks inventory per item. Available items are grouped into a `Shipment` and sent immediately. Unavailable items are placed in `BackorderManager` with an expected restock date. Each `OrderItem` tracks its own state (PENDING, SHIPPED, BACKORDERED, CANCELLED). The customer can cancel backordered items for a partial refund. When backorder stock arrives, remaining items ship in a follow-up shipment.

### Key Classes to Add
```java
public class FulfillmentService {
    private InventoryService inventory;
    private BackorderManager backorderManager;

    public List<Shipment> fulfill(Order order) {
        // Check stock per item, create shipment for available, backorder rest
    }

    public void onStockReplenished(String productId) { /* Process backorders */ }
}

public class Shipment {
    private String id;
    private String orderId;
    private List<OrderItem> items;
    private ShipmentStatus status;
    private String trackingNumber;
    private LocalDateTime shippedAt;
}
```

---

## Variation 2: Subscription Orders
**Learning Value:** Introduces recurring order generation, subscription billing, and auto-renewal workflows.

### Additional Requirements
- Auto-renewing orders on schedule
- Configurable frequency (weekly, monthly, etc.)
- Pause/resume subscription
- Swap items between deliveries

### Design Changes
- Add `Subscription` class with schedule
- Add `SubscriptionService` managing lifecycle
- Add `DeliverySchedule` for next delivery computation
- Modify `Order` to link to parent subscription

### Solution Approach
A `Subscription` contains a list of items, frequency (WEEKLY, BIWEEKLY, MONTHLY), and payment method. The `SubscriptionService` runs a scheduler that creates orders on the delivery date. Users can pause (skip next N deliveries), resume, swap items, or change frequency. Each generated order links back to the subscription. Before generating, validate payment method and stock availability. If payment fails, retry with backoff and notify user.

### Key Classes to Add
```java
public class Subscription {
    private String id;
    private String userId;
    private List<OrderItem> items;
    private Frequency frequency;
    private SubscriptionStatus status; // ACTIVE, PAUSED, CANCELLED
    private LocalDate nextDeliveryDate;
    private String paymentMethodId;

    public void pause(int skipCount) { /* Advance next delivery */ }
    public void swapItem(String oldProductId, String newProductId) { /* Replace */ }
}

public class SubscriptionService {
    public void processScheduledDeliveries() { /* Create orders for due subscriptions */ }
    public void handlePaymentFailure(String subscriptionId) { /* Retry logic */ }
}
```

---

## Variation 3: Multi-Vendor Marketplace
**Learning Value:** Practices multi-vendor coordination, split payments, and marketplace order routing.

### Additional Requirements
- Split order into sub-orders per vendor
- Independent tracking per vendor
- Vendor settlement and commission
- Central order view for customer

### Design Changes
- Add `SubOrder` per vendor
- Add `VendorService` managing vendor operations
- Add `SettlementService` for payment distribution
- Add `MarketplaceOrder` aggregating sub-orders

### Solution Approach
When a customer places an order with items from multiple vendors, the system creates a parent `MarketplaceOrder` and splits it into `SubOrder` per vendor. Each vendor fulfills their sub-order independently with their own shipping and tracking. The customer sees a unified view. `SettlementService` holds payment in escrow, releases to vendor after delivery confirmation minus platform commission. Returns/refunds are handled per sub-order.

### Key Classes to Add
```java
public class MarketplaceOrder {
    private String id;
    private String customerId;
    private List<SubOrder> subOrders;
    private double totalAmount;

    public SubOrder getSubOrderForVendor(String vendorId) { /* Lookup */ }
    public OrderStatus getAggregateStatus() { /* Combine sub-order statuses */ }
}

public class SubOrder {
    private String id;
    private String parentOrderId;
    private String vendorId;
    private List<OrderItem> items;
    private OrderStatus status;
    private String trackingNumber;
}

public class SettlementService {
    public void settleSubOrder(SubOrder subOrder, double commissionRate) {
        // Calculate vendor payout = subtotal - commission, initiate transfer
    }
}
```

---

## Variation 4: Order Modification
**Learning Value:** Explores trade-offs between flexibility and complexity in post-placement order changes.

### Additional Requirements
- Change quantity before shipping
- Update shipping address before dispatch
- Reauthorize payment on price change
- Modification window with deadline

### Design Changes
- Add `OrderModification` request class
- Add `ModificationPolicy` defining allowed changes per state
- Add `PaymentReauthorization` for price adjustments
- Modify `OrderService` with modification workflow

### Solution Approach
An `OrderModification` represents a requested change (quantity, address, add/remove item). The `ModificationPolicy` defines what changes are allowed in each order state — e.g., address change allowed before SHIPPED, quantity change before PACKED. If modification changes the total, `PaymentReauthorization` adjusts the charge. Modifications are applied atomically and logged in order history. A modification deadline (e.g., 30 minutes after order) can be enforced.

### Key Classes to Add
```java
public class OrderModification {
    private String orderId;
    private ModificationType type; // QUANTITY_CHANGE, ADDRESS_CHANGE, ADD_ITEM, REMOVE_ITEM
    private Map<String, Object> changes;
    private LocalDateTime requestedAt;
}

public class ModificationPolicy {
    private Map<OrderState, Set<ModificationType>> allowedModifications;

    public boolean canModify(Order order, ModificationType type) {
        // Check state and deadline
    }
}

public class OrderModificationService {
    private ModificationPolicy policy;
    private PaymentService paymentService;

    public Order applyModification(OrderModification mod) {
        // Validate policy, apply changes, reauthorize if needed
    }
}
```

---

## Variation 5: Fraud Detection
**Learning Value:** Deepens understanding of anomaly detection, risk scoring, and automated fraud prevention in orders.

### Additional Requirements
- Velocity checks (too many orders in short time)
- Address mismatch detection
- ML-based risk scoring
- Manual review queue for flagged orders

### Design Changes
- Add `FraudDetectionService` with rule engine
- Add `RiskScore` computed per order
- Add `FraudRule` interface for extensible checks
- Add `ManualReviewQueue` for human review

### Solution Approach
Before completing an order, `FraudDetectionService` runs a pipeline of `FraudRule` checks: velocity (too many orders per hour), address mismatch (billing vs shipping in different countries), high-value threshold, new account + expensive items, etc. Each rule contributes to a `RiskScore`. If score exceeds threshold, the order is held for manual review. The review queue shows flagged orders with explanations. Approved orders proceed; rejected orders are cancelled with notification.

### Key Classes to Add
```java
public interface FraudRule {
    double evaluate(Order order, CustomerProfile profile);
    String getRuleName();
}

public class FraudDetectionService {
    private List<FraudRule> rules;
    private double autoApproveThreshold;
    private double autoRejectThreshold;

    public FraudResult assess(Order order) {
        double score = rules.stream().mapToDouble(r -> r.evaluate(order, getProfile(order))).sum();
        if (score < autoApproveThreshold) return FraudResult.APPROVED;
        if (score > autoRejectThreshold) return FraudResult.REJECTED;
        return FraudResult.MANUAL_REVIEW;
    }
}

public class ManualReviewQueue {
    private Queue<FlaggedOrder> queue;
    public void enqueue(Order order, double riskScore, List<String> reasons) { /* Add to queue */ }
    public void approve(String orderId, String reviewerId) { /* Release order */ }
    public void reject(String orderId, String reviewerId, String reason) { /* Cancel order */ }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

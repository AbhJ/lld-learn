# Food Delivery System - Variations

## Variation 1: Multi-restaurant Cart
**Learning Value:** Teaches multi-source order aggregation, split fulfillment, and coordinated delivery timing.

### Additional Requirements
- Order items from multiple restaurants in a single checkout
- Separate delivery per restaurant or batched delivery
- Independent preparation tracking per restaurant
- Combined or split payment options
- Per-restaurant delivery fee calculation

### Design Changes
- Modify `Order` to support multiple `RestaurantOrder` sub-orders
- Add `MultiCartManager` to handle items across restaurants
- Add `DeliveryBatching` strategy (separate vs. combined routes)
- Modify `DeliveryAgent` assignment to handle multi-stop pickups
- Add `SplitBill` for per-restaurant cost breakdown

### Solution Approach
The cart becomes a composite containing multiple `RestaurantOrder` objects. At checkout, the system creates individual sub-orders per restaurant, each with its own preparation lifecycle. A `DeliveryBatching` strategy decides whether to assign one agent for multi-stop pickup (if restaurants are close) or separate agents per restaurant. The `MultiCartManager` calculates delivery fees per restaurant (or offers a combined discount). Payment can be a single charge with internal splitting, or per-restaurant charges. Each sub-order progresses independently through the state machine.

### Key Classes to Add
```java
public class MultiCart {
    private Map<Restaurant, List<CartItem>> restaurantItems;
    private Customer customer;

    public List<RestaurantOrder> checkout(DeliveryBatchingStrategy strategy) {
        List<RestaurantOrder> subOrders = new ArrayList<>();
        for (Map.Entry<Restaurant, List<CartItem>> entry : restaurantItems.entrySet()) {
            subOrders.add(new RestaurantOrder(entry.getKey(), entry.getValue()));
        }
        strategy.assignDeliveries(subOrders, customer.getLocation());
        return subOrders;
    }

    public double getTotalDeliveryFee() {
        return restaurantItems.keySet().stream()
            .mapToDouble(r -> calculateDeliveryFee(r, customer))
            .sum();
    }
}
```

---

## Variation 2: Subscription (Delivery Pass)
**Learning Value:** Introduces subscription lifecycle management, benefit tracking, and usage-based access control.

### Additional Requirements
- Monthly subscription with free delivery above minimum order
- Reduced service fees for subscribers
- Exclusive restaurant deals and early access
- Subscription tiers (basic, premium)
- Auto-renewal and cancellation handling
- Usage tracking and savings display

### Design Changes
- Add `Subscription` class with tier-based benefits
- Add `SubscriptionManager` for lifecycle management
- Modify `FeeCalculator` to check subscription status
- Add `BenefitEngine` for applying subscriber perks
- Add `SavingsTracker` for showing value to subscribers

### Solution Approach
Create a `Subscription` entity with tier, start date, renewal date, and status. The `FeeCalculator` checks if the customer has an active subscription before applying delivery and service fees. If subscribed and order meets minimum amount, delivery fee is waived and service fee is reduced. The `BenefitEngine` can also apply exclusive discounts from partner restaurants. A `SavingsTracker` accumulates total savings per billing cycle to demonstrate subscription value. Handle edge cases: mid-order subscription expiry, refunds for unused periods, grace periods.

### Key Classes to Add
```java
public class Subscription {
    private String customerId;
    private SubscriptionTier tier; // BASIC, PREMIUM
    private LocalDate startDate;
    private LocalDate renewalDate;
    private boolean autoRenew;
    private SubscriptionStatus status;

    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE && !isExpired();
    }

    public DeliveryFeeDiscount getDeliveryDiscount(double orderAmount) {
        if (!isActive()) return DeliveryFeeDiscount.NONE;
        if (orderAmount < tier.getMinimumOrder()) return DeliveryFeeDiscount.NONE;
        return tier.getDeliveryDiscount(); // FREE or percentage off
    }
}

public class FeeCalculator {
    public OrderFees calculate(Order order, Subscription subscription) {
        double deliveryFee = baseDeliveryFee(order);
        double serviceFee = baseServiceFee(order);
        if (subscription != null && subscription.isActive()) {
            deliveryFee *= (1 - subscription.getDeliveryDiscount().getPercentage());
            serviceFee *= (1 - subscription.getServiceFeeReduction());
        }
        return new OrderFees(deliveryFee, serviceFee);
    }
}
```

---

## Variation 3: Live Kitchen Tracking
**Learning Value:** Practices real-time status broadcasting, kitchen workflow modeling, and preparation time estimation.

### Additional Requirements
- Real-time preparation status per item
- Estimated time per item based on historical data
- Kitchen display system integration
- Delay notifications to customer
- Priority ordering for delayed items
- Multi-station tracking (grill, fryer, assembly)

### Design Changes
- Add `KitchenDisplay` for restaurant-side order management
- Add `PrepStation` representing different kitchen stations
- Add `ItemProgress` tracker with estimated completion times
- Add `ETACalculator` using historical preparation data
- Modify `OrderState` to have granular sub-states for preparation

### Solution Approach
Each `MenuItem` is tagged with `PrepStation` assignments and historical average prep times. When an order is accepted, the `KitchenDisplay` breaks it into station-level tasks. Each task progresses through states: QUEUED -> IN_PROGRESS -> READY. The `ETACalculator` uses a weighted average of historical times, current kitchen load (orders in queue), and time of day to estimate completion. If any item's prep exceeds the estimate by a threshold, a delay notification is pushed to the customer. Items are assembled at a final station before handoff to the delivery agent.

### Key Classes to Add
```java
public class KitchenDisplay {
    private Restaurant restaurant;
    private Map<PrepStation, Queue<KitchenTask>> stationQueues;

    public void receiveOrder(Order order) {
        for (OrderItem item : order.getItems()) {
            PrepStation station = item.getMenuItem().getStation();
            KitchenTask task = new KitchenTask(order.getId(), item, estimateTime(item));
            stationQueues.get(station).add(task);
        }
    }

    public OrderETA getEstimatedReady(String orderId) {
        // ETA is the max completion time across all items in the order
        return stationQueues.values().stream()
            .flatMap(q -> q.stream())
            .filter(t -> t.getOrderId().equals(orderId))
            .mapToLong(KitchenTask::getEstimatedCompletionMillis)
            .max();
    }
}
```

---

## Variation 4: Group Ordering
**Learning Value:** Explores trade-offs between coordination complexity and user convenience in shared ordering.

### Additional Requirements
- Shared cart with multiple participants
- Individual item selection per participant
- Split payment (equal split, per-item, custom split)
- Cart lock/unlock by host
- Join via link or code
- Participant deadlines (auto-close cart after timeout)

### Design Changes
- Add `GroupOrder` extending `Order` with participant management
- Add `PaymentSplitter` with multiple splitting strategies
- Add `GroupCartSession` with expiry and host controls
- Add `Participant` class with individual selections and payment method
- Add `InviteManager` for generating and validating join links

### Solution Approach
A host creates a `GroupOrder` which generates a shareable join code/link. Participants join the session and independently add items from the same restaurant. The `GroupCartSession` has a configurable deadline after which the cart auto-locks. The host can manually lock the cart at any time. At checkout, the `PaymentSplitter` calculates each participant's share based on the chosen strategy (equal split, pay-for-own-items, or custom percentages). If a participant's payment fails, only their items are removed and the order proceeds for others.

### Key Classes to Add
```java
public class GroupOrder {
    private String groupCode;
    private Participant host;
    private List<Participant> participants;
    private Restaurant restaurant;
    private LocalDateTime deadline;
    private boolean locked;

    public void addParticipant(Participant participant) {
        if (locked || isExpired()) throw new GroupOrderClosedException();
        participants.add(participant);
    }

    public Map<Participant, Double> splitPayment(SplitStrategy strategy) {
        return strategy.split(participants, getTotalAmount());
    }
}

public interface SplitStrategy {
    Map<Participant, Double> split(List<Participant> participants, double total);
}
// Implementations: EqualSplit, PerItemSplit, CustomPercentageSplit
```

---

## Variation 5: Scheduled Delivery
**Learning Value:** Deepens understanding of time-slot scheduling, capacity planning, and advance order queuing.

### Additional Requirements
- Book delivery for a future time slot
- Time slot availability management
- Batch preparation optimization for same-slot orders
- Slot capacity limits
- Early preparation window calculation
- Rescheduling and cancellation policies

### Design Changes
- Add `TimeSlot` class with capacity management
- Add `SlotManager` for availability and booking
- Add `PrepScheduler` to calculate when kitchen should start
- Add `BatchOptimizer` for grouping same-slot orders
- Modify `Order` to include scheduled delivery time

### Solution Approach
The system maintains `TimeSlot` objects (e.g., 30-minute windows) with configurable capacity per restaurant/area. When a customer selects a future slot, the `SlotManager` reserves capacity. The `PrepScheduler` calculates the optimal start time by working backward from the delivery slot (delivery time minus estimated travel time minus prep time). The `BatchOptimizer` groups orders for the same slot and same restaurant to improve kitchen efficiency. Orders can be rescheduled to a different slot if the original slot's capacity allows, subject to cancellation policy (free reschedule up to 1 hour before).

### Key Classes to Add
```java
public class SlotManager {
    private Map<LocalDate, List<TimeSlot>> availableSlots;
    private int defaultCapacity;

    public List<TimeSlot> getAvailableSlots(LocalDate date, Location deliveryAddress) {
        return availableSlots.getOrDefault(date, Collections.emptyList()).stream()
            .filter(slot -> slot.hasCapacity())
            .filter(slot -> isServiceable(slot, deliveryAddress))
            .collect(Collectors.toList());
    }

    public boolean bookSlot(TimeSlot slot, Order order) {
        if (!slot.hasCapacity()) return false;
        slot.reserve(order);
        return true;
    }
}

public class PrepScheduler {
    public LocalDateTime calculatePrepStart(Order order, TimeSlot deliverySlot) {
        long prepTimeMinutes = order.getEstimatedPrepTime();
        long travelTimeMinutes = estimateTravel(order.getRestaurant(), order.getCustomer());
        long bufferMinutes = 10;
        return deliverySlot.getStartTime()
            .minusMinutes(prepTimeMinutes + travelTimeMinutes + bufferMinutes);
    }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

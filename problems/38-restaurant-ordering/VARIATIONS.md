# Restaurant Ordering - Variations

## Variation 1: Self-Service Kiosk
**Learning Value:** Teaches user-facing order interfaces, menu navigation, and self-service workflow design.

### Additional Requirements
- Touch screen ordering with visual menu
- Item customization (size, toppings, modifications)
- Real-time order total calculation
- Payment integration (card, mobile pay, cash)

### Design Changes
- Add KioskSession managing a customer's ordering flow
- Add CustomizationOption and Modifier for item customization
- Add KioskUI interface for rendering (screen state machine)
- Add PaymentProcessor with multiple payment method support

### Solution Approach
Model the kiosk as a state machine: BROWSE -> CUSTOMIZE -> CART -> PAYMENT -> CONFIRMATION. The KioskSession holds the current cart and navigates through screens. Each MenuItem has available CustomizationOptions (e.g., size: S/M/L, add-ons: extra cheese, no onions). Modifiers affect the price. The UI renders categories, items with images, and customization dialogs. Payment integrates with a PaymentProcessor that supports multiple methods. Handle edge cases: timeout (return to home after 2 min idle), item becoming unavailable mid-order, payment failure with retry.

### Key Classes to Add
```java
public class KioskSession {
    private final String sessionId;
    private final Cart cart;
    private KioskState state; // BROWSING, CUSTOMIZING, REVIEWING, PAYING, DONE
    private long lastInteractionTime;
    
    public void addToCart(MenuItem item, List<Customization> customizations) {
        CartItem cartItem = new CartItem(item, customizations);
        cart.add(cartItem);
    }
    
    public PaymentResult processPayment(PaymentMethod method) {
        double total = cart.calculateTotal();
        PaymentResult result = paymentProcessor.charge(method, total);
        if (result.isSuccess()) {
            Order order = convertToOrder(cart);
            restaurant.submitOrder(order);
            state = KioskState.DONE;
        }
        return result;
    }
}

public class Customization {
    private final String name;      // "Size", "Extra Topping"
    private final String value;     // "Large", "Mushrooms"
    private final double priceModifier; // +2.00, +0.50
}
```

---

## Variation 2: Split Bill
**Learning Value:** Introduces proportional splitting, per-item allocation, and flexible payment distribution.

### Additional Requirements
- Split by item (each person pays for their items)
- Equal split among N people
- Custom amount split
- Tax and tip allocation across splits

### Design Changes
- Add BillSplitter with multiple split strategies
- Add SplitBill containing per-person amounts
- Add TaxAllocator for proportional tax distribution
- Add TipCalculator with per-split tip options

### Solution Approach
Support three split modes: (1) Per-item: each person claims items, pays only for those plus proportional tax/tip. (2) Equal: total divided by N people, with remainder assigned to one person. (3) Custom: each person specifies their amount, validate total matches bill. Tax is allocated proportionally based on each person's subtotal. Tip can be split equally or proportionally. Handle edge cases: shared items split among multiple people, rounding errors (distribute cents fairly), partial payments where one person pays and others owe them.

### Key Classes to Add
```java
public interface SplitStrategy {
    List<PersonBill> split(Bill bill, List<String> people);
}

public class PerItemSplit implements SplitStrategy {
    private final Map<String, List<OrderItem>> assignments; // person -> their items
    
    public List<PersonBill> split(Bill bill, List<String> people) {
        List<PersonBill> splits = new ArrayList<>();
        for (String person : people) {
            double subtotal = assignments.get(person).stream()
                .mapToDouble(OrderItem::getPrice).sum();
            double taxShare = bill.getTax() * (subtotal / bill.getSubtotal());
            double tipShare = bill.getTip() * (subtotal / bill.getSubtotal());
            splits.add(new PersonBill(person, subtotal, taxShare, tipShare));
        }
        return splits;
    }
}

public class EqualSplit implements SplitStrategy {
    public List<PersonBill> split(Bill bill, List<String> people) {
        double perPerson = bill.getTotal() / people.size();
        double remainder = bill.getTotal() - (perPerson * people.size());
        // Assign remainder cents round-robin
    }
}
```

---

## Variation 3: Kitchen Display System
**Learning Value:** Practices real-time kitchen workflow visualization, order prioritization, and preparation tracking.

### Additional Requirements
- Priority ordering (VIP, longest wait, course sequence)
- Cook time estimation per dish
- Station routing (grill, fryer, salad, dessert)
- Order synchronization (all items for a table ready together)

### Design Changes
- Add KitchenDisplay with station-based views
- Add Station representing a cooking station
- Add CookTimeEstimator based on item and current load
- Add OrderSynchronizer to coordinate multi-item orders

### Solution Approach
Each order is decomposed into station tickets: items route to the appropriate station (grill station, fryer, cold prep, dessert). The KitchenDisplay shows each station's queue ordered by priority (VIP first, then longest wait time). CookTimeEstimator considers: base cook time per item, current station load (queue depth), and parallelism at the station. OrderSynchronizer ensures all items for a table are ready at the same time by staggering start times (start slow items first, fast items later). Fire courses sequentially: appetizers first, wait for clear, then mains.

### Key Classes to Add
```java
public class KitchenDisplay {
    private final Map<String, Station> stations;
    private final OrderSynchronizer synchronizer;
    
    public void receiveOrder(Order order) {
        Map<Station, List<OrderItem>> routing = routeItems(order);
        synchronizer.planExecution(order, routing);
    }
}

public class Station {
    private final String name; // "Grill", "Fryer", "Cold Prep"
    private final PriorityQueue<StationTicket> queue;
    private final int parallelCapacity;
    
    public Duration estimateWait(OrderItem item) {
        int queueDepth = queue.size();
        Duration baseCookTime = item.getCookTime();
        return baseCookTime.plus(Duration.ofMinutes(queueDepth * avgItemTime / parallelCapacity));
    }
}

public class OrderSynchronizer {
    public void planExecution(Order order, Map<Station, List<OrderItem>> routing) {
        Duration maxCookTime = routing.entrySet().stream()
            .mapToLong(e -> e.getKey().estimateWait(e.getValue().get(0)).toMillis())
            .max().orElse(0);
        // Stagger start: fast items delayed so everything finishes together
        for (Map.Entry<Station, List<OrderItem>> entry : routing.entrySet()) {
            Duration stationTime = entry.getKey().estimateWait(entry.getValue().get(0));
            long delay = maxCookTime - stationTime.toMillis();
            entry.getKey().scheduleWithDelay(entry.getValue(), delay);
        }
    }
}
```

---

## Variation 4: Loyalty/Rewards Program
**Learning Value:** Explores trade-offs between reward generosity and business sustainability in point-based loyalty systems.

### Additional Requirements
- Earn points per order (based on spend amount)
- Redeem points for discounts or free items
- Tier levels (Bronze, Silver, Gold) with benefits
- Point expiry and bonus multiplier events

### Design Changes
- Add LoyaltyProgram managing member accounts
- Add PointsCalculator with tier multipliers
- Add RewardsRedemption for applying rewards to orders
- Add TierManager for promotion/demotion logic

### Solution Approach
Each customer has a LoyaltyAccount with point balance and tier. Points earned = spend amount * tier multiplier (Gold gets 2x). Tiers are based on annual spend: Bronze (0-500), Silver (500-2000), Gold (2000+). Evaluate tier quarterly. Points expire after 12 months (track per-transaction point batches with expiry dates, use FIFO for redemption). Redemption: define a RewardsCatalog (100 pts = free coffee, 500 pts = free meal). Apply rewards as a discount on the Bill. Bonus events: double points on weekends, birthday bonus.

### Key Classes to Add
```java
public class LoyaltyAccount {
    private final String memberId;
    private final String customerName;
    private Tier tier;
    private final List<PointsBatch> pointBatches; // for expiry tracking
    
    public int getBalance() {
        return pointBatches.stream()
            .filter(b -> !b.isExpired())
            .mapToInt(PointsBatch::getRemainingPoints)
            .sum();
    }
    
    public void earnPoints(double orderAmount) {
        int points = (int)(orderAmount * tier.getMultiplier());
        pointBatches.add(new PointsBatch(points, LocalDate.now().plusYears(1)));
    }
    
    public boolean redeemPoints(int amount) {
        if (getBalance() < amount) return false;
        // FIFO: deduct from oldest batches first
        int remaining = amount;
        for (PointsBatch batch : pointBatches) {
            if (remaining <= 0) break;
            int deducted = Math.min(remaining, batch.getRemainingPoints());
            batch.deduct(deducted);
            remaining -= deducted;
        }
        return true;
    }
}

public enum Tier {
    BRONZE(1.0, 0), SILVER(1.5, 500), GOLD(2.0, 2000);
    private final double multiplier;
    private final double annualSpendThreshold;
}
```

---

## Variation 5: Multi-Restaurant Food Court
**Learning Value:** Deepens understanding of multi-vendor coordination, shared infrastructure, and unified ordering across providers.

### Additional Requirements
- Central ordering from multiple restaurants/kitchens
- Unified bill across restaurants
- Different preparation times per restaurant
- Order coordination (everything ready together)

### Design Changes
- Add FoodCourt managing multiple Restaurant instances
- Add UnifiedCart holding items from different restaurants
- Add CrossRestaurantOrder for coordinated delivery
- Add CentralBilling aggregating from multiple sources

### Solution Approach
The FoodCourt acts as a facade over multiple Restaurants. A customer creates a UnifiedCart that can hold items from different restaurants. On checkout, the order is split into sub-orders per restaurant, each sent to the respective kitchen. A CrossRestaurantCoordinator tracks all sub-orders; it estimates completion time for each and communicates target ready-time to faster restaurants (so they don't prepare too early). The CentralBilling aggregates all sub-order totals, applies food-court-wide promotions, and generates a single bill. Handle partial failures: if one restaurant can't fulfill, offer alternatives or partial order.

### Key Classes to Add
```java
public class FoodCourt {
    private final Map<String, Restaurant> restaurants;
    private final CrossRestaurantCoordinator coordinator;
    private final CentralBilling billing;
    
    public UnifiedOrder placeOrder(UnifiedCart cart) {
        Map<Restaurant, List<CartItem>> split = splitByRestaurant(cart);
        List<SubOrder> subOrders = new ArrayList<>();
        
        for (Map.Entry<Restaurant, List<CartItem>> entry : split.entrySet()) {
            SubOrder sub = entry.getKey().placeOrder(entry.getValue());
            subOrders.add(sub);
        }
        
        UnifiedOrder unified = new UnifiedOrder(subOrders);
        coordinator.coordinate(unified); // sync timing
        return unified;
    }
    
    public Bill generateUnifiedBill(UnifiedOrder order) {
        double total = order.getSubOrders().stream()
            .mapToDouble(SubOrder::getSubtotal).sum();
        return billing.createBill(total, order);
    }
}

public class CrossRestaurantCoordinator {
    public void coordinate(UnifiedOrder order) {
        Duration maxPrepTime = order.getSubOrders().stream()
            .map(SubOrder::getEstimatedPrepTime)
            .max(Duration::compareTo).orElse(Duration.ZERO);
        
        for (SubOrder sub : order.getSubOrders()) {
            Duration delay = maxPrepTime.minus(sub.getEstimatedPrepTime());
            sub.setTargetStartDelay(delay);
        }
    }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

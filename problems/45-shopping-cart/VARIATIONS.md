# Shopping Cart - Variations

## Variation 1: Abandoned Cart Recovery
**Learning Value:** Teaches re-engagement workflows, scheduled reminders, and conversion recovery strategies.

### Additional Requirements
- Track carts that haven't been checked out
- Configurable abandonment timeout
- Send reminder emails with incentives
- Analytics on recovery rates

### Design Changes
- Add `AbandonedCartTracker` monitoring idle carts
- Add `RecoveryService` for outreach campaigns
- Add `Incentive` class (discount code, free shipping)
- Add `CartActivityLog` tracking user actions

### Solution Approach
The `AbandonedCartTracker` monitors cart activity timestamps. When a cart has items but no checkout activity for a configurable duration (e.g., 1 hour, 24 hours), it's marked as abandoned. The `RecoveryService` triggers a sequence: first reminder email, then email with incentive (e.g., 10% off), then final reminder. Track which recovery actions lead to conversion. Use a state machine for the recovery funnel (ACTIVE -> IDLE -> ABANDONED -> RECOVERED/EXPIRED).

### Key Classes to Add
```java
public class AbandonedCartTracker {
    private Map<String, LocalDateTime> lastActivity;
    private Duration abandonmentThreshold;

    public List<ShoppingCart> getAbandonedCarts() { /* Filter by threshold */ }
    public void recordActivity(String cartId) { /* Update timestamp */ }
}

public class RecoveryService {
    private AbandonedCartTracker tracker;

    public void processAbandonedCarts() { /* Check and trigger recovery */ }
    public void sendReminder(String userId, ShoppingCart cart, Incentive incentive) { /* Email */ }
    public double getRecoveryRate() { /* Analytics */ }
}
```

---

## Variation 2: Wishlist / Save for Later
**Learning Value:** Introduces deferred purchase patterns, list management, and cart-to-wishlist transitions.

### Additional Requirements
- Move items between cart and wishlist
- Price drop alerts for wishlist items
- Share wishlist with others
- Multiple wishlists (birthday, holiday, etc.)

### Design Changes
- Add `Wishlist` class with named lists
- Add `PriceWatcher` for price drop detection
- Add `WishlistService` managing operations
- Modify `ShoppingCart` to support move-to-wishlist

### Solution Approach
A user can have multiple named `Wishlist` objects. Items can be moved between cart and wishlist seamlessly (remove from one, add to other). The `PriceWatcher` tracks prices for wishlist items and sends notifications when prices drop below a threshold. Wishlists can be shared via link (read-only for others, or collaborative). Each wishlist item stores the price when added for comparison.

### Key Classes to Add
```java
public class Wishlist {
    private String id;
    private String name;
    private String userId;
    private List<WishlistItem> items;
    private boolean isPublic;

    public void addItem(Product product) { /* Store with current price */ }
    public void moveToCart(String itemId, ShoppingCart cart) { /* Transfer */ }
}

public class PriceWatcher {
    private Map<String, Double> watchedPrices; // productId -> price when added

    public void checkPriceDrops() { /* Compare current vs stored price */ }
    public void notifyPriceDrop(String userId, Product product, double oldPrice, double newPrice) { /* Alert */ }
}
```

---

## Variation 3: Cart Sharing
**Learning Value:** Practices shared state management, collaborative editing, and access-controlled shared resources.

### Additional Requirements
- Generate shareable cart link
- Collaborative shopping list (multiple people add items)
- Wedding/baby registry functionality
- Permission levels (view, edit, purchase)

### Design Changes
- Add `SharedCart` extending ShoppingCart with permissions
- Add `CartShareService` for link generation
- Add `Registry` class for gift registries
- Add `Permission` enum (VIEW, EDIT, PURCHASE)

### Solution Approach
A `SharedCart` extends the base cart with a list of collaborators and their permissions. The `CartShareService` generates unique shareable URLs. When someone accesses the link, they're granted the specified permission level. For registries, items can be "claimed" by purchasers (hidden from others to avoid duplicates). Use optimistic locking for concurrent edits — if two people add items simultaneously, both additions succeed; conflicts only arise on quantity changes.

### Key Classes to Add
```java
public class SharedCart extends ShoppingCart {
    private Map<String, Permission> collaborators;
    private String shareLink;

    public void addCollaborator(String userId, Permission permission) { /* Grant access */ }
    public boolean canModify(String userId) { /* Check permission */ }
}

public class CartShareService {
    public String generateShareLink(String cartId, Permission defaultPermission) { /* Create URL */ }
    public SharedCart resolveShareLink(String link) { /* Look up cart */ }
}

public class Registry extends SharedCart {
    private Map<String, String> claimedItems; // itemId -> claimedByUserId
    public void claimItem(String itemId, String userId) { /* Mark as purchased */ }
}
```

---

## Variation 4: Inventory-Aware Cart
**Learning Value:** Explores trade-offs between real-time accuracy and performance in stock-aware cart validation.

### Additional Requirements
- Real-time stock checking when adding to cart
- Remove or flag unavailable items at checkout
- Waitlist for out-of-stock items
- Reserve inventory on add-to-cart (with timeout)

### Design Changes
- Add `InventoryService` for stock queries
- Add `StockReservation` with TTL
- Add `WaitlistService` for OOS notifications
- Modify `ShoppingCart` to validate on add and checkout

### Solution Approach
When an item is added to cart, `InventoryService` checks current stock. If available, a `StockReservation` is created with a TTL (e.g., 15 minutes) to hold the item. If not available, the user can join a waitlist. At checkout, re-validate all reservations — expired reservations are released and the user is informed. The waitlist notifies users in FIFO order when stock is replenished. This prevents overselling while not blocking inventory indefinitely.

### Key Classes to Add
```java
public class InventoryService {
    private Map<String, Integer> stock;
    private Map<String, StockReservation> reservations;

    public boolean checkAndReserve(String productId, int quantity, String cartId) { /* Atomic check+reserve */ }
    public void releaseExpiredReservations() { /* Cleanup */ }
}

public class StockReservation {
    private String productId;
    private String cartId;
    private int quantity;
    private LocalDateTime expiresAt;
    public boolean isExpired() { return LocalDateTime.now().isAfter(expiresAt); }
}

public class WaitlistService {
    private Map<String, Queue<String>> waitlists; // productId -> userIds
    public void join(String productId, String userId) { /* Enqueue */ }
    public void notifyAvailable(String productId, int quantity) { /* Notify top N */ }
}
```

---

## Variation 5: Multi-Seller Cart (Marketplace)
**Learning Value:** Deepens understanding of multi-seller aggregation, split checkout, and marketplace cart orchestration.

### Additional Requirements
- Items from different sellers in one cart
- Separate shipping per seller
- Seller-specific promotions and policies
- Split payment to multiple sellers

### Design Changes
- Add `Seller` class with policies
- Add `SellerGroup` grouping cart items by seller
- Add `ShippingCalculator` per seller
- Modify `Checkout` to handle multi-seller settlement

### Solution Approach
Group cart items by `Seller` into `SellerGroup` objects. Each group has its own shipping calculation, return policy, and promotions. At checkout, display shipping cost per seller group. Payment is collected as one transaction from the buyer, then `SettlementService` distributes funds to each seller (minus platform commission). Each seller group may ship independently with different carriers and timelines.

### Key Classes to Add
```java
public class SellerGroup {
    private Seller seller;
    private List<CartItem> items;
    private double shippingCost;

    public double getSubtotal() { /* Sum item prices */ }
    public double calculateShipping() { /* Seller-specific rates */ }
}

public class MarketplaceCheckout extends Checkout {
    private List<SellerGroup> sellerGroups;

    public OrderSummary calculateTotals() { /* Per-seller subtotals + shipping */ }
    public List<SubOrder> splitOrder() { /* Create sub-order per seller */ }
}

public class SettlementService {
    private double platformCommissionRate;
    public void settle(Order order) { /* Distribute to each seller minus commission */ }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

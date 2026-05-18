# Vending Machine - Variations

## Variation 1: Combo Vending (Snacks + Drinks)
**Learning Value:** Teaches composite product management, slot allocation strategies, and inventory diversity.

### Additional Requirements
- Multiple product types with different storage needs
- Temperature zones (refrigerated, ambient, heated)
- Combo deals across product categories
- Separate dispensing mechanisms per zone

### Design Changes
- Add `ProductCategory` enum (SNACK, COLD_DRINK, HOT_DRINK, FRESH_FOOD)
- Add `TemperatureZone` with monitoring and alerts
- Add `ComboOffer` linking multiple products at discount
- Modify `Inventory` to be zone-aware

### Solution Approach
Partition the machine into `TemperatureZone`s, each with its own storage rack and temperature controller. Products are placed in appropriate zones. `ComboOffer` defines product bundles with a combined price less than individual sum. When a user selects a combo, validate all items are in stock across zones, then dispense sequentially. Temperature monitoring runs as a background thread alerting when zones go out of range.

### Key Classes to Add
```java
public class TemperatureZone {
    private String zoneId;
    private ZoneType type; // REFRIGERATED, AMBIENT, HEATED
    private double targetTemp;
    private double currentTemp;
    private List<Rack> racks;

    public boolean isInRange() {
        return Math.abs(currentTemp - targetTemp) < TOLERANCE;
    }
}

public class ComboOffer {
    private String comboId;
    private List<Product> products;
    private int comboPrice; // less than sum of individual prices
    private boolean isActive;

    public int getDiscount() {
        int individualTotal = products.stream().mapToInt(Product::getPrice).sum();
        return individualTotal - comboPrice;
    }
}
```

---

## Variation 2: Card Payment Integration
**Learning Value:** Introduces payment abstraction, multi-provider integration, and transaction lifecycle management.

### Additional Requirements
- NFC contactless payment
- Credit/debit card reader
- Digital wallet support (mobile wallet payments)
- Refund handling for failed dispensing

### Design Changes
- Add `PaymentGateway` interface with multiple implementations
- Add `CardReader` for physical card swipe/tap
- Add `NFCHandler` for contactless payments
- Add `RefundManager` for failed transaction reversal

### Solution Approach
Implement `PaymentGateway` as a Strategy pattern with implementations for each payment type: `CashPayment`, `CardPayment`, `NFCPayment`, `DigitalWalletPayment`. On product selection, authorize the payment amount (hold funds). On successful dispense, capture the payment. On dispense failure, initiate refund via `RefundManager`. Handle timeout scenarios where authorization expires. Store transaction log for reconciliation.

### Key Classes to Add
```java
public interface PaymentGateway {
    PaymentAuthorization authorize(int amount, PaymentCredential credential);
    boolean capture(PaymentAuthorization auth);
    boolean refund(PaymentAuthorization auth, int amount);
}

public class CardPayment implements PaymentGateway {
    private CardReader cardReader;
    private PaymentProcessor processor;

    public PaymentAuthorization authorize(int amount, PaymentCredential credential) {
        CardDetails card = cardReader.readCard();
        return processor.authorize(card, amount);
    }
}

public class RefundManager {
    private List<RefundRecord> refundLog;

    public void initiateRefund(PaymentAuthorization auth, String reason) {
        auth.getGateway().refund(auth, auth.getAmount());
        refundLog.add(new RefundRecord(auth, reason, LocalDateTime.now()));
    }
}
```

---

## Variation 3: Remote Inventory Monitoring
**Learning Value:** Practices IoT monitoring patterns, remote state observation, and threshold-based alerting.

### Additional Requirements
- Real-time stock level monitoring via IoT sensors
- Automatic reorder alerts when stock falls below threshold
- Remote temperature and machine health monitoring
- Dashboard for fleet management of multiple machines

### Design Changes
- Add `IoTSensor` interface for various sensor types
- Add `InventoryMonitor` with threshold-based alerts
- Add `MachineHealthCheck` for diagnostics
- Add `FleetManager` for multi-machine oversight

### Solution Approach
Each product slot has an `IoTSensor` (weight-based or optical) detecting remaining quantity. `InventoryMonitor` polls sensors periodically and fires events when stock drops below configurable thresholds. Events are published to a message queue consumed by `FleetManager` (which manages all machines in a region). Automatic reorder creates a `RestockOrder` sent to supplier. Health checks monitor temperature, coin mechanism, and dispenser motors.

### Key Classes to Add
```java
public class InventoryMonitor {
    private Map<String, Integer> thresholds; // productSlot -> minStock
    private List<AlertListener> listeners;

    public void checkLevels(Map<String, Integer> currentStock) {
        currentStock.forEach((slot, qty) -> {
            if (qty <= thresholds.getOrDefault(slot, 2)) {
                notifyLowStock(slot, qty);
            }
        });
    }
}

public class FleetManager {
    private List<VendingMachine> machines;
    private RestockScheduler scheduler;

    public void onLowStockAlert(String machineId, String productSlot) {
        RestockOrder order = new RestockOrder(machineId, productSlot);
        scheduler.schedule(order);
    }
}
```

---

## Variation 4: Promotional Pricing
**Learning Value:** Explores trade-offs between promotional complexity and pricing strategy extensibility.

### Additional Requirements
- Happy hour discounts during specific time windows
- Buy-2-get-1-free offers
- Loyalty points accumulation and redemption
- Flash sales with limited quantity at discount

### Design Changes
- Add `Promotion` abstract class with various implementations
- Add `PromotionEngine` evaluating applicable promotions
- Add `LoyaltyAccount` tracking points
- Add `FlashSale` with quantity limits and time windows

### Solution Approach
Define promotions with start/end times, applicable products, and discount rules. `PromotionEngine` evaluates all active promotions for a given purchase and applies the best one (or stackable ones). Happy hour: time-based discount. BOGO: track purchase count in session. Loyalty: accumulate points per purchase, redeem for free items. Flash sale: limited discounted stock that decrements atomically. Only one promotion applies unless explicitly stackable.

### Key Classes to Add
```java
public abstract class Promotion {
    private String promoId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Set<String> applicableProducts;

    public abstract int calculateDiscount(List<Product> cart);
    public boolean isActive() { return LocalDateTime.now().isBefore(endTime) && ...; }
}

public class BuyNGetFree extends Promotion {
    private int buyCount;
    private int freeCount;

    @Override
    public int calculateDiscount(List<Product> cart) {
        int sets = cart.size() / (buyCount + freeCount);
        return sets * cart.get(0).getPrice() * freeCount;
    }
}

public class PromotionEngine {
    private List<Promotion> activePromotions;

    public int getBestDiscount(List<Product> selectedProducts) {
        return activePromotions.stream()
            .filter(Promotion::isActive)
            .mapToInt(p -> p.calculateDiscount(selectedProducts))
            .max().orElse(0);
    }
}
```

---

## Variation 5: Age-Restricted Products
**Learning Value:** Deepens understanding of policy enforcement, verification workflows, and compliance-driven design.

### Additional Requirements
- ID verification for alcohol, tobacco, or medications
- Age verification via ID scanner or manual attendant approval
- Audit trail for age-restricted sales
- Lockout mechanism for failed verifications

### Design Changes
- Add `AgeRestriction` associated with products
- Add `IDVerifier` interface (scanner, manual, remote)
- Add `ComplianceLog` for audit trail
- Add `LockoutPolicy` after failed verification attempts

### Solution Approach
Tag restricted products with `AgeRestriction` (minimum age, restriction type). When a restricted product is selected, trigger verification flow before dispensing. `IDVerifier` can be implemented as barcode scanner (reads DL/passport), camera-based (AI age estimation), or remote attendant approval. All verification attempts logged in `ComplianceLog` for regulatory compliance. After N failed attempts, lock the product category for a cooldown period.

### Key Classes to Add
```java
public class AgeRestriction {
    private int minimumAge;
    private RestrictionType type; // ALCOHOL, TOBACCO, MEDICATION
    private boolean requiresPhotoID;
}

public interface IDVerifier {
    VerificationResult verify(int requiredAge);
}

public class IDScannerVerifier implements IDVerifier {
    public VerificationResult verify(int requiredAge) {
        IDDocument doc = scanner.scanDocument();
        int age = calculateAge(doc.getDateOfBirth());
        return new VerificationResult(age >= requiredAge, doc.getDocumentId());
    }
}

public class ComplianceLog {
    private List<VerificationRecord> records;

    public void logAttempt(String productId, VerificationResult result, LocalDateTime time) {
        records.add(new VerificationRecord(productId, result, time));
    }

    public List<VerificationRecord> getAuditTrail(LocalDate date) { ... }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

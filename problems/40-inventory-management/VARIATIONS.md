# Inventory Management - Variations

## Variation 1: Multi-Warehouse Routing
**Learning Value:** Teaches geographic routing optimization, warehouse selection algorithms, and distributed fulfillment.

### Additional Requirements
- Ship from nearest warehouse to customer
- Split shipment when no single warehouse has all items
- Cost optimization (shipping cost vs speed)
- Warehouse capacity and workload balancing

### Design Changes
- Add RoutingEngine to determine optimal fulfillment source
- Add ShipmentPlanner for split shipment decisions
- Add DistanceCalculator for proximity-based routing
- Add CostOptimizer balancing shipping cost vs delivery speed

### Solution Approach
When an order arrives, the RoutingEngine determines which warehouse(s) should fulfill it. Algorithm: (1) Find warehouses that have ALL items in stock (single-source preferred to avoid split shipment cost). (2) Among those, rank by proximity to customer (shipping cost/time). (3) If no single warehouse has everything, find the minimum-cost split: use set cover to minimize the number of shipments while covering all items. Factor in warehouse workload (prefer less busy warehouses when cost is similar). Consider: same-day delivery eligibility, carrier agreements per warehouse, and regional shipping zones.

### Key Classes to Add
```java
public class RoutingEngine {
    private final List<Warehouse> warehouses;
    private final DistanceCalculator distanceCalc;
    private final CostCalculator costCalc;
    
    public FulfillmentPlan route(Order order, Address customerAddress) {
        // Try single-source first
        List<Warehouse> fullStock = warehouses.stream()
            .filter(w -> w.hasAllItems(order.getItems()))
            .toList();
        
        if (!fullStock.isEmpty()) {
            Warehouse best = fullStock.stream()
                .min(Comparator.comparing(w -> costCalc.shippingCost(w, customerAddress)))
                .get();
            return FulfillmentPlan.singleSource(best, order.getItems());
        }
        
        // Split shipment - minimize number of warehouses
        return computeOptimalSplit(order, customerAddress);
    }
    
    private FulfillmentPlan computeOptimalSplit(Order order, Address address) {
        // Greedy set cover: pick warehouse covering most remaining items
        // weighted by shipping cost
    }
}

public class FulfillmentPlan {
    private final List<Shipment> shipments;
    private final double totalCost;
    private final Duration estimatedDelivery;
}
```

---

## Variation 2: Just-In-Time (JIT) Inventory
**Learning Value:** Introduces demand-driven replenishment, supplier integration, and lean inventory principles.

### Additional Requirements
- Demand forecasting based on historical sales
- Supplier lead time tracking
- Safety stock calculation
- Automatic purchase order generation

### Design Changes
- Add DemandForecaster using time series analysis
- Add SupplierManager tracking lead times and reliability
- Add SafetyStockCalculator based on demand variability
- Add AutoPurchaseOrderGenerator triggered by forecast

### Solution Approach
JIT minimizes inventory holding costs by ordering just enough, just in time. DemandForecaster uses historical sales data (moving average, exponential smoothing, or seasonal decomposition) to predict demand for the next period. Safety stock = Z-score * sqrt(lead time variance * avg demand^2 + avg lead time * demand variance^2). Reorder point = (average daily demand * lead time) + safety stock. When projected inventory (current - forecasted demand + incoming orders) drops below reorder point, automatically generate a purchase order to the preferred supplier. Track supplier reliability to adjust lead time estimates.

### Key Classes to Add
```java
public class DemandForecaster {
    private final Map<String, List<DailySales>> historicalData;
    
    public DemandForecast forecast(String productId, int daysAhead) {
        List<DailySales> history = historicalData.get(productId);
        double movingAvg = calculateMovingAverage(history, 30);
        double seasonalFactor = getSeasonalFactor(history, LocalDate.now());
        double forecast = movingAvg * seasonalFactor;
        double variance = calculateVariance(history, 30);
        return new DemandForecast(forecast * daysAhead, variance, daysAhead);
    }
}

public class SafetyStockCalculator {
    public int calculate(String productId, double serviceLevel) {
        double zScore = getZScore(serviceLevel); // 95% -> 1.65
        double demandVariance = forecaster.getDemandVariance(productId);
        double leadTimeVariance = supplierManager.getLeadTimeVariance(productId);
        double avgDemand = forecaster.getAverageDailyDemand(productId);
        double avgLeadTime = supplierManager.getAverageLeadTime(productId);
        
        return (int) Math.ceil(zScore * Math.sqrt(
            leadTimeVariance * avgDemand * avgDemand + 
            avgLeadTime * demandVariance));
    }
}

public class ReorderPointCalculator {
    public int calculate(String productId) {
        double avgDailyDemand = forecaster.getAverageDailyDemand(productId);
        double leadTimeDays = supplierManager.getAverageLeadTime(productId);
        int safetyStock = safetyStockCalc.calculate(productId, 0.95);
        return (int)(avgDailyDemand * leadTimeDays) + safetyStock;
    }
}
```

---

## Variation 3: Batch/Lot Tracking
**Learning Value:** Practices traceability systems, expiration tracking, and regulatory compliance in inventory.

### Additional Requirements
- Track batch/lot numbers for each unit
- Expiry date management with FIFO dispatch
- Recall management (identify all affected items)
- Quarantine capability for suspect batches

### Design Changes
- Add Batch/Lot entity with manufacturing date, expiry, and source
- Add ExpiryManager for FIFO dispatch and alerts
- Add RecallManager to trace and quarantine affected batches
- Add BatchInventory tracking stock per batch within warehouse

### Solution Approach
Each stock unit is associated with a Batch (lot number, manufacturing date, expiry date, supplier). Inventory is tracked at the batch level (Product + Warehouse + Batch). Dispatch uses FEFO (First Expiry First Out): always ship the batch closest to expiry that hasn't expired. ExpiryManager runs daily to flag batches approaching expiry (configurable threshold, e.g., 30 days). On recall: given a batch number, immediately identify all warehouses holding that batch, quarantine the stock (block from dispatch), and trace which orders received items from that batch for customer notification.

### Key Classes to Add
```java
public class Batch {
    private final String lotNumber;
    private final String productId;
    private final LocalDate manufacturingDate;
    private final LocalDate expiryDate;
    private final String supplierId;
    private BatchStatus status; // ACTIVE, QUARANTINED, EXPIRED, RECALLED
    
    public boolean isExpiringSoon(int thresholdDays) {
        return LocalDate.now().plusDays(thresholdDays).isAfter(expiryDate);
    }
}

public class BatchInventory {
    private final Map<String, TreeMap<LocalDate, List<BatchStock>>> inventory;
    // productId -> expiryDate -> batches (sorted for FEFO)
    
    public BatchStock pickForDispatch(String productId, int quantity) {
        TreeMap<LocalDate, List<BatchStock>> byExpiry = inventory.get(productId);
        // FEFO: pick from earliest expiry first
        for (Map.Entry<LocalDate, List<BatchStock>> entry : byExpiry.entrySet()) {
            if (entry.getKey().isBefore(LocalDate.now())) continue; // skip expired
            // Pick from this batch...
        }
    }
}

public class RecallManager {
    public RecallReport executeRecall(String lotNumber) {
        Batch batch = batchRegistry.findByLotNumber(lotNumber);
        batch.setStatus(BatchStatus.QUARANTINED);
        
        List<Warehouse> affectedWarehouses = findWarehousesWithBatch(lotNumber);
        List<Order> affectedOrders = findOrdersWithBatch(lotNumber);
        
        affectedWarehouses.forEach(w -> w.quarantineBatch(lotNumber));
        return new RecallReport(batch, affectedWarehouses, affectedOrders);
    }
}
```

---

## Variation 4: Inventory Reservation
**Learning Value:** Explores trade-offs between availability guarantees and over-commitment in reservation-based inventory.

### Additional Requirements
- Soft reserve on cart add (temporary hold)
- Timeout release (unreserved after 15 min)
- Hard reserve on payment confirmation
- Oversell protection with atomic operations

### Design Changes
- Add ReservationManager with soft and hard reservations
- Add ReservationTimeout for automatic release
- Add AtomicStockOperation for thread-safe decrement
- Add ReservationState (SOFT, HARD, RELEASED, FULFILLED)

### Solution Approach
When a customer adds an item to cart, create a soft reservation: decrement available-to-promise (ATP) count but not physical stock. Start a timeout timer (15 min). If the customer checks out within the timeout, convert to hard reservation (confirmed). If timeout expires, release the reservation (increment ATP). On payment confirmation, the hard reservation becomes a fulfillment order. Use optimistic locking or atomic CAS operations to prevent oversell under concurrent access. Track: physical stock, reserved stock, ATP (physical - reserved). ATP is what's shown to customers.

### Key Classes to Add
```java
public class ReservationManager {
    private final Map<String, StockLedger> ledgers; // productId+warehouseId -> ledger
    private final ScheduledExecutorService timeoutScheduler;
    
    public Reservation softReserve(String productId, String warehouseId, int qty, long timeoutMs) {
        StockLedger ledger = ledgers.get(productId + ":" + warehouseId);
        if (!ledger.tryReserve(qty)) {
            throw new InsufficientStockException("Only " + ledger.getATP() + " available");
        }
        Reservation res = new Reservation(UUID.randomUUID().toString(), productId, qty, ReservationState.SOFT);
        scheduleTimeout(res, timeoutMs);
        return res;
    }
    
    public void confirmReservation(String reservationId) {
        Reservation res = findReservation(reservationId);
        res.setState(ReservationState.HARD);
        cancelTimeout(reservationId);
    }
    
    private void onTimeout(Reservation res) {
        if (res.getState() == ReservationState.SOFT) {
            res.setState(ReservationState.RELEASED);
            ledgers.get(res.getLedgerKey()).release(res.getQuantity());
        }
    }
}

public class StockLedger {
    private final AtomicInteger physicalStock;
    private final AtomicInteger reservedStock;
    
    public int getATP() { return physicalStock.get() - reservedStock.get(); }
    
    public boolean tryReserve(int qty) {
        while (true) {
            int current = reservedStock.get();
            if (physicalStock.get() - current < qty) return false;
            if (reservedStock.compareAndSet(current, current + qty)) return true;
        }
    }
}
```

---

## Variation 5: Returns Processing
**Learning Value:** Deepens understanding of reverse logistics, condition assessment, and restocking workflow design.

### Additional Requirements
- RMA (Return Merchandise Authorization) workflow
- Inspection and grading (new, refurbished, defective)
- Restocking to appropriate inventory pool
- Refund calculation (full, partial, store credit)

### Design Changes
- Add ReturnRequest with RMA number and reason
- Add InspectionProcess with grading outcomes
- Add RestockingRouter directing items to correct pool
- Add RefundCalculator based on condition and policy

### Solution Approach
Customer initiates a return with a reason code. System generates an RMA and shipping label. On receipt at the warehouse, the item enters inspection: grade A (like new, restock as new), grade B (minor damage, restock as refurbished at lower price), grade C (defective, send to disposal or supplier). RestockingRouter directs items to the appropriate inventory pool based on grade. Refund is calculated based on: return reason (defective = full refund, change of mind = minus restocking fee), condition grade, and time since purchase. Track return rate per product for quality signals.

### Key Classes to Add
```java
public class ReturnRequest {
    private final String rmaNumber;
    private final String orderId;
    private final List<ReturnItem> items;
    private final ReturnReason reason;
    private ReturnState state; // REQUESTED, SHIPPED, RECEIVED, INSPECTED, COMPLETED
    
    public enum ReturnReason {
        DEFECTIVE, WRONG_ITEM, CHANGE_OF_MIND, DAMAGED_IN_TRANSIT, NOT_AS_DESCRIBED
    }
}

public class InspectionProcess {
    public InspectionResult inspect(ReturnItem item) {
        // In practice, a warehouse worker grades the item
        Grade grade = assessCondition(item);
        return new InspectionResult(item, grade, determineDisposition(grade));
    }
    
    public enum Grade { A_LIKE_NEW, B_MINOR_WEAR, C_DEFECTIVE, D_UNSALVAGEABLE }
    
    private Disposition determineDisposition(Grade grade) {
        switch (grade) {
            case A_LIKE_NEW: return Disposition.RESTOCK_AS_NEW;
            case B_MINOR_WEAR: return Disposition.RESTOCK_AS_REFURBISHED;
            case C_DEFECTIVE: return Disposition.RETURN_TO_SUPPLIER;
            case D_UNSALVAGEABLE: return Disposition.DISPOSE;
        }
    }
}

public class RefundCalculator {
    public Refund calculate(ReturnRequest request, InspectionResult inspection) {
        double originalPrice = request.getOriginalPrice();
        
        if (request.getReason() == ReturnReason.DEFECTIVE) {
            return new Refund(originalPrice, RefundMethod.ORIGINAL_PAYMENT);
        }
        
        double restockingFee = 0;
        if (request.getReason() == ReturnReason.CHANGE_OF_MIND) {
            restockingFee = originalPrice * 0.15; // 15% restocking fee
        }
        
        return new Refund(originalPrice - restockingFee, RefundMethod.ORIGINAL_PAYMENT);
    }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

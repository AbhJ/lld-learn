# Car Rental System - Variations

## Variation 1: Peer-to-peer Car Sharing
**Learning Value:** Teaches peer-to-peer marketplace design, trust systems, and owner-renter coordination.

### Additional Requirements
- Car owners can list their vehicles with availability windows
- Insurance integration (host protection, guest liability)
- Damage claim workflow with photos and dispute resolution
- Revenue sharing model (platform fee percentage)
- Owner/guest ratings and verification
- Trip extensions and early returns

### Design Changes
- Add `CarOwner` class extending `User` with listing management
- Add `Listing` class with availability calendar and pricing
- Add `InsurancePolicy` with different coverage tiers
- Add `DamageClaim` workflow with state machine
- Replace `RentalSystem` fleet management with marketplace model

### Solution Approach
Transform the centralized rental model into a marketplace. `CarOwner` creates `Listing` objects with photos, pricing rules (daily/weekly/monthly), and availability calendar. Guests search listings by location, date range, and vehicle type. The booking creates a `Trip` that links owner, guest, and vehicle. Insurance is attached at booking time with the platform providing baseline coverage. A `DamageClaimService` handles post-trip disputes with photo evidence, owner/guest statements, and platform arbitration. Revenue is split via `PaymentSplitter` (e.g., 75% owner, 25% platform).

### Key Classes to Add
```java
public class Listing {
    private CarOwner owner;
    private Vehicle vehicle;
    private AvailabilityCalendar calendar;
    private PricingRules pricing;
    private List<String> photoUrls;
    private InsuranceTier minimumInsurance;

    public boolean isAvailable(LocalDate start, LocalDate end) {
        return calendar.isAvailable(start, end);
    }

    public double calculatePrice(LocalDate start, LocalDate end) {
        return pricing.calculate(start, end);
    }
}

public class DamageClaim {
    private Trip trip;
    private ClaimState state; // FILED, UNDER_REVIEW, APPROVED, DENIED, APPEALED
    private List<Photo> evidence;
    private double claimAmount;
    private double deductible;
}
```

---

## Variation 2: One-way Rentals
**Learning Value:** Introduces asymmetric logistics, fleet rebalancing, and location-aware pricing.

### Additional Requirements
- Different pickup and return locations
- Relocation fee calculation based on distance
- Inventory rebalancing algorithm
- Popular route discounting (high-demand corridors)
- Vehicle tracking for drop-off confirmation

### Design Changes
- Add `RelocationFeeCalculator` strategy
- Add `InventoryBalancer` to track vehicle distribution across locations
- Modify `Reservation` to handle asymmetric locations
- Add `Route` class representing location pairs with demand data
- Add `RebalancingJob` for periodic fleet redistribution

### Solution Approach
Allow reservations with different pickup/return locations. The `RelocationFeeCalculator` determines the surcharge based on distance between locations and current inventory levels (lower fee if the destination is understocked). The `InventoryBalancer` maintains target vehicle counts per location and triggers `RebalancingJob` when imbalance exceeds a threshold. Popular one-way routes (e.g., airport to downtown) may have reduced or zero relocation fees since the return flow is naturally balanced. Track vehicle GPS to confirm proper drop-off at the designated location.

### Key Classes to Add
```java
public class RelocationFeeCalculator {
    private Map<String, Integer> targetInventory; // locationId -> target count
    private double baseFeePerMile;

    public double calculateFee(Location pickup, Location dropoff) {
        double distance = pickup.distanceTo(dropoff);
        double baseFee = distance * baseFeePerMile;
        double demandFactor = getDemandFactor(pickup, dropoff);
        return baseFee * demandFactor; // 0.0 if route is naturally balanced
    }

    private double getDemandFactor(Location from, Location to) {
        int toDeficit = targetInventory.get(to.getId()) - getCurrentCount(to);
        if (toDeficit > 0) return 0.5; // discount - we need cars there
        return 1.0 + (getCurrentCount(to) * 0.1); // surcharge - too many there
    }
}
```

---

## Variation 3: Fleet Management
**Learning Value:** Practices fleet-wide monitoring, maintenance scheduling, and utilization optimization.

### Additional Requirements
- Preventive maintenance scheduling based on mileage/time
- Real-time mileage tracking and alerts
- Fuel policy enforcement (return full, prepay, charge per mile)
- Vehicle lifecycle management (acquisition, depreciation, disposal)
- Utilization reporting and optimization

### Design Changes
- Add `MaintenanceScheduler` with rule-based triggers
- Add `FuelPolicy` strategy (FullToFull, PrepaidFuel, PayPerMile)
- Add `VehicleLifecycle` state machine (NEW -> ACTIVE -> MAINTENANCE -> RETIRED)
- Add `Odometer` tracking with alerts
- Add `FleetReport` for utilization metrics

### Solution Approach
Each vehicle maintains a `MaintenanceLog` and the `MaintenanceScheduler` triggers service reminders based on mileage intervals (every 5000 miles) or time intervals (every 3 months). When a vehicle is due for maintenance, it's automatically blocked from new reservations. The `FuelPolicy` is set per reservation and enforced at return (charge difference if not returned full). The `VehicleLifecycle` tracks total cost of ownership including depreciation, maintenance costs, and revenue generated, signaling when a vehicle should be retired based on maintenance-cost-to-revenue ratio.

### Key Classes to Add
```java
public class MaintenanceScheduler {
    private List<MaintenanceRule> rules;

    public List<MaintenanceAlert> checkFleet(List<Vehicle> vehicles) {
        List<MaintenanceAlert> alerts = new ArrayList<>();
        for (Vehicle v : vehicles) {
            for (MaintenanceRule rule : rules) {
                if (rule.isDue(v)) {
                    alerts.add(new MaintenanceAlert(v, rule.getType(), rule.getUrgency()));
                    v.blockForMaintenance();
                }
            }
        }
        return alerts;
    }
}

public interface FuelPolicy {
    double calculateFuelCharge(double startLevel, double returnLevel, double milesDriven);
}
```

---

## Variation 4: Corporate Accounts
**Learning Value:** Explores trade-offs between customization and standardization in B2B account management.

### Additional Requirements
- Negotiated bulk rates with tiered pricing
- Pre-approved driver lists per company
- Department-level billing and cost centers
- Monthly invoicing instead of per-rental payment
- Usage reports and budget tracking
- Policy enforcement (vehicle class limits, max rental duration)

### Design Changes
- Add `CorporateAccount` with `RentalPolicy` configuration
- Add `CostCenter` and `Department` for billing allocation
- Add `CorporateRate` pricing strategy with volume discounts
- Add `InvoiceGenerator` for monthly billing cycle
- Add `PolicyEngine` to enforce corporate rental rules

### Solution Approach
Create `CorporateAccount` entities with negotiated `CorporateRate` structures (e.g., flat daily rates by vehicle class, volume discounts above N rentals/month). Each account has a `RentalPolicy` defining allowed vehicle classes, maximum duration, geographic restrictions, and approved drivers. When a corporate user books, the `PolicyEngine` validates the request against the policy. Charges accumulate against a `CostCenter` and a monthly `InvoiceGenerator` produces consolidated bills. Budget alerts notify account managers when spending approaches limits.

### Key Classes to Add
```java
public class CorporateAccount {
    private String accountId;
    private String companyName;
    private RentalPolicy policy;
    private List<CostCenter> costCenters;
    private CorporateRate negotiatedRate;
    private double monthlyBudget;
    private double currentMonthSpend;

    public boolean isDriverApproved(String driverId) {
        return policy.getApprovedDrivers().contains(driverId);
    }

    public boolean isWithinPolicy(Reservation reservation) {
        return policy.validate(reservation);
    }
}

public class RentalPolicy {
    private Set<String> allowedVehicleClasses;
    private int maxRentalDays;
    private Set<String> approvedDrivers;
    private Set<String> allowedLocations;

    public boolean validate(Reservation reservation) { /* check all constraints */ }
}
```

---

## Variation 5: Damage Assessment
**Learning Value:** Deepens understanding of assessment workflows, evidence collection, and dispute resolution patterns.

### Additional Requirements
- Before/after photo documentation at pickup and return
- AI-assisted damage detection (scratches, dents, glass)
- Deductible calculation based on insurance tier
- Dispute resolution workflow
- Third-party estimate integration
- Wear-and-tear vs. damage classification

### Design Changes
- Add `VehicleInspection` with photo capture workflow
- Add `DamageDetector` interface (manual and AI-based implementations)
- Add `DamageReport` with severity classification
- Add `DisputeResolution` state machine (FILED -> REVIEWING -> RESOLVED/ESCALATED)
- Add `DeductibleCalculator` based on insurance and damage type

### Solution Approach
At pickup and return, a `VehicleInspection` captures timestamped photos of all vehicle panels. The `DamageDetector` compares before/after images (AI-based or manual review) to identify new damage. Detected damage creates a `DamageReport` with location, severity (MINOR/MODERATE/SEVERE), estimated repair cost, and classification (customer-caused vs. pre-existing vs. wear-and-tear). The `DeductibleCalculator` determines the customer's financial responsibility based on their insurance tier. If disputed, a `DisputeResolution` workflow collects additional evidence and routes to an arbiter.

### Key Classes to Add
```java
public class VehicleInspection {
    private String reservationId;
    private InspectionType type; // PICKUP or RETURN
    private List<PanelPhoto> photos;
    private LocalDateTime timestamp;
    private String inspectorNotes;
}

public class DamageReport {
    private VehicleInspection beforeInspection;
    private VehicleInspection afterInspection;
    private List<DamageItem> damages;
    private double totalEstimatedCost;

    public double getCustomerLiability(InsuranceTier tier) {
        double deductible = DeductibleCalculator.calculate(tier, totalEstimatedCost);
        return Math.min(deductible, totalEstimatedCost);
    }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

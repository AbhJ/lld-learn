# Parking Lot - Variations

## Variation 1: Multi-Entry/Exit Gates
**Learning Value:** Teaches distributed gate management, load balancing across entry points, and per-gate traffic optimization.

### Additional Requirements
- Multiple entry gates and multiple exit gates
- Each gate operates independently (no single bottleneck)
- Load balancing — direct vehicles to least-busy gate
- Per-gate revenue tracking and traffic analytics
- Gate-specific spot assignment (nearest spots to that gate)

### Design Changes
- Add `Gate` class with type (ENTRY/EXIT), location, and queue
- Add `GateManager` for load balancing across gates
- Modify `ParkingLevel` to track proximity to each gate
- Add `GateAssignmentStrategy` interface (round-robin, nearest-available, least-queue)

### Solution Approach
Create a `Gate` class representing physical entry/exit points, each with its own ticket machine and barrier. A `GateManager` monitors queue lengths at each gate and directs incoming vehicles to the least-busy entry. Spot assignment considers which gate the vehicle entered from — assign spots closest to that gate for shorter walk times. Each gate processes vehicles independently (no global lock), enabling true parallel entry/exit. Use Observer pattern to notify the display board at each gate about current availability.

### Key Classes to Add
```java
public class Gate {
    private String gateId;
    private GateType type; // ENTRY, EXIT
    private int queueLength;
    private List<ParkingLevel> nearestLevels; // ordered by proximity

    public Ticket processEntry(Vehicle vehicle) { ... }
    public double processExit(Ticket ticket) { ... }
}

public class GateManager {
    private List<Gate> entryGates;
    private List<Gate> exitGates;

    public Gate suggestEntryGate(Vehicle vehicle) { ... } // least-busy
    public Gate suggestExitGate(ParkingSpot spot) { ... } // nearest to spot
}
```

---

## Variation 2: Electric Vehicle Charging Spots
**Learning Value:** Introduces resource scheduling and time-based billing on top of the base allocation problem.

### Additional Requirements
- Dedicated charging spots with different charger types (Level 1, 2, DC Fast)
- Time-based billing for charging sessions
- Queue management when all chargers are occupied
- Notifications when charging complete

### Design Changes
- Add `ChargingSpot extends ParkingSpot` with charger type
- Add `ChargingSession` with start time, energy consumed, billing
- Add `ChargerType` enum (LEVEL_1, LEVEL_2, DC_FAST)
- Modify `PricingStrategy` to include per-kWh charging cost

### Solution Approach
Extend `ParkingSpot` to create `ChargingSpot` that includes charger type and power output. When an EV parks, a `ChargingSession` starts tracking energy delivery. Billing combines parking time + energy consumed. A `ChargingQueue` manages waitlisted vehicles and notifies them via Observer pattern when a charger becomes available. Add timeout rules to prevent fully-charged vehicles from hogging spots.

### Key Classes to Add
```java
public class ChargingSpot extends ParkingSpot {
    private ChargerType chargerType;
    private double powerOutputKW;
    private ChargingSession currentSession;

    public void startCharging(Vehicle vehicle) { ... }
    public void stopCharging() { ... }
}

public class ChargingSession {
    private Vehicle vehicle;
    private LocalDateTime startTime;
    private double energyConsumedKWh;
    private double costPerKWh;

    public double calculateChargingCost() { ... }
}
```

---

## Variation 3: Valet Parking
**Learning Value:** Practices service facade design, worker pool management, and token-based retrieval patterns.

### Additional Requirements
- Valet attendants pick up and return vehicles
- Automatic spot assignment (customer doesn't choose)
- Queue management during peak hours
- Key management and vehicle tracking

### Design Changes
- Add `ValetService` class managing attendant pool
- Add `ValetAttendant` with availability status
- Add `ValetRequest` queue with priority (VIP first)
- Add `KeyLocker` for secure key storage tracking

### Solution Approach
Create a `ValetService` that acts as a facade. When a customer arrives, they join a queue. Available `ValetAttendant` picks next request, assigns optimal spot (closest to exit for quick retrieval), stores keys in `KeyLocker`, and gives customer a token. On retrieval, token maps to spot + key. Use a priority queue for attendant assignment. Track attendant location to minimize walk time.

### Key Classes to Add
```java
public class ValetService {
    private Queue<ValetRequest> requestQueue;
    private List<ValetAttendant> attendants;
    private KeyLocker keyLocker;

    public String parkVehicle(Vehicle vehicle, boolean isVIP) { ... }
    public Vehicle retrieveVehicle(String token) { ... }
}

public class ValetAttendant {
    private String id;
    private AttendantStatus status; // AVAILABLE, PARKING, RETRIEVING
    private ParkingSpot currentLocation;

    public void assignTask(ValetRequest request) { ... }
}
```

---

## Variation 4: Dynamic Pricing (Airport Style)
**Learning Value:** Explores trade-offs between revenue optimization and fairness using strategy pattern with dynamic inputs.

### Additional Requirements
- Price increases as occupancy rises
- Different rates for short-term vs long-term parking
- Surge pricing during peak hours/events
- Pre-booking discounts

### Design Changes
- Extend `PricingStrategy` to consider occupancy percentage
- Add `DynamicPricingEngine` with configurable tiers
- Add `PricingTier` (occupancy range -> multiplier)
- Add `TimeBasedRate` for peak/off-peak differentiation

### Solution Approach
Implement a `DynamicPricingEngine` that recalculates rates periodically based on current occupancy. Define pricing tiers (e.g., 0-50% = 1x, 50-75% = 1.5x, 75-90% = 2x, 90%+ = 3x). Layer time-based multipliers on top (rush hour, weekends, events). For pre-booking, apply a discount but lock in the rate at time of reservation. Use Strategy pattern to swap pricing algorithms easily.

### Key Classes to Add
```java
public class DynamicPricingEngine implements PricingStrategy {
    private List<PricingTier> tiers;
    private Map<TimeSlot, Double> timeMultipliers;

    public double calculateRate(ParkingSpot spot, LocalDateTime entry) {
        double baseRate = spot.getBaseRate();
        double occupancyMultiplier = getOccupancyMultiplier();
        double timeMultiplier = getTimeMultiplier(entry);
        return baseRate * occupancyMultiplier * timeMultiplier;
    }
}

public class PricingTier {
    private double minOccupancy; // 0.0 to 1.0
    private double maxOccupancy;
    private double multiplier;
}
```

---

## Variation 5: Parking Reservation System
**Learning Value:** Deepens understanding of temporal conflict detection, interval scheduling, and cancellation policies.

### Additional Requirements
- Reserve spots for future time slots
- No-show penalties after grace period
- Modification and cancellation policies
- Conflict resolution for overlapping reservations

### Design Changes
- Add `Reservation` class with time slot, status, penalties
- Add `ReservationManager` handling CRUD and conflicts
- Add `TimeSlot` value object for start/end times
- Modify `ParkingSpot` to maintain reservation schedule

### Solution Approach
Each `ParkingSpot` maintains a schedule (list of time slots). When a reservation is requested, check for conflicts against existing reservations. Use an interval tree or sorted list for efficient overlap detection. On arrival, validate reservation within grace period. If no-show after grace period, release spot and apply penalty. Allow modifications if new time slot is available. Cancellation refund depends on how far in advance.

### Key Classes to Add
```java
public class Reservation {
    private String reservationId;
    private ParkingSpot spot;
    private Vehicle vehicle;
    private TimeSlot timeSlot;
    private ReservationStatus status; // CONFIRMED, CHECKED_IN, NO_SHOW, CANCELLED
    private double penaltyAmount;

    public boolean isWithinGracePeriod(LocalDateTime arrival) { ... }
    public void markNoShow() { ... }
}

public class ReservationManager {
    private Map<String, List<Reservation>> spotSchedule; // spotId -> reservations

    public Reservation reserve(Vehicle v, ParkingSpot spot, TimeSlot slot) { ... }
    public boolean modify(String reservationId, TimeSlot newSlot) { ... }
    public double cancel(String reservationId) { ... }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

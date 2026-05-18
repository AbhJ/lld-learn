# Hotel Management - Variations

## Variation 1: Multi-Property Chain
**Learning Value:** Teaches multi-tenant architecture, cross-property coordination, and centralized resource management.

### Additional Requirements
- Central reservation system across multiple hotels
- Cross-property loyalty program
- Transfer/redirect bookings between properties
- Standardized room categories across chain

### Design Changes
- Add `HotelChain` managing multiple `Hotel` instances
- Add `CentralReservationSystem` for cross-property search
- Add `PropertyTransfer` for booking modifications
- Add `BrandStandard` defining room category mappings

### Solution Approach
`HotelChain` aggregates multiple `Hotel` instances. `CentralReservationSystem` searches availability across all properties for a given city/region and date range. When one property is full, suggest alternatives at nearby properties. Loyalty points earned at any property can be redeemed at any other. `PropertyTransfer` handles moving a booking between hotels (cancel + rebook atomically). Room categories (Standard, Deluxe, Suite) are standardized across the chain with minimum amenity guarantees.

### Key Classes to Add
```java
public class HotelChain {
    private String chainName;
    private List<Hotel> properties;
    private CentralReservationSystem crs;
    private LoyaltyProgram loyaltyProgram;

    public List<Hotel> findAvailableProperties(String city, LocalDate checkIn, 
                                                LocalDate checkOut, RoomCategory category) {
        return properties.stream()
            .filter(h -> h.getCity().equals(city))
            .filter(h -> h.hasAvailability(checkIn, checkOut, category))
            .collect(Collectors.toList());
    }
}

public class CentralReservationSystem {
    private Map<String, List<Hotel>> propertiesByCity;

    public Booking createBooking(Guest guest, Hotel hotel, RoomCategory cat, 
                                  LocalDate checkIn, LocalDate checkOut) { ... }
    public Booking transferBooking(String bookingId, Hotel newHotel) { ... }
}
```

---

## Variation 2: Dynamic Pricing (Revenue Management)
**Learning Value:** Introduces demand forecasting, yield management, and time-sensitive pricing algorithms.

### Additional Requirements
- Adjust rates based on demand, occupancy, and events
- Competitor price monitoring
- Minimum/maximum rate guardrails
- Forecasting future demand for rate setting

### Design Changes
- Add `RevenueManager` computing optimal rates
- Add `DemandForecast` using historical data
- Add `RateGuardrails` with min/max boundaries
- Add `CompetitorRates` monitoring market prices

### Solution Approach
`RevenueManager` adjusts rates daily based on: current occupancy, booking pace (how fast rooms are selling vs. historical), upcoming events (conferences, holidays), and day-of-week patterns. When occupancy > 80%, increase rates by a multiplier. When below forecast, decrease to stimulate demand. Never go below floor rate (cost-based minimum) or above ceiling (brand-damaging maximum). Use a simple rule engine initially, upgrade to ML models for demand forecasting.

### Key Classes to Add
```java
public class RevenueManager {
    private Hotel hotel;
    private DemandForecast forecast;
    private RateGuardrails guardrails;

    public double getOptimalRate(RoomCategory category, LocalDate date) {
        double baseRate = category.getBaseRate();
        double occupancy = hotel.getOccupancyRate(date);
        double demandMultiplier = forecast.getDemandMultiplier(date);
        double eventMultiplier = getEventMultiplier(date);
        double rate = baseRate * demandMultiplier * eventMultiplier;
        return guardrails.clamp(rate, category);
    }
}

public class DemandForecast {
    private Map<DayOfWeek, Double> dayOfWeekFactors;
    private Map<LocalDate, Double> eventFactors;

    public double getDemandMultiplier(LocalDate date) {
        double base = dayOfWeekFactors.getOrDefault(date.getDayOfWeek(), 1.0);
        double event = eventFactors.getOrDefault(date, 1.0);
        return base * event;
    }
}

public class RateGuardrails {
    private Map<RoomCategory, Double> minRates;
    private Map<RoomCategory, Double> maxRates;

    public double clamp(double rate, RoomCategory category) {
        return Math.max(minRates.get(category), Math.min(maxRates.get(category), rate));
    }
}
```

---

## Variation 3: Group Booking
**Learning Value:** Practices block allocation, group lifecycle management, and partial release policies.

### Additional Requirements
- Block reservations for wedding/conference groups
- Negotiated group rates with minimum commitment
- Room allocation management (assign specific rooms to group members)
- Cutoff date for unbooked rooms to release back to inventory

### Design Changes
- Add `GroupBooking` with block size and negotiated rate
- Add `RoomBlock` holding inventory for group
- Add `CutoffPolicy` releasing unused rooms after deadline
- Add `GroupCoordinator` managing individual assignments

### Solution Approach
`GroupBooking` reserves a block of N rooms at a negotiated rate. Individual group members book against this block (not general inventory). `CutoffPolicy` defines a date (e.g., 30 days before event) after which unbooked rooms release to general inventory. Track pick-up rate (how many rooms actually booked vs. block size). Attrition clause: if group doesn't fill minimum percentage, penalty applies. Support rooming lists where coordinator assigns specific rooms to members.

### Key Classes to Add
```java
public class GroupBooking {
    private String groupId;
    private String groupName;
    private int blockSize;
    private double negotiatedRate;
    private LocalDate eventDate;
    private LocalDate cutoffDate;
    private double minimumPickupPercentage; // e.g., 0.80
    private List<IndividualReservation> reservations;

    public int getRemainingRooms() { return blockSize - reservations.size(); }
    public double getPickupRate() { return (double) reservations.size() / blockSize; }
    public boolean isPastCutoff() { return LocalDate.now().isAfter(cutoffDate); }
}

public class RoomBlock {
    private GroupBooking group;
    private List<Room> blockedRooms;
    private RoomCategory category;

    public void releaseUnbooked() {
        List<Room> unbooked = blockedRooms.stream()
            .filter(r -> !isAssigned(r))
            .collect(Collectors.toList());
        unbooked.forEach(r -> r.releaseToGeneralInventory());
    }
}

public class CutoffPolicy {
    public void checkAndRelease(GroupBooking group) {
        if (group.isPastCutoff()) {
            group.getRoomBlock().releaseUnbooked();
            if (group.getPickupRate() < group.getMinimumPickupPercentage()) {
                applyAttritionPenalty(group);
            }
        }
    }
}
```

---

## Variation 4: Loyalty/Rewards Program
**Learning Value:** Explores trade-offs between reward generosity and program sustainability using point-based systems.

### Additional Requirements
- Points earned per dollar spent on rooms, dining, spa
- Tier levels: Silver, Gold, Platinum with escalating benefits
- Points redemption for free nights, upgrades, merchandise
- Tier qualification based on nights/points per year

### Design Changes
- Add `LoyaltyProgram` managing member tiers and points
- Add `LoyaltyTier` with benefits and qualification criteria
- Add `PointsTransaction` tracking earn/burn
- Add `RewardCatalog` listing redemption options

### Solution Approach
Each guest has a `LoyaltyAccount` tracking lifetime points, current balance, and tier status. Points earned on qualifying spend (e.g., 10 points per dollar on rooms, 5 per dollar on dining). Tier qualification: Silver (10 nights/year), Gold (25 nights), Platinum (50 nights). Benefits escalate: late checkout, room upgrades, lounge access, bonus points multiplier. Points expire after 24 months of inactivity. Redemption: free night = category-based points cost (e.g., Standard = 20,000 pts).

### Key Classes to Add
```java
public class LoyaltyAccount {
    private Guest guest;
    private LoyaltyTier currentTier;
    private long pointsBalance;
    private int qualifyingNightsThisYear;
    private List<PointsTransaction> transactions;

    public void earnPoints(double spendAmount, EarnCategory category) {
        int points = (int)(spendAmount * category.getMultiplier() * currentTier.getBonusMultiplier());
        pointsBalance += points;
        transactions.add(new PointsTransaction(EARN, points, LocalDate.now()));
    }

    public boolean redeem(RewardItem item) {
        if (pointsBalance < item.getPointsCost()) return false;
        pointsBalance -= item.getPointsCost();
        transactions.add(new PointsTransaction(BURN, item.getPointsCost(), LocalDate.now()));
        return true;
    }
}

public enum LoyaltyTier {
    MEMBER(0, 1.0),
    SILVER(10, 1.25),    // 10 nights, 25% bonus points
    GOLD(25, 1.5),       // 25 nights, 50% bonus points  
    PLATINUM(50, 2.0);   // 50 nights, 100% bonus points

    private int nightsRequired;
    private double bonusMultiplier;
}
```

---

## Variation 5: Smart Room (IoT)
**Learning Value:** Deepens understanding of IoT integration, preference-based automation, and energy optimization.

### Additional Requirements
- Temperature, lighting, curtain preferences per guest profile
- Preferences remembered across stays and properties
- Voice/app control for room features
- Energy optimization when room is unoccupied

### Design Changes
- Add `SmartRoom` with IoT device registry
- Add `GuestPreferences` storing room settings
- Add `IoTDevice` interface for various controllable devices
- Add `EnergyManager` optimizing power usage

### Solution Approach
Each room has a `SmartRoom` controller managing IoT devices (thermostat, lights, curtains, TV). When guest checks in, load their `GuestPreferences` from loyalty profile and auto-configure room. Preferences stored as key-value pairs (temperature: 72F, light_level: warm_dim, curtains: half_open). Guest can adjust via room tablet or voice (voice assistant). Changes update their profile for future stays. `EnergyManager` detects unoccupied room (motion sensor + door lock status) and enters eco-mode (raise/lower temp, turn off lights).

### Key Classes to Add
```java
public class SmartRoom {
    private Room room;
    private Map<String, IoTDevice> devices;
    private GuestPreferences activePreferences;

    public void activateForGuest(Guest guest) {
        activePreferences = guest.getPreferences();
        devices.forEach((key, device) -> {
            String setting = activePreferences.get(key);
            if (setting != null) device.applySettings(setting);
        });
    }

    public void enterEcoMode() {
        devices.get("thermostat").applySettings("eco");
        devices.get("lights").applySettings("off");
        devices.get("curtains").applySettings("closed");
    }
}

public class GuestPreferences {
    private Map<String, String> settings;

    public void updatePreference(String device, String value) {
        settings.put(device, value);
    }

    public String get(String deviceKey) { return settings.get(deviceKey); }
}

public interface IoTDevice {
    String getDeviceId();
    String getCurrentState();
    void applySettings(String settings);
    DeviceType getType();
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

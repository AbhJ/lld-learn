# Airline Reservation - Variations

## Variation 1: Overbooking Management
**Learning Value:** Teaches probabilistic capacity planning, compensation policies, and overbooking risk management.

### Additional Requirements
- Statistical model to determine overbooking ratio
- Volunteer bumping process (offer compensation)
- Involuntary denied boarding with compensation rules
- No-show prediction based on historical data

### Design Changes
- Add OverbookingPolicy with configurable overbooking ratio
- Add BumpingManager for volunteer solicitation
- Add CompensationCalculator based on regulations
- Add NoShowPredictor using historical no-show rates

### Solution Approach
Allow booking beyond physical capacity based on historical no-show rate (e.g., if 10% no-show, overbook by 8%). Track overbooking ratio per flight. At check-in time, if more passengers show up than seats, trigger the bumping process: first solicit volunteers by offering increasing compensation (vouchers, upgrades on next flight). If insufficient volunteers, apply involuntary denied boarding rules (last to check in, lowest fare class). Compensation follows regulations (e.g., 2x ticket price for short delay, 4x for long delay). Track outcomes to improve the no-show prediction model.

### Key Classes to Add
```java
public class OverbookingPolicy {
    private final NoShowPredictor predictor;
    private final double maxOverbookingRatio;
    
    public int getAllowedBookings(Flight flight) {
        double noShowRate = predictor.predictNoShowRate(flight);
        int physicalCapacity = flight.getTotalSeats();
        return (int)(physicalCapacity * (1 + Math.min(noShowRate * 0.8, maxOverbookingRatio)));
    }
}

public class BumpingManager {
    public List<Passenger> solicitVolunteers(Flight flight, int seatsNeeded) {
        List<Passenger> volunteers = new ArrayList<>();
        double compensation = 200.0; // starting offer
        
        while (volunteers.size() < seatsNeeded && compensation <= 1300.0) {
            notifyPassengers(flight, compensation);
            volunteers.addAll(collectVolunteers(flight, compensation));
            compensation += 200.0; // increase offer
        }
        return volunteers;
    }
    
    public Compensation calculateInvoluntaryCompensation(Booking booking, Duration delay) {
        double ticketPrice = booking.getPrice();
        if (delay.toHours() <= 2) return new Compensation(ticketPrice * 2, "VOUCHER");
        return new Compensation(ticketPrice * 4, "CASH");
    }
}
```

---

## Variation 2: Loyalty/Miles Program
**Learning Value:** Introduces point accumulation systems, tier-based benefits, and partner reward integration.

### Additional Requirements
- Earn miles based on distance and fare class
- Status tiers (Silver, Gold, Platinum) with benefits
- Redeem miles for flights, upgrades, or partner rewards
- Miles expiry and qualification rules

### Design Changes
- Add FrequentFlyerAccount with miles balance and status
- Add MilesCalculator based on route distance and fare multiplier
- Add StatusTier with benefits (lounge, priority boarding, upgrades)
- Add RedemptionEngine for award bookings

### Solution Approach
Each passenger has a FrequentFlyerAccount. Miles earned = route distance * fare class multiplier (First: 2x, Business: 1.5x, Economy: 1x). Status tiers require qualifying miles AND qualifying segments per year. Benefits per tier: Silver (priority boarding, extra baggage), Gold (lounge access, complimentary upgrades when available), Platinum (guaranteed upgrades, companion tickets). Redemption uses an award chart mapping routes to required miles by class. Implement waitlist for award seats. Miles expire after 18 months of account inactivity. Partner earn/burn through alliances.

### Key Classes to Add
```java
public class FrequentFlyerAccount {
    private final String memberId;
    private final Passenger passenger;
    private int redeemableMiles;
    private int qualifyingMiles; // for status
    private int qualifyingSegments;
    private StatusTier tier;
    
    public void earnMiles(Flight flight, String fareClass) {
        int distance = flight.getDistance();
        double multiplier = FareClassMultiplier.get(fareClass);
        int earned = (int)(distance * multiplier * tier.getBonusMultiplier());
        redeemableMiles += earned;
        qualifyingMiles += distance; // actual distance only
        qualifyingSegments++;
    }
    
    public boolean redeemForFlight(int milesRequired) {
        if (redeemableMiles < milesRequired) return false;
        redeemableMiles -= milesRequired;
        return true;
    }
}

public enum StatusTier {
    MEMBER(1.0, List.of()),
    SILVER(1.25, List.of("PRIORITY_BOARDING", "EXTRA_BAG")),
    GOLD(1.5, List.of("LOUNGE", "COMPLIMENTARY_UPGRADE", "PRIORITY_BOARDING")),
    PLATINUM(2.0, List.of("GUARANTEED_UPGRADE", "COMPANION_TICKET", "LOUNGE"));
    
    private final double bonusMultiplier;
    private final List<String> benefits;
}
```

---

## Variation 3: Multi-Leg Itinerary
**Learning Value:** Practices complex itinerary construction, connection validation, and multi-segment booking integrity.

### Additional Requirements
- Connecting flights with layover management
- Minimum connection time validation
- Rebooking on missed connection
- Through-pricing (cheaper than booking legs separately)

### Design Changes
- Add Itinerary containing multiple FlightLeg segments
- Add ConnectionValidator for minimum connection times
- Add ItineraryPricer for through-fare calculation
- Add RebookingEngine for disruption handling

### Solution Approach
An Itinerary is a sequence of FlightLegs. Validation ensures: (1) destination of leg N matches origin of leg N+1, (2) layover time meets minimum connection time for the airport (e.g., 1h domestic, 2h international), (3) no overnight connection unless explicitly allowed. Pricing: through-fares are calculated as a package (often cheaper than sum of individual legs). On disruption (missed connection), the RebookingEngine finds alternative connections protecting the final destination, prioritizing same-day arrival. PNR (Passenger Name Record) links all legs together for coordinated changes.

### Key Classes to Add
```java
public class Itinerary {
    private final String pnr; // Passenger Name Record
    private final List<FlightLeg> legs;
    private final Passenger passenger;
    
    public boolean isValid() {
        for (int i = 0; i < legs.size() - 1; i++) {
            FlightLeg current = legs.get(i);
            FlightLeg next = legs.get(i + 1);
            if (!current.getDestination().equals(next.getOrigin())) return false;
            Duration layover = Duration.between(current.getArrival(), next.getDeparture());
            if (layover.toMinutes() < getMinConnectionTime(current, next)) return false;
        }
        return true;
    }
}

public class RebookingEngine {
    public List<Itinerary> findAlternatives(Itinerary disrupted, int missedLegIndex) {
        String currentLocation = disrupted.getLeg(missedLegIndex).getOrigin();
        String finalDestination = disrupted.getFinalDestination();
        LocalDateTime deadline = disrupted.getOriginalArrival().plusHours(24);
        
        return flightSearch.findConnections(currentLocation, finalDestination, deadline)
            .stream()
            .sorted(Comparator.comparing(Itinerary::getArrivalTime))
            .limit(5)
            .toList();
    }
}

public class ItineraryPricer {
    public double calculateThroughFare(Itinerary itinerary) {
        // Through-fare is typically 70-90% of sum of individual legs
        double sumOfLegs = itinerary.getLegs().stream()
            .mapToDouble(FlightLeg::getBasePrice).sum();
        double discount = getThroughFareDiscount(itinerary);
        return sumOfLegs * (1 - discount);
    }
}
```

---

## Variation 4: Seat Upgrade Auction
**Learning Value:** Explores trade-offs between revenue optimization and fairness in auction-based upgrade systems.

### Additional Requirements
- Passengers bid for upgrades before departure
- Dynamic pricing based on demand and availability
- Last-minute upgrade offers at check-in
- Sealed-bid auction with reserve price

### Design Changes
- Add UpgradeAuction managing bids per flight
- Add Bid with amount, passenger, and target class
- Add AuctionResolver that selects winners
- Add DynamicReservePrice based on demand signals

### Solution Approach
Before departure (e.g., 72 hours), open an upgrade auction for premium seats. Passengers submit sealed bids specifying how much extra they'll pay for an upgrade. The reserve price is dynamically set based on: demand for the flight, remaining premium inventory, and historical bid patterns. At auction close (e.g., 24 hours before departure), sort bids by amount, accept bids above reserve price starting from highest until premium seats are filled. Notify winners, charge the bid amount, reassign seats. Released economy seats can cascade (upgrade from economy+, fill economy+ from economy).

### Key Classes to Add
```java
public class UpgradeAuction {
    private final Flight flight;
    private final String targetClass;
    private final List<Bid> bids = new ArrayList<>();
    private final double reservePrice;
    private AuctionState state; // OPEN, CLOSED, RESOLVED
    
    public void submitBid(Passenger passenger, double amount) {
        if (state != AuctionState.OPEN) throw new IllegalStateException("Auction closed");
        if (amount < reservePrice) throw new IllegalArgumentException("Below reserve price");
        bids.add(new Bid(passenger, amount, System.currentTimeMillis()));
    }
    
    public List<AuctionResult> resolve() {
        state = AuctionState.CLOSED;
        int availableSeats = flight.getAvailableSeats(targetClass);
        
        List<Bid> sortedBids = bids.stream()
            .sorted(Comparator.comparing(Bid::getAmount).reversed())
            .limit(availableSeats)
            .toList();
        
        return sortedBids.stream()
            .map(bid -> processUpgrade(bid))
            .toList();
    }
}

public class DynamicReservePrice {
    public double calculate(Flight flight, String targetClass) {
        double basePrice = flight.getClassPrice(targetClass) - flight.getClassPrice("ECONOMY");
        double occupancyFactor = flight.getOccupancyRate(); // higher occupancy -> higher reserve
        double demandFactor = getHistoricalDemandFactor(flight.getRoute());
        return basePrice * 0.3 * occupancyFactor * demandFactor;
    }
}
```

---

## Variation 5: Flight Delay Management
**Learning Value:** Deepens understanding of cascading disruption handling, rebooking algorithms, and passenger communication workflows.

### Additional Requirements
- Cascade effects (delayed plane affects next flight)
- Automatic rebooking for affected passengers
- Compensation rules based on delay duration
- Real-time notification to passengers

### Design Changes
- Add DelayManager tracking aircraft rotations
- Add CascadeCalculator for downstream impact analysis
- Add AutoRebooker for affected passenger rebooking
- Add NotificationService for real-time updates
- Add CompensationEngine based on regulation rules

### Solution Approach
Track aircraft rotations (which plane operates which flights). When a flight is delayed, CascadeCalculator identifies downstream flights using the same aircraft and calculates propagated delays. For each affected flight, determine which passengers have tight connections and trigger AutoRebooker. Rebooking priority: premium passengers first, then by connection urgency. Send real-time notifications (push, SMS, email) at each status change. Calculate compensation based on regulations (EU261: 3h+ delay = 250-600 EUR depending on distance). Maintain a delay reason code for insurance/compensation claims.

### Key Classes to Add
```java
public class DelayManager {
    private final Map<String, AircraftRotation> rotations; // aircraft -> flight sequence
    private final NotificationService notifier;
    
    public DelayImpact reportDelay(String flightId, Duration delay, String reason) {
        Flight flight = findFlight(flightId);
        flight.setDelay(delay, reason);
        
        // Calculate cascade
        AircraftRotation rotation = rotations.get(flight.getAircraftId());
        List<Flight> affected = rotation.getDownstreamFlights(flight);
        
        DelayImpact impact = new DelayImpact(flight, delay);
        for (Flight downstream : affected) {
            Duration propagated = calculatePropagatedDelay(downstream, delay);
            downstream.setDelay(propagated, "CASCADED_FROM_" + flightId);
            impact.addCascade(downstream, propagated);
            rebookAffectedPassengers(downstream);
        }
        
        notifyPassengers(flight, delay, reason);
        return impact;
    }
}

public class CompensationEngine {
    public Compensation calculate(Booking booking, Duration actualDelay, String regulation) {
        if ("EU261".equals(regulation)) {
            int distance = booking.getFlight().getDistance();
            if (actualDelay.toHours() >= 3) {
                if (distance <= 1500) return new Compensation(250, "EUR");
                if (distance <= 3500) return new Compensation(400, "EUR");
                return new Compensation(600, "EUR");
            }
        }
        return Compensation.NONE;
    }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

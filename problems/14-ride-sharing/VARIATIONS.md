# Ride Sharing System - Variations

## Variation 1: Ride Pooling (Shared Rides)
**Learning Value:** Teaches route-matching algorithms, detour optimization, and shared resource allocation.

### Additional Requirements
- Match riders traveling in the same direction
- Route optimization for minimal detour
- Dynamic pricing (cheaper than solo rides)
- Maximum detour tolerance per rider
- Seat capacity management
- Real-time re-matching as new requests come in

### Design Changes
- Add `PoolMatcher` algorithm for matching compatible riders
- Add `RouteOptimizer` for multi-pickup/drop-off routing
- Add `DetourCalculator` to ensure detour within acceptable limits
- Modify `Trip` to support multiple riders (`PoolTrip`)
- Add `PoolPricing` strategy (discount based on number of riders)

### Solution Approach
When a pool ride is requested, the `PoolMatcher` checks existing active pool trips for compatibility. Compatibility is determined by: direction similarity (angle between vectors < threshold), detour percentage (added time < 20% of original ETA), and seat availability. The `RouteOptimizer` calculates the optimal pickup/drop-off sequence using a greedy approach (nearest next stop that doesn't violate any rider's maximum detour). Each rider pays a reduced fare proportional to their share of the total route. New riders can be added to an in-progress pool trip if the detour constraint is still satisfied.

### Key Classes to Add
```java
public class PoolMatcher {
    private double maxDetourPercent = 0.20;
    private double maxDirectionAngle = 45.0; // degrees

    public Optional<PoolTrip> findMatch(RideRequest request, List<PoolTrip> activeTrips) {
        for (PoolTrip trip : activeTrips) {
            if (!trip.hasAvailableSeats()) continue;
            double detour = calculateDetour(trip, request);
            if (detour <= maxDetourPercent && isDirectionCompatible(trip, request)) {
                return Optional.of(trip);
            }
        }
        return Optional.empty(); // No match, create new pool trip
    }

    private double calculateDetour(PoolTrip trip, RideRequest newRider) {
        double originalETA = trip.getCurrentETA();
        double newETA = RouteOptimizer.calculateWithNewStop(trip, newRider);
        return (newETA - originalETA) / originalETA;
    }
}
```

---

## Variation 2: Scheduled Rides
**Learning Value:** Introduces advance booking, time-slot management, and future resource commitment patterns.

### Additional Requirements
- Book rides for future times (up to 7 days ahead)
- Driver pre-assignment based on availability
- Recurring rides (daily commute)
- Guaranteed pickup (or penalty/credit)
- Fare lock at booking time
- Reminder notifications for driver and rider

### Design Changes
- Add `ScheduledRide` with booking time and scheduled pickup time
- Add `DriverScheduler` for managing driver availability slots
- Add `RecurringRideTemplate` for commute patterns
- Add `FareLock` to freeze fare estimate at booking time
- Add `ReminderService` for notifications before pickup

### Solution Approach
A rider books a `ScheduledRide` specifying pickup time and location. The system locks the estimated fare at booking time (protecting against future surge). The `DriverScheduler` assigns a driver based on proximity to pickup location at the scheduled time, using historical location data and driver availability declarations. For recurring rides, a `RecurringRideTemplate` creates scheduled rides automatically (e.g., Mon-Fri at 8:30 AM). 15 minutes before pickup, the `ReminderService` notifies both parties. If no driver is available at the scheduled time, the system guarantees a ride within 5 minutes or issues a credit.

### Key Classes to Add
```java
public class ScheduledRide {
    private Rider rider;
    private Location pickup;
    private Location dropoff;
    private LocalDateTime scheduledTime;
    private double lockedFare;
    private Driver assignedDriver;
    private ScheduleStatus status; // BOOKED, DRIVER_ASSIGNED, CONFIRMED, COMPLETED

    public boolean isWithinBookingWindow() {
        return scheduledTime.isAfter(LocalDateTime.now())
            && scheduledTime.isBefore(LocalDateTime.now().plusDays(7));
    }
}

public class DriverScheduler {
    public Driver preAssign(ScheduledRide ride) {
        List<Driver> candidates = getDriversNearLocation(
            ride.getPickup(), ride.getScheduledTime());
        return candidates.stream()
            .filter(d -> d.isAvailableAt(ride.getScheduledTime()))
            .min(Comparator.comparingDouble(d -> d.historicalDistanceTo(ride.getPickup())))
            .orElse(null);
    }
}
```

---

## Variation 3: Multi-stop Trips
**Learning Value:** Practices waypoint management, dynamic route recalculation, and multi-destination optimization.

### Additional Requirements
- Add intermediate stops to a trip (max 3-4 stops)
- Wait time at each stop (configurable maximum)
- Fare calculation per segment
- Split fare per segment with different payers
- Stop modification mid-trip (add/remove/reorder)
- ETA updates after each stop addition

### Design Changes
- Add `TripSegment` for each pickup-to-stop and stop-to-stop portion
- Modify `Trip` to contain ordered list of `TripStop` waypoints
- Add `WaitTimeTracker` with per-stop charges
- Add `SegmentFare` for per-leg pricing
- Add `FareSplitter` for attributing cost to different riders per segment

### Solution Approach
A `Trip` contains an ordered list of `TripStop` objects (pickup, intermediate stops, final destination). Each pair of consecutive stops forms a `TripSegment` with its own distance, duration, and fare. At each stop, a `WaitTimeTracker` starts; if wait time exceeds the free threshold (e.g., 3 minutes), per-minute charges apply. Riders can add stops mid-trip via the app, which triggers route recalculation and ETA update for subsequent stops. The `FareSplitter` allows different riders to be tagged to different segments (e.g., friend gets dropped off at stop 1, then rider continues to stop 2).

### Key Classes to Add
```java
public class MultiStopTrip extends Trip {
    private List<TripStop> stops; // ordered waypoints
    private int currentStopIndex;
    private WaitTimeTracker waitTracker;

    public void addStop(Location location, int position) {
        if (stops.size() >= MAX_STOPS) throw new MaxStopsExceededException();
        stops.add(position, new TripStop(location));
        recalculateRoute();
    }

    public double calculateTotalFare() {
        double fare = 0;
        for (int i = 0; i < stops.size() - 1; i++) {
            fare += calculateSegmentFare(stops.get(i), stops.get(i + 1));
        }
        fare += waitTracker.getTotalWaitCharge();
        return fare;
    }
}

public class TripStop {
    private Location location;
    private String label;
    private StopStatus status; // UPCOMING, ARRIVED, WAITING, DEPARTED
    private LocalDateTime arrivedAt;
    private LocalDateTime departedAt;
}
```

---

## Variation 4: Driver Incentives
**Learning Value:** Explores trade-offs between cost and driver motivation in incentive and bonus calculation systems.

### Additional Requirements
- Surge bonus (extra pay during high demand)
- Quest/challenge completion rewards (e.g., complete 20 rides for $50 bonus)
- Peak hour bonuses (guaranteed minimum per hour)
- Consecutive trip bonus (no reject between rides)
- Referral bonuses for bringing new drivers
- Earnings dashboard and projected earnings

### Design Changes
- Add `IncentiveEngine` with pluggable incentive types
- Add `Quest` class with progress tracking and reward
- Add `SurgeBonus` calculated per trip during surge periods
- Add `EarningsDashboard` for real-time and projected earnings
- Add `IncentiveEligibility` rules engine

### Solution Approach
The `IncentiveEngine` evaluates driver activity against active incentive programs after each trip. A `Quest` defines a target (e.g., 20 trips in a week), tracks progress, and awards a bonus on completion. `SurgeBonus` adds extra earnings during surge periods (on top of surge pricing passed to the rider). `PeakHourGuarantee` ensures minimum earnings/hour during designated peak times (if actual earnings fall short, the platform tops up). The system uses an event-driven approach: trip completion events trigger incentive evaluation, updating the driver's `EarningsDashboard` in real-time.

### Key Classes to Add
```java
public class IncentiveEngine {
    private List<Incentive> activeIncentives;

    public List<Reward> evaluateTrip(Driver driver, Trip completedTrip) {
        List<Reward> rewards = new ArrayList<>();
        for (Incentive incentive : activeIncentives) {
            if (incentive.isEligible(driver) && incentive.evaluate(completedTrip)) {
                rewards.add(incentive.calculateReward(driver, completedTrip));
            }
        }
        return rewards;
    }
}

public class Quest implements Incentive {
    private String name;
    private int targetTrips;
    private Duration timeWindow;
    private double bonusAmount;
    private Map<String, Integer> driverProgress; // driverId -> completedTrips

    public boolean evaluate(Trip trip) {
        int progress = driverProgress.merge(trip.getDriverId(), 1, Integer::sum);
        return progress >= targetTrips;
    }
}
```

---

## Variation 5: Safety Features
**Learning Value:** Deepens understanding of safety-critical feature design, real-time monitoring, and emergency workflows.

### Additional Requirements
- SOS/emergency button triggering alerts to emergency services
- Real-time ride sharing with trusted contacts
- Driver identity verification (face match before going online)
- Route deviation detection and alerts
- Audio recording option during rides
- Automated check-ins for long or late-night rides

### Design Changes
- Add `SafetyService` coordinating all safety features
- Add `SOSHandler` with emergency contact notification and location sharing
- Add `RideShareTracker` for sharing live location with contacts
- Add `RouteDeviationDetector` comparing actual vs. expected route
- Add `DriverVerification` for periodic identity checks
- Add `AutoCheckIn` for welfare checks during rides

### Solution Approach
The `SafetyService` is a facade coordinating multiple safety subsystems. The `SOSHandler` is triggered by the panic button, immediately sharing the rider's GPS coordinates with emergency services and pre-configured emergency contacts. The `RouteDeviationDetector` runs continuously during a trip, comparing the driver's actual path against the expected route; deviations beyond a threshold trigger an alert to the rider and optionally to safety monitors. `AutoCheckIn` prompts the rider during late-night rides ("Are you okay?"); no response within 60 seconds triggers an escalation. `DriverVerification` requires a selfie match against the driver's photo before each shift.

### Key Classes to Add
```java
public class SafetyService {
    private SOSHandler sosHandler;
    private RouteDeviationDetector deviationDetector;
    private RideShareTracker shareTracker;

    public void activateSOS(Trip trip) {
        Location currentLocation = trip.getCurrentLocation();
        sosHandler.alertEmergencyServices(currentLocation, trip);
        sosHandler.notifyEmergencyContacts(trip.getRider(), currentLocation);
        sosHandler.startAudioRecording(trip);
    }
}

public class RouteDeviationDetector {
    private double deviationThresholdMeters = 500;

    public void checkDeviation(Trip trip, Location currentLocation) {
        Route expectedRoute = trip.getExpectedRoute();
        double deviation = expectedRoute.distanceFromPath(currentLocation);
        if (deviation > deviationThresholdMeters) {
            alertRider(trip, "Your driver appears to be off the expected route");
            logDeviation(trip, currentLocation, deviation);
        }
    }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

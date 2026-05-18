# Vehicle Tracking - Variations

## Variation 1: Route Optimization
**Learning Value:** Teaches graph-based path finding, constraint-aware routing, and real-time route recalculation.

### Additional Requirements
- Optimal route for multiple delivery stops
- Traffic-aware routing (avoid congestion)
- Time-window constraints per delivery
- Re-route on road closures

### Design Changes
- Add `RouteOptimizer` with TSP approximation
- Add `TrafficService` providing real-time conditions
- Add `DeliveryWindow` per stop
- Modify `Route` to support dynamic re-routing

### Solution Approach
Model stops as nodes in a weighted graph where edge weights are travel times (incorporating traffic). Use a nearest-neighbor heuristic or 2-opt improvement for TSP approximation. `TrafficService` provides real-time edge weights; when conditions change significantly, trigger re-optimization. Respect `DeliveryWindow` constraints by ordering stops so that time-sensitive deliveries are prioritized. On road closure, remove the edge and recompute the route.

### Key Classes to Add
```java
public class RouteOptimizer {
    private TrafficService trafficService;

    public Route optimizeMultiStop(Location origin, List<DeliveryStop> stops) {
        // Nearest neighbor + 2-opt improvement, respecting time windows
    }

    public Route reoptimize(Route current, TrafficUpdate update) {
        // Recompute remaining stops given new conditions
    }
}

public class DeliveryStop {
    private Location location;
    private DeliveryWindow window; // earliest and latest delivery time
    private int priority;
}

public class TrafficService {
    public Duration getTravelTime(Location from, Location to) { /* Real-time estimate */ }
    public List<TrafficUpdate> getUpdates(Route route) { /* Alerts on route */ }
}
```

---

## Variation 2: Driver Behavior Scoring
**Learning Value:** Introduces telemetry analysis, scoring algorithms, and behavior pattern classification.

### Additional Requirements
- Detect harsh braking and acceleration
- Track speeding incidents
- Monitor idle time and fuel efficiency
- Generate driver safety score

### Design Changes
- Add `BehaviorAnalyzer` processing telemetry
- Add `DrivingEvent` for incidents (braking, speeding)
- Add `DriverScore` with weighted scoring
- Add `FuelEfficiencyTracker`

### Solution Approach
Collect GPS data at high frequency (every 1-2 seconds). `BehaviorAnalyzer` computes acceleration/deceleration between consecutive points — values exceeding thresholds flag harsh braking or acceleration. Speed is compared against road speed limits. Idle time is detected when location doesn't change but engine is on. Each incident type has a weight; `DriverScore` aggregates weighted incidents over a time period into a 0-100 score. Low scores trigger coaching or alerts.

### Key Classes to Add
```java
public class BehaviorAnalyzer {
    private double harshBrakeThreshold; // m/s^2
    private double harshAccelThreshold;

    public List<DrivingEvent> analyzeTrip(List<Location> gpsPoints) {
        // Compute acceleration between consecutive points, flag incidents
    }
}

public class DriverScore {
    private String driverId;
    private int score; // 0-100
    private Map<DrivingEventType, Integer> incidentCounts;

    public void recalculate(List<DrivingEvent> recentEvents) {
        // Weighted penalty per event type, decay over time
    }
}

public class DrivingEvent {
    private DrivingEventType type; // HARSH_BRAKE, HARSH_ACCEL, SPEEDING, IDLE
    private Location location;
    private LocalDateTime timestamp;
    private double severity;
}
```

---

## Variation 3: ETA Prediction
**Learning Value:** Practices prediction modeling, historical data analysis, and confidence interval estimation.

### Additional Requirements
- ML-based arrival time prediction
- Factor in traffic patterns and history
- Update ETA in real-time as vehicle moves
- Confidence intervals on predictions

### Design Changes
- Add `ETAPredictor` with prediction model
- Add `TrafficPattern` historical data
- Add `ETAUpdate` for real-time recalculation
- Add `ConfidenceInterval` for uncertainty

### Solution Approach
`ETAPredictor` uses a feature-based model considering: distance remaining, current traffic, time of day (rush hour patterns), day of week, weather, and historical trip durations for the route. As the vehicle progresses, recompute ETA using actual progress vs. predicted progress. Provide confidence intervals (e.g., "15-20 minutes") rather than point estimates. Historical data improves predictions over time. Fall back to simple distance/speed when model data is sparse.

### Key Classes to Add
```java
public class ETAPredictor {
    private TrafficPattern historicalData;

    public ETAPrediction predict(Location current, Location destination, LocalDateTime now) {
        // Feature extraction + model inference
    }

    public ETAPrediction updatePrediction(Location current, ETAPrediction previous) {
        // Adjust based on actual progress
    }
}

public class ETAPrediction {
    private Duration estimatedTime;
    private Duration lowerBound;
    private Duration upperBound;
    private double confidence;
    private LocalDateTime predictedArrival;
}

public class TrafficPattern {
    private Map<String, Map<Integer, Double>> segmentSpeeds; // roadSegment -> hourOfDay -> avgSpeed
    public double getExpectedSpeed(String segment, LocalDateTime time) { /* Lookup */ }
}
```

---

## Variation 4: Maintenance Prediction
**Learning Value:** Explores trade-offs between prediction accuracy and maintenance cost in predictive maintenance systems.

### Additional Requirements
- Mileage-based maintenance schedules
- Sensor data analysis (engine, brakes, tires)
- Predictive alerts before breakdown
- Maintenance history tracking

### Design Changes
- Add `MaintenancePredictor` with prediction logic
- Add `SensorData` collecting vehicle telemetry
- Add `MaintenanceSchedule` with service intervals
- Add `MaintenanceHistory` tracking past services

### Solution Approach
Track cumulative mileage and engine hours for schedule-based maintenance (oil change every 5000km). Beyond scheduled maintenance, `SensorData` (engine temperature, brake pad thickness, tire pressure, battery voltage) feeds into `MaintenancePredictor`. The predictor uses threshold rules and trend analysis — if brake pad thickness is decreasing linearly, predict when it will reach minimum. Generate alerts well before critical thresholds. Log all maintenance in history for pattern detection.

### Key Classes to Add
```java
public class MaintenancePredictor {
    private Map<String, List<SensorReading>> sensorHistory;

    public List<MaintenanceAlert> predict(String vehicleId) {
        // Trend analysis on sensor data, threshold proximity
    }

    public LocalDate predictNextService(String vehicleId, MaintenanceType type) {
        // Based on mileage rate and service interval
    }
}

public class SensorData {
    private String vehicleId;
    private double engineTemp;
    private double brakePadThickness;
    private double tirePressure;
    private double batteryVoltage;
    private double oilLevel;
    private LocalDateTime timestamp;
}

public class MaintenanceAlert {
    private String vehicleId;
    private MaintenanceType type;
    private AlertSeverity severity; // INFO, WARNING, CRITICAL
    private String message;
    private LocalDate predictedDueDate;
}
```

---

## Variation 5: Multi-Modal Tracking
**Learning Value:** Deepens understanding of multi-source tracking aggregation, handoff logic, and intermodal coordination.

### Additional Requirements
- Track across transport modes (truck, drone, foot)
- Handoff tracking between modes
- Mode-specific telemetry
- End-to-end chain of custody

### Design Changes
- Add `TransportMode` hierarchy (Truck, Drone, FootCourier)
- Add `HandoffEvent` recording transfers
- Add `ChainOfCustody` tracking possession changes
- Modify `TrackingService` to support mode transitions

### Solution Approach
A delivery can span multiple `TransportMode` segments: warehouse to hub (truck), hub to neighborhood (drone), drone landing to door (foot courier). Each mode has different tracking characteristics (GPS frequency, altitude for drones, walking speed for foot). `HandoffEvent` records when a package transfers between modes (who, when, where, condition). `ChainOfCustody` provides an immutable audit trail of all custody changes for the package from origin to destination.

### Key Classes to Add
```java
public abstract class TransportMode {
    private String vehicleId;
    private String operatorId;
    public abstract Location getCurrentLocation();
    public abstract Duration getUpdateFrequency();
}

public class HandoffEvent {
    private String packageId;
    private TransportMode fromMode;
    private TransportMode toMode;
    private Location handoffLocation;
    private LocalDateTime timestamp;
    private String condition; // GOOD, DAMAGED, etc.
}

public class ChainOfCustody {
    private String packageId;
    private List<CustodyEntry> entries;

    public void recordHandoff(HandoffEvent event) { /* Append to chain */ }
    public List<CustodyEntry> getFullHistory() { /* Immutable audit trail */ }
    public TransportMode getCurrentCustodian() { /* Latest entry */ }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

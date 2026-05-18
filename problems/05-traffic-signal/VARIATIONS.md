# Traffic Signal - Variations

## Variation 1: Adaptive Signal (AI-Based)
**Learning Value:** Teaches feedback-loop control systems, sensor-driven decision making, and adaptive algorithms.

### Additional Requirements
- Traffic density sensors (camera, induction loops) feed real-time data
- Signal timing adjusts dynamically based on queue length
- Learning from historical patterns (time of day, day of week)
- Emergency vehicle preemption (green wave for ambulances)

### Design Changes
- Add `TrafficSensor` interface (camera, loop detector, radar)
- Add `AdaptiveController` that adjusts phase durations
- Add `TrafficModel` predicting demand from historical data
- Add `PreemptionHandler` for emergency vehicle override

### Solution Approach
Deploy `TrafficSensor`s at each approach to measure queue length and flow rate. `AdaptiveController` runs a feedback loop: measure -> compute optimal phase split -> apply. Phase duration is proportional to demand ratio. If north-south has 2x traffic of east-west, give it 2x green time. Emergency preemption: on detecting emergency vehicle (via GPS or optical sensor), immediately transition to green for that approach. After vehicle passes, smoothly resume adaptive cycle.

### Key Classes to Add
```java
public class AdaptiveController {
    private Map<String, TrafficSensor> sensors; // direction -> sensor
    private int minGreenSeconds;
    private int maxGreenSeconds;

    public Map<String, Integer> calculateOptimalPhases() {
        Map<String, Integer> demand = sensors.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getQueueLength()));
        int totalDemand = demand.values().stream().mapToInt(i -> i).sum();
        int totalGreenTime = getCycleLength() - getAllRedTime();
        // Allocate proportional to demand
        return demand.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, 
                e -> clamp(totalGreenTime * e.getValue() / totalDemand, minGreenSeconds, maxGreenSeconds)));
    }
}

public class PreemptionHandler {
    public void onEmergencyVehicleDetected(String approachDirection) {
        // Force green for emergency approach, red for all others
    }
}
```

---

## Variation 2: Green Wave Corridor
**Learning Value:** Introduces coordination across sequential systems and timing synchronization patterns.

### Additional Requirements
- Synchronize multiple signals along a corridor
- Vehicles traveling at speed limit hit green lights continuously
- Offset calculation based on distance between intersections
- Bidirectional coordination for two-way streets

### Design Changes
- Add `Corridor` grouping multiple intersections in sequence
- Add `OffsetCalculator` computing signal timing offsets
- Add `SpeedProfile` defining target travel speed
- Modify `Intersection` to accept offset from corridor controller

### Solution Approach
A `Corridor` manages N intersections along a road. Calculate the travel time between consecutive intersections at target speed. Set each signal's green start as an offset from the first signal: offset_i = distance_i / speed. All signals share the same cycle length. For bidirectional green wave, use a cycle length that is 2x the one-way travel time of the corridor. The "bandwidth" (percentage of cycle that's green) determines how many vehicles can travel uninterrupted.

### Key Classes to Add
```java
public class Corridor {
    private String corridorName;
    private List<Intersection> intersections;
    private double targetSpeedKmh;
    private int cycleLengthSeconds;

    public void synchronize() {
        double speedMps = targetSpeedKmh / 3.6;
        for (int i = 1; i < intersections.size(); i++) {
            double distance = getDistance(intersections.get(i-1), intersections.get(i));
            int offset = (int)(distance / speedMps) % cycleLengthSeconds;
            intersections.get(i).setGreenOffset(offset);
        }
    }
}

public class OffsetCalculator {
    public int calculateOffset(double distanceMeters, double speedMps, int cycleLength) {
        return (int)(distanceMeters / speedMps) % cycleLength;
    }
}
```

---

## Variation 3: Railway Crossing Integration
**Learning Value:** Practices priority-based preemption, safety interlock design, and multi-system integration.

### Additional Requirements
- Train approach detection triggers crossing closure
- All road signals turn red, barriers lower
- Minimum warning time before barriers close
- Stuck vehicle detection on tracks

### Design Changes
- Add `RailwayCrossing` with barrier state machine
- Add `TrainDetector` sensing approaching trains
- Add `BarrierController` managing physical barriers
- Add `TrackOccupancySensor` detecting stuck vehicles

### Solution Approach
`TrainDetector` (track circuit or GPS) detects approaching train at configurable distance. On detection: start warning sequence (flashing lights, bells) -> wait minimum warning time -> lower barriers -> set all road signals to red. `TrackOccupancySensor` checks if any vehicle is on the track; if so, delay barrier closure and trigger alarm. After train passes, reverse sequence: raise barriers -> restore normal signal operation. State machine: OPEN -> WARNING -> CLOSING -> CLOSED -> OPENING -> OPEN.

### Key Classes to Add
```java
public class RailwayCrossing {
    private CrossingState state; // OPEN, WARNING, CLOSED
    private BarrierController barrier;
    private TrainDetector detector;
    private TrackOccupancySensor trackSensor;
    private Intersection controlledIntersection;

    public void onTrainApproaching(TrainInfo train) {
        state = CrossingState.WARNING;
        controlledIntersection.overrideAllRed();
        startWarningSequence();
        scheduleBarrierClose(MINIMUM_WARNING_SECONDS);
    }

    public void onTrainPassed() {
        barrier.raise();
        state = CrossingState.OPEN;
        controlledIntersection.resumeNormal();
    }
}

public class BarrierController {
    private BarrierPosition position; // UP, MOVING_DOWN, DOWN, MOVING_UP
    public void lower() { ... }
    public void raise() { ... }
}
```

---

## Variation 4: School Zone Mode
**Learning Value:** Explores trade-offs between throughput and safety using context-aware mode switching.

### Additional Requirements
- Reduced speed limits during school hours
- Longer pedestrian crossing times
- Flashing beacons for school zone awareness
- Schedule-based activation (school calendar integration)

### Design Changes
- Add `SchoolZoneSchedule` with activation times
- Add `PedestrianPhase` with extended crossing time
- Add `SpeedAdvisory` displaying reduced speed
- Modify signal timing profile for school zone mode

### Solution Approach
Define `SchoolZoneSchedule` linked to school calendar (school days, holidays, breaks). During active hours, switch to school-zone timing profile: longer pedestrian phases (e.g., 30s instead of 15s), shorter vehicle green phases, activate flashing beacons. Speed advisory signs display reduced limit. `PedestrianPhase` includes a countdown timer and audible signals. On school holidays or outside hours, revert to normal timing. Allow manual override by school resource officers.

### Key Classes to Add
```java
public class SchoolZoneSchedule {
    private LocalTime morningStart;
    private LocalTime morningEnd;
    private LocalTime afternoonStart;
    private LocalTime afternoonEnd;
    private Set<LocalDate> schoolHolidays;

    public boolean isActive(LocalDateTime now) {
        if (schoolHolidays.contains(now.toLocalDate())) return false;
        LocalTime time = now.toLocalTime();
        return (time.isAfter(morningStart) && time.isBefore(morningEnd)) ||
               (time.isAfter(afternoonStart) && time.isBefore(afternoonEnd));
    }
}

public class SchoolZoneController {
    private Intersection intersection;
    private SchoolZoneSchedule schedule;
    private TimingProfile normalProfile;
    private TimingProfile schoolZoneProfile; // longer pedestrian, shorter vehicle

    public void checkAndApply(LocalDateTime now) {
        if (schedule.isActive(now)) {
            intersection.applyTimingProfile(schoolZoneProfile);
        } else {
            intersection.applyTimingProfile(normalProfile);
        }
    }
}
```

---

## Variation 5: Multi-Intersection Network
**Learning Value:** Deepens understanding of distributed coordination, network-wide optimization, and emergent behavior.

### Additional Requirements
- Central controller optimizing signal timing across a grid
- Minimize total network delay (vehicle-hours of delay)
- Handle spillback (queue from one intersection blocking another)
- Real-time re-optimization as conditions change

### Design Changes
- Add `NetworkController` managing all intersections
- Add `TrafficGraph` representing intersection connectivity
- Add `SpillbackDetector` monitoring queue lengths
- Add `NetworkOptimizer` running global optimization

### Solution Approach
Model the road network as a directed graph where nodes are intersections and edges are road segments with capacity and current flow. `NetworkOptimizer` runs periodically: collect flow data from all sensors, solve for optimal cycle lengths and phase splits that minimize total network delay. Use a simplified version of SCOOT/SCATS algorithms. Detect spillback when a queue reaches the upstream intersection, and reduce green time for that upstream approach to prevent gridlock. Decentralized fallback if central controller fails.

### Key Classes to Add
```java
public class NetworkController {
    private TrafficGraph network;
    private List<Intersection> intersections;
    private NetworkOptimizer optimizer;

    public void optimize() {
        Map<String, TrafficData> data = collectAllSensorData();
        Map<String, TimingPlan> plans = optimizer.computeOptimalPlans(network, data);
        plans.forEach((id, plan) -> getIntersection(id).applyTimingPlan(plan));
    }
}

public class TrafficGraph {
    private Map<String, IntersectionNode> nodes;
    private Map<String, RoadSegment> edges;

    public List<String> getUpstreamIntersections(String intersectionId) { ... }
    public double getSegmentOccupancy(String fromId, String toId) { ... }
}

public class SpillbackDetector {
    private double spillbackThreshold; // e.g., 90% queue occupancy

    public boolean isSpillbackRisk(RoadSegment segment) {
        return segment.getQueueLength() / segment.getCapacity() > spillbackThreshold;
    }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

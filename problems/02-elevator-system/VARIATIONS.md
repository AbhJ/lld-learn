# Elevator System - Variations

## Variation 1: Destination Dispatch (Kiosk-Based)
**Learning Value:** Teaches destination-aware grouping algorithms and cost-function-based optimization.

### Additional Requirements
- Passengers input their destination floor at the lobby kiosk
- System groups passengers going to same/nearby floors into same elevator
- No traditional up/down buttons - only destination input
- Optimize for minimum average wait time and travel time

### Design Changes
- Replace `Direction` buttons with `DestinationPanel` at each floor
- Add `PassengerGroup` to batch passengers by destination
- Modify `ElevatorScheduler` to use destination-aware algorithm
- Add `DispatchKiosk` class for lobby input interface

### Solution Approach
When a passenger enters their destination at a kiosk, the system evaluates which elevator will result in minimum total cost (wait time + travel time) considering current elevator positions, directions, and existing passenger groups. Assign the passenger to that elevator and display the elevator letter/number. Group passengers going to nearby floors (e.g., floors 10-12) into the same car. Use a cost function: cost = waitTime * w1 + additionalStops * w2 + detourDistance * w3.

### Key Classes to Add
```java
public class DestinationDispatcher {
    private List<Elevator> elevators;
    
    public ElevatorAssignment dispatch(int sourceFloor, int destFloor) {
        return elevators.stream()
            .min(Comparator.comparing(e -> calculateCost(e, sourceFloor, destFloor)))
            .map(e -> new ElevatorAssignment(e.getId(), estimateWaitTime(e)))
            .orElseThrow();
    }
    
    private double calculateCost(Elevator e, int source, int dest) { ... }
}

public class PassengerGroup {
    private int destinationFloor;
    private List<Passenger> passengers;
    private Elevator assignedElevator;
}
```

---

## Variation 2: Freight Elevator
**Learning Value:** Introduces weight-based capacity management, sensor integration, and priority scheduling.

### Additional Requirements
- Weight-based capacity (not just person count)
- Priority scheduling for freight vs passenger requests
- Loading/unloading time buffer at each stop
- Overweight alarm and door-hold prevention

### Design Changes
- Add `FreightElevator extends Elevator` with weight sensor
- Add `LoadManifest` tracking current weight and items
- Modify scheduling to account for loading time at stops
- Add `WeightSensor` that prevents door closing when overloaded

### Solution Approach
Create `FreightElevator` with a `maxWeightCapacity` and real-time weight tracking via `WeightSensor`. Each stop includes configurable loading/unloading buffer time. Scheduling algorithm prioritizes freight requests during off-peak hours and passenger requests during peak hours. When a freight request comes in, calculate if elevator can handle the weight. If overloaded, alarm triggers and doors stay open until weight is reduced.

### Key Classes to Add
```java
public class FreightElevator extends Elevator {
    private double maxWeightKg;
    private WeightSensor weightSensor;
    private Duration loadingBufferTime;

    @Override
    public boolean canAcceptLoad(double additionalWeight) {
        return weightSensor.getCurrentWeight() + additionalWeight <= maxWeightKg;
    }
}

public class WeightSensor {
    private double currentWeightKg;
    private double alarmThreshold;

    public boolean isOverloaded() { return currentWeightKg > alarmThreshold; }
    public void onWeightChange(double newWeight) { ... }
}
```

---

## Variation 3: Emergency Mode
**Learning Value:** Practices state machine design with multiple emergency modes and fail-safe transitions.

### Additional Requirements
- Fire alarm triggers all elevators to ground floor
- Fireman override mode - manual control of single elevator
- Earthquake mode - stop at nearest floor and open doors
- Power failure - battery backup to nearest floor

### Design Changes
- Add `EmergencyController` with mode state machine
- Add `EmergencyMode` enum (NORMAL, FIRE, EARTHQUAKE, POWER_FAILURE)
- Add `FiremanOverride` allowing manual floor-by-floor control
- Modify `Elevator` to support emergency state transitions

### Solution Approach
Implement an `EmergencyController` that monitors building alarm systems. On fire alarm: cancel all pending requests, move all elevators to ground floor without stopping, open doors, disable call buttons. Fireman override: one designated elevator enters manual mode controlled by key switch - goes only where fireman directs. Earthquake mode: all elevators stop at nearest floor, open doors, shut down. Use State pattern for clean transitions between modes.

### Key Classes to Add
```java
public class EmergencyController {
    private EmergencyMode currentMode;
    private List<Elevator> allElevators;
    private Elevator firemanElevator;

    public void activateFireMode() {
        currentMode = EmergencyMode.FIRE;
        allElevators.forEach(e -> e.recallToGround());
        disableAllCallButtons();
    }

    public void activateFiremanOverride(String elevatorId) {
        firemanElevator = getElevator(elevatorId);
        firemanElevator.enterManualMode();
    }
}

public enum EmergencyMode {
    NORMAL, FIRE, EARTHQUAKE, POWER_FAILURE, FIREMAN_OVERRIDE
}
```

---

## Variation 4: VIP/Express Elevator
**Learning Value:** Explores trade-offs between access control, priority queuing, and zone-based resource partitioning.

### Additional Requirements
- Express elevators serving only certain floors (e.g., lobby + floors 20-30)
- VIP access via keycard for executive floors
- Skip intermediate floors during express mode
- Priority queuing for VIP passengers

### Design Changes
- Add `ExpressElevator extends Elevator` with allowed floor set
- Add `AccessControl` with keycard/badge authentication
- Add `VIPRequest` with higher priority in scheduling queue
- Modify `ElevatorScheduler` to respect floor restrictions

### Solution Approach
Define `ExpressElevator` that only stops at configured floors (typically lobby + high floors). Passengers without VIP access get regular elevators. VIP requests jump the scheduling queue. Access control verifies badge before allowing floor selection on express elevators. Regular elevators handle the remaining floors. Split elevator banks: low-rise (1-15), mid-rise (1, 16-30), high-rise/express (1, 31-50). Route passengers to correct bank based on destination.

### Key Classes to Add
```java
public class ExpressElevator extends Elevator {
    private Set<Integer> allowedFloors;
    private AccessControl accessControl;

    @Override
    public boolean canServiceFloor(int floor) {
        return allowedFloors.contains(floor);
    }

    public boolean requestFloor(int floor, BadgeCredential badge) {
        if (!accessControl.hasAccess(badge, floor)) return false;
        return addDestination(floor);
    }
}

public class ElevatorBank {
    private String bankName; // "Low-rise", "High-rise", "Express"
    private List<Elevator> elevators;
    private Set<Integer> serviceFloors;
}
```

---

## Variation 5: Energy-Efficient Scheduling
**Learning Value:** Deepens understanding of energy-aware scheduling, statistical positioning, and fleet-level optimization.

### Additional Requirements
- Minimize total travel distance across all elevators
- Idle elevators positioned at strategic "home" floors
- Off-peak mode: reduce active elevators to save power
- Regenerative braking energy tracking

### Design Changes
- Add `EnergyOptimizer` that calculates optimal idle positions
- Add `SchedulingMetrics` tracking energy consumption
- Modify scheduler to minimize total system movement
- Add `PeakDetector` to toggle between peak/off-peak modes

### Solution Approach
Implement a cost function that considers not just wait time but also total energy (proportional to distance traveled). During off-peak, shut down some elevators and position remaining ones at statistically optimal "home" floors (based on historical call data). Use a look-ahead algorithm: when a new request arrives, evaluate all possible assignments and pick the one minimizing total fleet movement. Track energy via `EnergyMeter` and report savings.

### Key Classes to Add
```java
public class EnergyOptimizer {
    private Map<Integer, Double> floorCallFrequency; // historical data
    private int activeElevatorCount;

    public List<Integer> calculateOptimalIdlePositions(int numElevators) {
        // K-means clustering on historical call floors
        return clusterCenters(floorCallFrequency, numElevators);
    }

    public ElevatorAssignment assignWithMinEnergy(Request request, List<Elevator> elevators) {
        return elevators.stream()
            .min(Comparator.comparing(e -> energyCost(e, request)))
            .map(e -> new ElevatorAssignment(e, energyCost(e, request)))
            .orElseThrow();
    }
}

public class PeakDetector {
    private TimeBasedPattern pattern;
    public boolean isPeakHour(LocalTime time) { ... }
    public int recommendedActiveElevators(LocalTime time) { ... }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

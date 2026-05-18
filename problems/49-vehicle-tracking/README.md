# Vehicle Tracking


## Problem Statement
Design a fleet vehicle tracking system that ingests GPS updates, computes speed between updates, raises alerts on speeding, and detects entry/exit of geofenced regions.

The naive variant stores unbounded location history and scans all vehicles for geofence checks. The optimized variant uses a circular buffer for history and a spatial grid index for fast lookups.

## Requirements

### Functional Requirements
- Register vehicles with type and ID
- Ingest location updates with timestamps
- Compute current speed from consecutive points
- Define geofences (circular regions)
- Raise alerts on speeding and on geofence enter/exit
- Query alerts and per-vehicle history

### Non-functional Requirements
- Bounded memory per vehicle (circular buffer in optimized)
- O(1) spatial lookup via grid index (optimized)

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Observer | AlertListener / LoggingAlertListener | Subscribers notified on speeding and geofence enter/exit alerts |
| Spatial Index | SpatialGrid (optimized) | Fast lookup of nearby vehicles/fences |
| Circular Buffer | CircularBuffer (optimized) | Cap location history memory |
| Facade | TrackingService | Unified API over vehicles, fences, alerts |

## Folder Structure

```
49-vehicle-tracking/
├── README.md
├── VARIATIONS.md
├── naive/
│   ├── model/        ← Location, Vehicle, VehicleState, GeoFence, Alert
│   ├── service/      ← TrackingService, SpeedMonitor
│   └── Main.java
└── optimized/
    ├── model/        ← Location, Vehicle (dedup), VehicleState, GeoFence (bbox), Alert, CircularBuffer
    ├── service/      ← TrackingService, SpeedMonitor, SpatialGrid
    └── Main.java
```

## How to Run

```bash
# Naive
cd problems/49-vehicle-tracking/naive
mkdir -p out && javac -d out model/*.java service/*.java Main.java && java -cp out Main

# Optimized
cd problems/49-vehicle-tracking/optimized
mkdir -p out && javac -d out model/*.java service/*.java Main.java && java -cp out Main
```

## Naive vs Optimized

| Aspect | Naive | Optimized |
|--------|-------|-----------|
| Geofence check | O(n) scan ALL geofences | O(1) spatial grid index for candidate fences |
| Location storage | Unbounded ArrayList (memory leak) | Circular buffer with fixed max size |
| GPS noise | Stores every point | Distance-based dedup (< 5m ignored) |
| Geofence.contains | Always computes Haversine distance | Bounding box fast-reject, then distance |

---

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** GPS update arriving while geofence check is running on previous location — alert based on stale position.

```bash
cd concurrent
mkdir -p out
find . -name '*.java' | xargs javac -d out
java -cp out Main
```

### Key Concurrency Techniques Used
| Technique | Where | Why |
|-----------|-------|-----|
| AtomicReference<Location> | VehicleTracker.latestLocation | Always reads latest GPS position — no stale data |
| CAS-based update | VehicleTracker.updateLocation() | Only accepts higher sequence numbers — rejects out-of-order GPS |
| Immutable Location | Location class | Thread-safe by construction — geofence reads are always consistent |
| CopyOnWriteArrayList | VehicleTracker.alerts | Safe alert accumulation from concurrent geofence checks |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

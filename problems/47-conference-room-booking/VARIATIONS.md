# Conference Room Booking - Variations

## Variation 1: Smart Release
**Learning Value:** Teaches automatic resource release, occupancy detection, and no-show handling workflows.

### Additional Requirements
- Auto-release unconfirmed bookings after grace period
- Check-in via QR code or app
- No-show tracking and penalties
- Early release if meeting ends early

### Design Changes
- Add `CheckInService` with QR verification
- Add `SmartReleasePolicy` with grace period logic
- Add `NoShowTracker` recording patterns
- Modify `Booking` to include confirmation status

### Solution Approach
Each booking has a `ConfirmationStatus` (PENDING, CONFIRMED, RELEASED). A grace period timer starts at the booking's start time (e.g., 10 minutes). If no one checks in via QR scan or app tap, `SmartReleasePolicy` auto-releases the room, making it available for walk-ins. `NoShowTracker` records no-shows per user; repeated no-shows may reduce booking priority. Users can also release early via the app, freeing the room for the remainder.

### Key Classes to Add
```java
public class CheckInService {
    private Map<String, Booking> activeBookings;

    public boolean checkIn(String bookingId, String qrCode) {
        // Validate QR matches booking, mark as CONFIRMED
    }

    public void releaseEarly(String bookingId) { /* Free room for remaining time */ }
}

public class SmartReleasePolicy {
    private Duration gracePeriod;
    private NoShowTracker noShowTracker;

    public void processUnconfirmedBookings() {
        // Find bookings past grace period without check-in, release them
    }
}

public class NoShowTracker {
    private Map<String, Integer> noShowCounts;
    public void recordNoShow(String userId) { /* Increment count */ }
    public int getNoShowCount(String userId) { /* Lookup */ }
}
```

---

## Variation 2: Hybrid Meeting (Virtual + Physical)
**Learning Value:** Introduces hybrid resource coordination, virtual meeting integration, and dual-mode booking.

### Additional Requirements
- Automatically add video conference link
- Track physical and virtual capacity separately
- Hardware requirements (camera, display, speaker)
- Join link in calendar invite

### Design Changes
- Add `VideoConferenceService` for link generation
- Add `HybridMeeting` extending Booking
- Add `RoomHardware` tracking AV equipment
- Modify `Room` to include hybrid capabilities

### Solution Approach
When a booking is created as hybrid, the system automatically generates a video conference link via `VideoConferenceService` (Zoom, Teams, etc.) and attaches it to the booking. The room must have the required `RoomHardware` (camera, microphone, display). Physical capacity is the room's seat count; virtual capacity is the platform's limit. The booking invitation includes both the room location and join link. Check that physical attendees fit in room capacity.

### Key Classes to Add
```java
public class HybridMeeting extends Booking {
    private String videoLink;
    private int physicalAttendees;
    private int virtualAttendees;
    private List<String> requiredHardware;
}

public class VideoConferenceService {
    public String generateMeetingLink(Booking booking) { /* Create Zoom/Teams link */ }
    public void updateLink(String bookingId, String newLink) { /* Modify */ }
}

public class RoomHardware {
    private String roomId;
    private boolean hasCamera;
    private boolean hasDisplay;
    private boolean hasSpeakerphone;
    private int displayCount;
    public boolean meetsRequirements(List<String> requirements) { /* Check all present */ }
}
```

---

## Variation 3: Visitor Management
**Learning Value:** Practices visitor lifecycle management, pre-registration workflows, and access provisioning.

### Additional Requirements
- Register external guests for meetings
- Lobby notifications when guest arrives
- NDA/document signing on arrival
- Visitor badge printing

### Design Changes
- Add `Visitor` class for external guests
- Add `VisitorManagementService` for registration
- Add `LobbyNotification` alerting the host
- Add `DocumentSigning` for NDAs

### Solution Approach
When creating a booking with external guests, the organizer registers `Visitor` objects (name, email, company). The system sends pre-visit emails with directions and WiFi info. When a visitor arrives at reception, they check in via tablet — `LobbyNotification` alerts the host. If NDA signing is required, `DocumentSigning` presents the document digitally before granting access. A visitor badge is generated with name, host, and meeting room.

### Key Classes to Add
```java
public class Visitor {
    private String name;
    private String email;
    private String company;
    private String hostUserId;
    private String bookingId;
    private VisitorStatus status; // EXPECTED, CHECKED_IN, CHECKED_OUT
}

public class VisitorManagementService {
    public void registerVisitor(Visitor visitor, Booking booking) { /* Pre-register */ }
    public void checkInVisitor(String visitorId) { /* Mark arrived, notify host */ }
    public void checkOutVisitor(String visitorId) { /* Mark departed */ }
    public Badge generateBadge(Visitor visitor) { /* Create printable badge */ }
}

public class LobbyNotification {
    public void notifyHost(String hostUserId, Visitor visitor) { /* Push notification */ }
}
```

---

## Variation 4: Analytics / Utilization
**Learning Value:** Explores trade-offs between data granularity and privacy in space utilization analytics.

### Additional Requirements
- Room usage reports (% utilization)
- Peak hours analysis
- Cost per room per meeting
- Underutilized room detection

### Design Changes
- Add `AnalyticsService` computing metrics
- Add `UsageReport` with visualizable data
- Add `CostTracker` per room
- Add `OccupancySensor` integration (optional)

### Solution Approach
The `AnalyticsService` aggregates booking data over configurable time ranges. It computes utilization rate (booked hours / available hours), identifies peak hours and days, and detects underutilized rooms (consistently low bookings). `CostTracker` assigns operating costs to rooms (rent per sqft, utilities, AV maintenance) and divides by meeting count for cost-per-meeting. Reports can be generated per room, per floor, or per department. Data feeds into decisions about room allocation or closure.

### Key Classes to Add
```java
public class AnalyticsService {
    private BookingSystem bookingSystem;

    public UsageReport getUtilization(String roomId, LocalDate start, LocalDate end) {
        // Calculate booked vs available hours
    }

    public Map<Integer, Double> getPeakHours(LocalDate start, LocalDate end) {
        // Hour of day -> average bookings
    }

    public List<Room> getUnderutilizedRooms(double thresholdPercent) { /* Below threshold */ }
}

public class UsageReport {
    private String roomId;
    private double utilizationPercent;
    private int totalBookings;
    private Duration totalBookedTime;
    private Map<String, Integer> bookingsByDepartment;
}
```

---

## Variation 5: Multi-Floor / Building
**Learning Value:** Deepens understanding of multi-location resource discovery, cross-building coordination, and campus-wide booking.

### Additional Requirements
- Campus-wide room search across buildings
- Travel time between buildings
- Floor preference and proximity
- Building-specific amenities and access

### Design Changes
- Add `Building` and `Floor` hierarchy
- Add `CampusService` for cross-building search
- Add `TravelTimeCalculator` between locations
- Modify `RoomFinder` to consider proximity

### Solution Approach
Model a hierarchy: Campus -> Building -> Floor -> Room. `CampusService` provides a unified search across all buildings. `TravelTimeCalculator` estimates walking time between buildings (precomputed graph). When a user searches for a room, results are ranked by proximity to the user's usual building/floor. If a meeting has attendees from different buildings, suggest a central location minimizing total travel. Building access controls ensure users can only book rooms they have access to.

### Key Classes to Add
```java
public class Building {
    private String id;
    private String name;
    private String address;
    private List<Floor> floors;
    private List<String> amenities;
}

public class CampusService {
    private List<Building> buildings;
    private TravelTimeCalculator travelCalc;

    public List<Room> searchAcrossCampus(int capacity, TimeSlot slot, String preferredBuilding) {
        // Search all buildings, rank by proximity and preference
    }

    public Room suggestOptimalLocation(List<String> attendeeBuildings, int capacity, TimeSlot slot) {
        // Minimize total travel time for all attendees
    }
}

public class TravelTimeCalculator {
    private Map<String, Map<String, Duration>> travelTimes; // buildingId -> buildingId -> time
    public Duration getWalkingTime(String fromBuilding, String toBuilding) { /* Lookup */ }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

# Calendar System - Variations

## Variation 1: Scheduling Assistant (Find Free Time)
**Learning Value:** Teaches free-busy computation, availability intersection, and optimal meeting slot discovery.

### Additional Requirements
- Query multiple users' calendars for availability
- Find optimal meeting slots for a group
- Respect working hours preferences
- Suggest ranked time slots

### Design Changes
- Add `SchedulingAssistant` service class
- Add `Availability` and `FreeSlot` classes
- Add `WorkingHours` preference per user
- Modify `CalendarService` to expose availability queries

### Solution Approach
The `SchedulingAssistant` takes a list of attendees and a desired duration. For each attendee, it fetches their events for the target date range and computes free slots by inverting busy intervals. It then intersects all attendees' free slots to find common availability. Slots are ranked by proximity to preferred time, number of attendees available, and working hours compliance. Return the top N suggestions.

### Key Classes to Add
```java
public class SchedulingAssistant {
    private CalendarService calendarService;

    public List<FreeSlot> findCommonFreeSlots(List<String> attendeeIds, Duration duration, LocalDate start, LocalDate end) {
        // Fetch events for each attendee, compute free intervals, intersect
    }

    public List<FreeSlot> suggestOptimalSlots(List<String> attendeeIds, Duration duration, int maxSuggestions) {
        // Rank by preference score
    }
}

public class FreeSlot {
    private LocalDateTime start;
    private LocalDateTime end;
    private double score; // ranking score
}
```

---

## Variation 2: Resource Booking
**Learning Value:** Introduces physical resource management, capacity constraints, and shared asset booking.

### Additional Requirements
- Book rooms, projectors, vehicles alongside events
- Resource availability checking
- Conflict prevention for resources
- Resource capacity and attributes

### Design Changes
- Add `Resource` class with type, capacity, attributes
- Add `ResourceBooking` linking event to resource
- Add `ResourceManager` for availability and booking
- Modify `Event` to include resource requirements

### Solution Approach
Create a `Resource` hierarchy (Room, Equipment, Vehicle) each with attributes (capacity, location, features). When creating an event, the user specifies required resources. `ResourceManager` checks availability against existing bookings, prevents double-booking via locking, and creates a `ResourceBooking` record. On event cancellation, the resource is released. Support searching resources by attributes (e.g., "room with projector for 10 people").

### Key Classes to Add
```java
public class Resource {
    private String id;
    private String name;
    private ResourceType type; // ROOM, PROJECTOR, VEHICLE
    private int capacity;
    private Map<String, String> attributes;
}

public class ResourceManager {
    private Map<String, Resource> resources;
    private Map<String, List<ResourceBooking>> bookings;

    public boolean isAvailable(String resourceId, TimeSlot slot) { /* Check conflicts */ }
    public ResourceBooking book(String resourceId, String eventId, TimeSlot slot) { /* Reserve */ }
    public List<Resource> findByAttributes(Map<String, String> criteria) { /* Search */ }
}
```

---

## Variation 3: Timezone Handling
**Learning Value:** Practices timezone-aware scheduling, DST handling, and cross-timezone coordination.

### Additional Requirements
- Events spanning multiple timezones
- Travel detection and timezone adjustment
- Daylight Saving Time transitions
- Display in user's local timezone

### Design Changes
- Add `TimezoneService` for conversions
- Modify `Event` to store timezone with start/end
- Add `TravelEvent` that carries timezone transitions
- Add `DSTHandler` for edge cases

### Solution Approach
Store all event times in UTC internally with an associated timezone ID (e.g., "America/New_York"). The `TimezoneService` handles conversion to/from user's display timezone. For travel events, detect timezone changes and adjust subsequent events. Handle DST by using `ZonedDateTime` which automatically accounts for transitions. When displaying recurring events, recompute each occurrence in the local timezone to handle DST shifts correctly.

### Key Classes to Add
```java
public class TimezoneService {
    public ZonedDateTime convertToUserTimezone(Instant utcTime, ZoneId userZone) { /* Convert */ }
    public Instant toUTC(LocalDateTime localTime, ZoneId sourceZone) { /* Normalize */ }
    public boolean crossesDST(ZonedDateTime start, ZonedDateTime end) { /* Check transition */ }
    public ZoneId detectTimezone(Location location) { /* Geo to timezone */ }
}

public class TravelEvent extends Event {
    private ZoneId departureZone;
    private ZoneId arrivalZone;
    public Duration getTimezoneOffset() { /* Calculate difference */ }
}
```

---

## Variation 4: Calendar Sync (CalDAV)
**Learning Value:** Explores trade-offs between data ownership and synchronization in calendar protocol integration.

### Additional Requirements
- Two-way sync with external calendars
- Conflict resolution on simultaneous edits
- Import/export in iCal format
- Periodic sync with configurable frequency

### Design Changes
- Add `CalDAVClient` for external sync
- Add `SyncEngine` with conflict resolution strategy
- Add `ICalParser` for import/export
- Add `SyncState` to track per-calendar sync status

### Solution Approach
The `SyncEngine` maintains a `SyncState` per external calendar (last sync token, etag). On sync, it fetches remote changes since last token, fetches local changes since last sync, and applies conflict resolution (last-write-wins, or user-prompt for conflicts). The `ICalParser` converts between internal Event objects and iCalendar format. Schedule periodic sync via a timer, with immediate sync on local changes.

### Key Classes to Add
```java
public class SyncEngine {
    private CalDAVClient client;
    private ICalParser parser;
    private Map<String, SyncState> syncStates;

    public SyncResult sync(String calendarId) { /* Fetch remote, merge, push */ }
    public ConflictResolution resolveConflict(Event local, Event remote) { /* Strategy */ }
}

public class SyncState {
    private String calendarId;
    private String syncToken;
    private LocalDateTime lastSynced;
    private SyncStatus status; // IN_SYNC, PENDING, CONFLICT
}
```

---

## Variation 5: Working Hours / Out of Office
**Learning Value:** Deepens understanding of recurring availability patterns, exception handling, and schedule boundary enforcement.

### Additional Requirements
- Define working hours per user per day
- Auto-decline meetings outside working hours
- Vacation/OOO with delegation
- Respect working hours in scheduling suggestions

### Design Changes
- Add `WorkingHours` configuration per user
- Add `OutOfOffice` period with delegate
- Add `AutoDeclinePolicy` for enforcement
- Modify `SchedulingAssistant` to respect hours

### Solution Approach
Each user configures `WorkingHours` (start, end, days of week, timezone). When an invitation arrives outside working hours, the `AutoDeclinePolicy` checks the policy and auto-declines with a message. For OOO periods, the user sets a delegate who receives forwarded invitations. The scheduling assistant filters out non-working-hours when suggesting slots. OOO shows as a blocking all-day event to others querying availability.

### Key Classes to Add
```java
public class WorkingHours {
    private String userId;
    private Map<DayOfWeek, TimeSlot> hours;
    private ZoneId timezone;

    public boolean isWithinHours(ZonedDateTime dateTime) { /* Check */ }
}

public class OutOfOffice {
    private String userId;
    private LocalDate start;
    private LocalDate end;
    private String delegateUserId;
    private String autoReplyMessage;

    public boolean isActive(LocalDate date) { /* Check range */ }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

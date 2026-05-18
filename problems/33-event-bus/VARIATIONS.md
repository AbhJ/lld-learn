# Event Bus - Variations

## Variation 1: Distributed Event Bus
**Learning Value:** Teaches distributed event routing, network partitioning, and cross-service event delivery.

### Additional Requirements
- Cross-service event delivery over the network
- Eventual consistency guarantees
- Idempotent event handling (at-least-once delivery)
- Partition tolerance and ordering within partitions

### Design Changes
- Add NetworkTransport layer (gRPC/HTTP) for cross-service delivery
- Add EventSerializer for wire format (Protobuf/JSON)
- Add IdempotencyStore to deduplicate events by ID
- Add PartitionRouter to maintain ordering per entity

### Solution Approach
Each service has a local event bus that also publishes to a distributed commit log. Events are serialized with a unique ID, timestamp, and source service. The transport layer delivers events to subscriber services. Each subscriber maintains an idempotency store (event ID set with TTL) to handle duplicates from at-least-once delivery. Events are partitioned by entity ID to maintain ordering within an entity. Use vector clocks or Lamport timestamps for causal ordering across services. Implement a retry mechanism with exponential backoff for failed deliveries.

### Key Classes to Add
```java
public class DistributedEventBus extends EventBus {
    private final NetworkTransport transport;
    private final EventSerializer serializer;
    private final IdempotencyStore idempotencyStore;
    
    public void publish(Event event) {
        event.setId(UUID.randomUUID().toString());
        event.setTimestamp(System.currentTimeMillis());
        super.publish(event); // local delivery
        transport.broadcast(serializer.serialize(event)); // remote delivery
    }
    
    public void onRemoteEvent(byte[] data) {
        Event event = serializer.deserialize(data);
        if (idempotencyStore.isDuplicate(event.getId())) return;
        idempotencyStore.record(event.getId());
        super.publish(event);
    }
}
```

---

## Variation 2: Event Sourcing
**Learning Value:** Introduces append-only event storage, state reconstruction, and temporal querying patterns.

### Additional Requirements
- Store all events as the source of truth
- Rebuild aggregate state by replaying events
- Snapshots for performance (avoid replaying full history)
- Append-only event store with versioning

### Design Changes
- Add EventStore (append-only persistent log)
- Add Aggregate base class that applies events to rebuild state
- Add SnapshotStore for periodic state snapshots
- Add EventStream per aggregate ID

### Solution Approach
Instead of storing current state, store every event that changed the state. An EventStore is an append-only log partitioned by aggregate ID. To get current state, load the event stream for an aggregate and replay all events through the aggregate's apply() method. For performance, periodically save snapshots; on load, start from the latest snapshot and replay only subsequent events. This gives a complete audit trail, enables temporal queries ("what was the state at time T?"), and supports rebuilding read models from events.

### Key Classes to Add
```java
public class EventStore {
    private final Map<String, List<StoredEvent>> streams = new ConcurrentHashMap<>();
    
    public void append(String aggregateId, Event event, int expectedVersion) {
        // Optimistic concurrency check
        List<StoredEvent> stream = streams.computeIfAbsent(aggregateId, k -> new ArrayList<>());
        if (stream.size() != expectedVersion) throw new ConcurrencyException();
        stream.add(new StoredEvent(event, stream.size(), System.currentTimeMillis()));
    }
    
    public List<StoredEvent> getStream(String aggregateId, int fromVersion) {
        return streams.getOrDefault(aggregateId, List.of())
            .stream().filter(e -> e.getVersion() >= fromVersion).toList();
    }
}

public abstract class Aggregate {
    private int version;
    protected abstract void apply(Event event);
    
    public void rehydrate(List<StoredEvent> events) {
        events.forEach(e -> { apply(e.getEvent()); version = e.getVersion(); });
    }
}
```

---

## Variation 3: Saga/Choreography
**Learning Value:** Practices distributed transaction coordination, compensation logic, and eventual consistency patterns.

### Additional Requirements
- Multi-step distributed transactions via events
- Compensating transactions on failure (rollback)
- Saga state tracking and timeout handling
- Both choreography (event-driven) and orchestration patterns

### Design Changes
- Add SagaDefinition with steps and compensations
- Add SagaState tracker per saga instance
- Add CompensatingEvent for rollback
- Add SagaTimeout for stuck sagas

### Solution Approach
Define a saga as a sequence of steps, each with a forward action and a compensating action. In choreography, each service listens for events and publishes the next event (or a failure event). On failure, compensating events are published in reverse order. Track saga state (which steps completed) in a SagaLog. If a step times out, trigger compensation. In orchestration, a central SagaOrchestrator sends commands and awaits replies, deciding the next step. Use correlation IDs to link all events in a saga instance.

### Key Classes to Add
```java
public class SagaDefinition {
    private final List<SagaStep> steps;
    
    public static class SagaStep {
        String name;
        String forwardEventType;
        String compensateEventType;
        long timeoutMs;
    }
}

public class SagaOrchestrator {
    private final EventBus eventBus;
    private final Map<String, SagaInstance> activeSagas = new ConcurrentHashMap<>();
    
    public void startSaga(SagaDefinition definition, Object payload) {
        String sagaId = UUID.randomUUID().toString();
        SagaInstance instance = new SagaInstance(sagaId, definition);
        activeSagas.put(sagaId, instance);
        publishNextStep(instance);
    }
    
    public void onStepCompleted(String sagaId, Event event) {
        SagaInstance saga = activeSagas.get(sagaId);
        saga.markStepComplete(saga.getCurrentStep());
        if (saga.hasNextStep()) publishNextStep(saga);
        else completeSaga(saga);
    }
    
    public void onStepFailed(String sagaId, Event event) {
        SagaInstance saga = activeSagas.get(sagaId);
        compensate(saga); // publish compensating events in reverse
    }
}
```

---

## Variation 4: Event Replay
**Learning Value:** Explores trade-offs between storage cost and debugging power in event replay and reprocessing.

### Additional Requirements
- Replay events from a specific timestamp or sequence number
- Selective replay (filter by event type or aggregate)
- Speed control (replay at 10x, pause, resume)
- Side-effect-free replay mode for debugging

### Design Changes
- Add ReplayController with cursor management
- Add ReplayMode flag to prevent side effects during replay
- Add EventFilter for selective replay
- Add SpeedController for replay rate

### Solution Approach
The EventStore maintains a global sequence number for all events. A ReplayController can seek to any position and replay events forward. During replay, set a context flag (ReplayMode) so handlers know not to trigger side effects (no emails, no external API calls). Filters allow replaying only specific event types or aggregates. Speed control uses a rate limiter to throttle event emission. Use cases: rebuild a corrupted read model, debug production issues by replaying the exact sequence, or populate a new service with historical data.

### Key Classes to Add
```java
public class ReplayController {
    private final EventStore store;
    private final EventBus targetBus;
    private long currentPosition;
    private ReplaySpeed speed = ReplaySpeed.NORMAL;
    private boolean paused;
    
    public void replayFrom(long timestamp) {
        ReplayContext.setReplayMode(true);
        List<StoredEvent> events = store.getEventsAfter(timestamp);
        for (StoredEvent event : events) {
            if (paused) waitForResume();
            speed.throttle();
            targetBus.publish(event.getEvent());
            currentPosition = event.getSequenceNumber();
        }
        ReplayContext.setReplayMode(false);
    }
    
    public void replayFiltered(long from, EventFilter filter) {
        store.getEventsAfter(from).stream()
            .filter(filter::matches)
            .forEach(e -> targetBus.publish(e.getEvent()));
    }
}
```

---

## Variation 5: Schema Versioning
**Learning Value:** Deepens understanding of backward/forward compatibility, event versioning, and contract evolution.

### Additional Requirements
- Evolve event schemas without breaking consumers
- Backward compatibility (new readers, old events)
- Forward compatibility (old readers, new events)
- Schema registry with version tracking

### Design Changes
- Add SchemaRegistry to store and validate schemas
- Add EventUpcaster to transform old events to new format
- Add CompatibilityChecker for schema evolution rules
- Add versioned serialization/deserialization

### Solution Approach
Maintain a SchemaRegistry that stores all versions of each event type's schema. When a new schema version is registered, check compatibility (backward: new code can read old events; forward: old code can read new events). Use upcasters to transform events from old versions to the latest schema on read. Events are stored with their schema version. On deserialization, if the stored version differs from the latest, run the event through a chain of upcasters (v1->v2->v3). This allows schema evolution without migrating historical events.

### Key Classes to Add
```java
public class SchemaRegistry {
    private final Map<String, List<Schema>> schemas = new HashMap<>();
    
    public void register(String eventType, Schema schema, CompatibilityMode mode) {
        List<Schema> versions = schemas.computeIfAbsent(eventType, k -> new ArrayList<>());
        if (!versions.isEmpty()) {
            Schema latest = versions.get(versions.size() - 1);
            if (!isCompatible(latest, schema, mode)) throw new IncompatibleSchemaException();
        }
        versions.add(schema);
    }
}

public class UpcasterChain {
    private final List<Upcaster> upcasters; // ordered v1->v2, v2->v3, etc.
    
    public Event upcast(Event event, int fromVersion, int toVersion) {
        Event current = event;
        for (int v = fromVersion; v < toVersion; v++) {
            current = upcasters.get(v).upcast(current);
        }
        return current;
    }
}

public interface Upcaster {
    Event upcast(Event oldVersionEvent);
    int getFromVersion();
    int getToVersion();
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

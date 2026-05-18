# Pub-Sub System - Variations

## Variation 1: Distributed Pub-Sub (Partitioned Log)
**Learning Value:** Teaches distributed message brokering, partition-based parallelism, and consumer group coordination.

### Additional Requirements
- Topic partitioning for parallel consumption
- Consumer groups with partition assignment
- Offset management (committed position per consumer)
- At-least-once/exactly-once delivery guarantees
- Message retention (time-based and size-based)
- Partition rebalancing when consumers join/leave

### Design Changes
- Add `Partition` class for topic subdivision
- Add `ConsumerGroup` with partition assignment strategy
- Add `OffsetManager` for tracking consumer position
- Add `RetentionPolicy` for message lifecycle management
- Add `RebalanceStrategy` (range, round-robin, sticky)
- Modify `Topic` to contain ordered partitions

### Solution Approach
Each `Topic` is divided into N `Partition` objects, each an ordered, append-only log. Messages are assigned to partitions by key hash (same key always goes to same partition, ensuring ordering per key). A `ConsumerGroup` assigns partitions to consumers such that each partition has exactly one consumer. The `OffsetManager` tracks each consumer's committed offset (last successfully processed message). When a consumer joins/leaves, `RebalanceStrategy` redistributes partitions. Messages are retained based on `RetentionPolicy` (e.g., 7 days or 1GB per partition), allowing consumers to replay. Delivery guarantee is achieved by committing offsets only after successful processing.

### Key Classes to Add
```java
public class Partition {
    private int partitionId;
    private List<Message> log; // append-only
    private long currentOffset;

    public long append(Message message) {
        message.setOffset(currentOffset++);
        log.add(message);
        return message.getOffset();
    }

    public List<Message> fetch(long fromOffset, int maxMessages) {
        int startIdx = (int) fromOffset;
        int endIdx = Math.min(startIdx + maxMessages, log.size());
        return log.subList(startIdx, endIdx);
    }
}

public class ConsumerGroup {
    private String groupId;
    private List<Consumer> consumers;
    private Map<Consumer, List<Partition>> assignment;
    private RebalanceStrategy strategy;
    private OffsetManager offsetManager;

    public void rebalance(List<Partition> partitions) {
        assignment = strategy.assign(consumers, partitions);
        // Each consumer processes only its assigned partitions
    }

    public void commitOffset(Consumer consumer, Partition partition, long offset) {
        offsetManager.commit(groupId, partition.getPartitionId(), offset);
    }
}
```

---

## Variation 2: Dead Letter Queue
**Learning Value:** Introduces failure handling patterns, poison message isolation, and retry exhaustion workflows.

### Additional Requirements
- Route failed messages to a dead letter queue after max retries
- Retry with exponential backoff before DLQ routing
- Poison pill detection (messages that always fail)
- DLQ monitoring and alerting
- Manual replay from DLQ (reprocess dead letters)
- Per-subscriber failure tracking

### Design Changes
- Add `DeadLetterQueue` as a special topic for failed messages
- Add `RetryPolicy` with configurable backoff
- Add `FailureTracker` counting attempts per message
- Add `DLQMonitor` for alerting on DLQ growth
- Add `DLQReplayService` for reprocessing dead letters
- Modify message delivery to wrap with retry logic

### Solution Approach
Each subscriber has a `RetryPolicy` (max attempts, backoff multiplier, initial delay). When a subscriber fails to process a message, the `FailureTracker` increments the attempt count. If below max retries, the message is re-queued with a delay (exponential backoff: delay * 2^attempt). If max retries are exceeded, the message is moved to the `DeadLetterQueue` along with failure metadata (error message, stack trace, attempt history). The `DLQMonitor` alerts when DLQ size exceeds a threshold. The `DLQReplayService` allows operators to reprocess DLQ messages (after fixing the underlying issue) either individually or in bulk.

### Key Classes to Add
```java
public class DeadLetterQueue {
    private String dlqTopicName;
    private List<DeadLetter> letters;
    private DLQMonitor monitor;

    public void route(Message message, String subscriberId, FailureRecord failure) {
        DeadLetter letter = new DeadLetter(message, subscriberId, failure);
        letters.add(letter);
        monitor.onNewDeadLetter(letter);
    }

    public List<DeadLetter> getLetters(String subscriberId) {
        return letters.stream()
            .filter(l -> l.getSubscriberId().equals(subscriberId))
            .collect(Collectors.toList());
    }
}

public class RetryPolicy {
    private int maxAttempts = 3;
    private long initialDelayMs = 1000;
    private double backoffMultiplier = 2.0;
    private DeadLetterQueue dlq;

    public void handleFailure(Message message, String subscriberId, Exception error) {
        FailureRecord record = FailureTracker.record(message.getMessageId(), error);
        if (record.getAttempts() >= maxAttempts) {
            dlq.route(message, subscriberId, record);
        } else {
            long delay = (long)(initialDelayMs * Math.pow(backoffMultiplier, record.getAttempts()));
            scheduleRetry(message, subscriberId, delay);
        }
    }
}
```

---

## Variation 3: Message Ordering
**Learning Value:** Practices ordering guarantees, partition-key routing, and causal consistency in message delivery.

### Additional Requirements
- Per-key ordering guarantee (all messages with same key delivered in order)
- Sequence numbers for order verification
- Resequencing buffer for out-of-order messages
- Ordering across partitions (global ordering subset)
- Duplicate detection with deduplication window
- Ordering impact on throughput (trade-offs)

### Design Changes
- Add `SequenceGenerator` for per-key sequence numbers
- Add `ResequencingBuffer` for reordering out-of-order deliveries
- Add `OrderingKey` concept for grouping related messages
- Add `DeduplicationFilter` using message IDs within a window
- Modify message delivery to enforce ordering guarantees
- Add `OrderedPartition` that ensures single-consumer per ordering key

### Solution Approach
Messages include an `OrderingKey` (e.g., orderId, userId). All messages with the same ordering key are routed to the same partition and processed by the same consumer, ensuring order within that key. The `SequenceGenerator` assigns monotonically increasing sequence numbers per ordering key. The consumer-side `ResequencingBuffer` holds messages until all predecessors are processed (if message 5 arrives before message 4, it waits). The `DeduplicationFilter` maintains a sliding window of recently processed message IDs to detect and drop duplicates. This design trades throughput (limited parallelism per key) for ordering guarantees.

### Key Classes to Add
```java
public class OrderedMessageRouter {
    private Map<String, Integer> keyToPartition; // ordering key -> partition

    public int route(Message message) {
        String orderingKey = message.getOrderingKey();
        return keyToPartition.computeIfAbsent(orderingKey,
            k -> Math.abs(k.hashCode()) % partitionCount);
    }
}

public class ResequencingBuffer {
    private Map<String, TreeMap<Long, Message>> buffers; // orderingKey -> seqNum -> message
    private Map<String, Long> expectedSequence; // orderingKey -> next expected seq

    public List<Message> add(Message message) {
        String key = message.getOrderingKey();
        long seq = message.getSequenceNumber();
        buffers.computeIfAbsent(key, k -> new TreeMap<>()).put(seq, message);

        // Release consecutive messages starting from expected sequence
        List<Message> ready = new ArrayList<>();
        TreeMap<Long, Message> buffer = buffers.get(key);
        long expected = expectedSequence.getOrDefault(key, 0L);
        while (buffer.containsKey(expected)) {
            ready.add(buffer.remove(expected));
            expected++;
        }
        expectedSequence.put(key, expected);
        return ready;
    }
}
```

---

## Variation 4: Fan-out/Fan-in
**Learning Value:** Explores trade-offs between latency and throughput in message distribution topologies.

### Additional Requirements
- Fan-out: one message delivered to many subscribers efficiently
- Fan-in: aggregate messages from multiple sources into one
- Aggregation patterns (collect N messages, then emit combined)
- Completion detection (all expected responses received)
- Timeout handling for incomplete fan-in
- Scatter-gather pattern implementation

### Design Changes
- Add `FanOutStrategy` (broadcast, filtered broadcast, partitioned)
- Add `Aggregator` for fan-in message collection
- Add `CompletionPolicy` (all, majority, first-N, timeout)
- Add `CorrelationManager` for tracking related messages
- Add `ScatterGather` combining fan-out request with fan-in response
- Add `TimeoutHandler` for incomplete aggregations

### Solution Approach
`FanOut` delivers a single published message to multiple subscriber queues efficiently. For broadcast, the message reference is added to each subscriber's queue (avoiding N copies of payload). For filtered fan-out, only matching subscribers receive the message. `FanIn` uses an `Aggregator` that collects messages sharing a correlation ID. The `CompletionPolicy` determines when the aggregation is complete (e.g., received from all 5 expected sources, or timeout after 30 seconds with partial results). `ScatterGather` combines both: sends a request to N services (fan-out), collects responses (fan-in), and returns the aggregated result. The `TimeoutHandler` ensures stuck aggregations don't leak memory.

### Key Classes to Add
```java
public class Aggregator {
    private Map<String, AggregationState> pendingAggregations;
    private CompletionPolicy completionPolicy;
    private TimeoutHandler timeoutHandler;

    public Optional<AggregatedMessage> addMessage(Message message) {
        String correlationId = message.getCorrelationId();
        AggregationState state = pendingAggregations.computeIfAbsent(
            correlationId, k -> new AggregationState(completionPolicy));

        state.add(message);
        if (state.isComplete()) {
            pendingAggregations.remove(correlationId);
            return Optional.of(state.buildResult());
        }
        return Optional.empty();
    }
}

public class ScatterGather {
    private MessageBroker broker;
    private Aggregator aggregator;
    private Duration timeout;

    public AggregatedMessage execute(String requestTopic, Message request, int expectedResponses) {
        String correlationId = UUID.randomUUID().toString();
        request.setCorrelationId(correlationId);
        aggregator.expect(correlationId, expectedResponses, timeout);
        broker.publish(requestTopic, request); // fan-out to N consumers
        return aggregator.awaitCompletion(correlationId); // fan-in responses
    }
}
```

---

## Variation 5: Schema Evolution
**Learning Value:** Deepens understanding of backward/forward compatibility, schema registries, and contract evolution.

### Additional Requirements
- Schema versioning for message payloads
- Backward compatibility (new schema reads old data)
- Forward compatibility (old schema reads new data)
- Schema registry for centralized schema management
- Serialization format support (Avro, Protobuf, JSON Schema)
- Breaking change detection and prevention

### Design Changes
- Add `SchemaRegistry` for storing and retrieving schemas
- Add `CompatibilityChecker` with different compatibility modes
- Add `SchemaEvolver` for automatic schema migration
- Add `SerializationProvider` interface (Avro, Protobuf, JSON)
- Modify `Message` to include schema ID in header
- Add `SchemaValidation` in publish pipeline

### Solution Approach
Every message type has a schema registered in the `SchemaRegistry` with a version. When publishing, the serializer includes the schema ID in the message header. The consumer uses the schema ID to deserialize correctly. The `CompatibilityChecker` enforces rules when registering new schema versions: BACKWARD (new schema can read old data - safe for consumers to upgrade first), FORWARD (old schema can read new data - safe for producers to upgrade first), or FULL (both). This is enforced by checking field additions have defaults, fields aren't removed unless optional, and types aren't changed. The `SchemaEvolver` can automatically transform data between compatible schema versions.

### Key Classes to Add
```java
public class SchemaRegistry {
    private Map<String, List<SchemaVersion>> schemas; // subject -> versions
    private CompatibilityMode defaultMode = CompatibilityMode.BACKWARD;

    public int registerSchema(String subject, Schema schema) {
        List<SchemaVersion> versions = schemas.computeIfAbsent(subject, k -> new ArrayList<>());
        if (!versions.isEmpty()) {
            Schema latest = versions.get(versions.size() - 1).getSchema();
            if (!CompatibilityChecker.isCompatible(latest, schema, defaultMode)) {
                throw new IncompatibleSchemaException(
                    "New schema is not " + defaultMode + " compatible");
            }
        }
        int version = versions.size() + 1;
        versions.add(new SchemaVersion(version, schema));
        return version;
    }

    public Schema getSchema(String subject, int version) {
        return schemas.get(subject).get(version - 1).getSchema();
    }
}

public enum CompatibilityMode {
    BACKWARD,  // new schema can read data written with old schema
    FORWARD,   // old schema can read data written with new schema
    FULL,      // both backward and forward compatible
    NONE       // no compatibility checks
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

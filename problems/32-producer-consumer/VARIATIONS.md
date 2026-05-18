# Producer-Consumer - Variations

## Variation 1: Multi-Topic Queues
**Learning Value:** Teaches topic-based message routing, subscriber isolation, and multi-channel queue management.

### Additional Requirements
- Multiple independent queues identified by topic name
- Consumer groups per topic (each group gets all messages)
- Load balancing within a consumer group
- Topic creation/deletion at runtime

### Design Changes
- Add TopicManager to manage named queues
- Add ConsumerGroup with partition assignment
- Each topic has its own BoundedBuffer
- Add message routing from producer to correct topic

### Solution Approach
Maintain a ConcurrentHashMap of topic name to BoundedBuffer. Each topic can have multiple consumer groups; within a group, messages are load-balanced (round-robin or sticky partition). Producers specify the topic when publishing. Consumer groups track offsets independently so each group processes all messages. Partition the topic buffer into segments for parallel consumption within a group. Use a coordinator to rebalance partitions when consumers join/leave a group.

### Key Classes to Add
```java
public class TopicManager {
    private final Map<String, Topic> topics = new ConcurrentHashMap<>();
    
    public void createTopic(String name, int partitions) {
        topics.put(name, new Topic(name, partitions));
    }
    
    public void publish(String topic, Message msg) {
        topics.get(topic).getPartition(msg.getKey()).enqueue(msg);
    }
}

public class ConsumerGroup {
    private final String groupId;
    private final List<GroupConsumer> consumers;
    private final Map<Integer, GroupConsumer> partitionAssignment;
    
    public void rebalance() {
        // Round-robin assign partitions to consumers
    }
}
```

---

## Variation 2: Priority Queue
**Learning Value:** Introduces priority-based consumption ordering, urgency classification, and fair scheduling.

### Additional Requirements
- Messages have priority levels
- Higher priority items consumed first
- Starvation prevention for low-priority items
- Priority can be dynamic (changes over time)

### Design Changes
- Replace ArrayBuffer with PriorityBuffer backed by a heap
- Add priority field to Item
- Add aging mechanism to prevent starvation
- Optional: separate queues per priority with weighted fair queuing

### Solution Approach
Use a PriorityBlockingQueue where items are ordered by priority. To prevent starvation, implement aging: items waiting longer than a threshold get their priority boosted. Alternative approach: use multiple queues (one per priority level) with weighted fair queuing - consume from high-priority queue 70% of the time, medium 20%, low 10%. This guarantees progress for all priorities while favoring higher ones. Track queue wait time metrics to detect starvation.

### Key Classes to Add
```java
public class PriorityBuffer<T extends Prioritizable> implements BoundedBuffer<T> {
    private final PriorityBlockingQueue<PriorityWrapper<T>> queue;
    private final int capacity;
    
    class PriorityWrapper<T> implements Comparable<PriorityWrapper<T>> {
        T item;
        int basePriority;
        long enqueueTime;
        
        int getEffectivePriority() {
            long age = System.currentTimeMillis() - enqueueTime;
            return basePriority + (int)(age / AGING_MS);
        }
    }
}
```

---

## Variation 3: Batch Consumer
**Learning Value:** Practices batch processing optimization, throughput tuning, and chunk-based consumption patterns.

### Additional Requirements
- Consume N items at once for batch processing efficiency
- Configurable batch size and flush timeout
- Partial batch flush when timeout expires
- Batch acknowledgment (all-or-nothing or partial)

### Design Changes
- Add BatchConsumer that accumulates items before processing
- Add FlushTimer that triggers flush on timeout
- Add BatchProcessor interface for batch handling
- Support both eager (size-triggered) and lazy (time-triggered) flush

### Solution Approach
The BatchConsumer drains up to N items from the buffer into a local batch list. Two triggers cause processing: (1) batch reaches configured size, or (2) flush timer fires (ensures latency bound for partial batches). Use a separate timer thread or ScheduledExecutorService for the timeout. After processing, acknowledge the batch. If processing fails, items can be returned to the buffer or sent to a retry queue. This amortizes per-item overhead (e.g., database batch inserts, network batch sends).

### Key Classes to Add
```java
public class BatchConsumer<T> extends Consumer<T> {
    private final int batchSize;
    private final long flushTimeoutMs;
    private final BatchProcessor<T> processor;
    private final List<T> currentBatch = new ArrayList<>();
    private long lastFlushTime = System.currentTimeMillis();
    
    public void consumeLoop() {
        while (running) {
            T item = buffer.poll(remainingTimeout(), TimeUnit.MILLISECONDS);
            if (item != null) currentBatch.add(item);
            if (currentBatch.size() >= batchSize || isTimedOut()) {
                processor.processBatch(Collections.unmodifiableList(currentBatch));
                currentBatch.clear();
                lastFlushTime = System.currentTimeMillis();
            }
        }
    }
}

public interface BatchProcessor<T> {
    void processBatch(List<T> batch);
}
```

---

## Variation 4: Dead Letter Queue
**Learning Value:** Explores trade-offs between reliability and throughput in failed message handling and isolation.

### Additional Requirements
- Failed messages routed to a separate dead letter queue
- Configurable max retry attempts before DLQ
- Exponential backoff between retries
- DLQ monitoring and manual reprocessing

### Design Changes
- Add DeadLetterQueue class
- Add RetryPolicy with backoff configuration
- Add message metadata (attempt count, last error, timestamps)
- Add DLQProcessor for manual replay

### Solution Approach
Wrap each message with metadata tracking attempt count and last failure reason. When processing fails, increment the attempt counter and reschedule with exponential backoff (delay = baseDelay * 2^attempt). If attempts exceed maxRetries, move the message to the dead letter queue. The DLQ is a separate persistent queue that can be monitored via metrics. Operators can inspect DLQ messages, fix the underlying issue, and replay them back to the main queue. Add alerting when DLQ depth crosses thresholds.

### Key Classes to Add
```java
public class RetryableMessage<T> {
    private final T payload;
    private int attemptCount;
    private String lastError;
    private long nextRetryTime;
    private final int maxRetries;
    private final long baseDelayMs;
    
    public long getBackoffDelay() {
        return baseDelayMs * (1L << Math.min(attemptCount, 10));
    }
    
    public boolean isExhausted() {
        return attemptCount >= maxRetries;
    }
}

public class DeadLetterQueue<T> {
    private final Queue<RetryableMessage<T>> dlq = new ConcurrentLinkedQueue<>();
    
    public void add(RetryableMessage<T> msg) { dlq.offer(msg); }
    public List<RetryableMessage<T>> peek(int n) { /* inspect */ }
    public void replay(RetryableMessage<T> msg, BoundedBuffer<T> target) { /* re-enqueue */ }
}
```

---

## Variation 5: Back-Pressure with Flow Control
**Learning Value:** Deepens understanding of flow control, producer throttling, and system stability under load.

### Additional Requirements
- Slow down producers when consumers cannot keep up
- Flow control signals (request N items model)
- Rate limiting at the producer side
- Metrics for pressure detection

### Design Changes
- Add FlowController that tracks consumer processing rate
- Add BackPressureStrategy (block, drop, sample, buffer-and-spill)
- Add rate limiter for producers (token bucket)
- Add pressure metrics (queue fill ratio, consumer lag)

### Solution Approach
Implement reactive-streams-style back-pressure: consumers signal demand by requesting N items. Producers are only allowed to emit when there is outstanding demand. If the consumer is slow, the producer blocks (or applies a strategy: drop oldest, drop newest, or buffer to disk). Use a token bucket rate limiter on the producer side calibrated to the consumer's throughput. Monitor the buffer fill ratio; when it crosses 80%, signal the producer to slow down. When it drops below 20%, signal to speed up. This prevents OOM while maximizing throughput.

### Key Classes to Add
```java
public class FlowControlledProducer<T> extends Producer<T> {
    private final Semaphore permits; // outstanding demand from consumer
    private final BackPressureStrategy strategy;
    
    public void produce(T item) {
        if (!permits.tryAcquire(timeout, TimeUnit.MILLISECONDS)) {
            strategy.onBackPressure(item); // drop, buffer to disk, etc.
            return;
        }
        buffer.put(item);
    }
}

public class FlowControlledConsumer<T> extends Consumer<T> {
    private final FlowControlledProducer<T> producer;
    private final int prefetchSize;
    
    public void onProcessed(T item) {
        producer.getPermits().release(); // signal demand
    }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

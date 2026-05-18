# Object Pool - Variations

## Variation 1: Partitioned Pool
**Learning Value:** Teaches partitioned resource management, affinity-based allocation, and reduced contention.

### Additional Requirements
- Per-thread or per-key pools to reduce contention
- Thread-local fast path (no locking for common case)
- Overflow to shared pool when local pool is empty
- Balance objects across partitions

### Design Changes
- Add PartitionedPool with ThreadLocal or key-based partitioning
- Add LocalPool per partition with small fixed capacity
- Add SharedPool as overflow/underflow buffer
- Add rebalancing logic across partitions

### Solution Approach
Each thread gets its own small local pool (stored in ThreadLocal). Borrow first checks the local pool (lock-free since it's thread-local). If empty, take from the shared pool (requires synchronization). Return puts back to the local pool; if full, overflow to the shared pool. For key-based partitioning (e.g., per-database), use a Map of pools. This dramatically reduces contention in high-throughput scenarios since most borrow/return operations are thread-local. Periodically rebalance by moving objects from overfull partitions to underfull ones.

### Key Classes to Add
```java
public class PartitionedPool<T> {
    private final ThreadLocal<LocalPool<T>> localPools;
    private final ObjectPool<T> sharedPool;
    private final int localCapacity;
    
    public T borrow() {
        LocalPool<T> local = localPools.get();
        T obj = local.tryBorrow(); // no lock needed
        if (obj != null) return obj;
        return sharedPool.borrow(); // fallback with lock
    }
    
    public void returnObject(T obj) {
        LocalPool<T> local = localPools.get();
        if (!local.tryReturn(obj)) { // local full
            sharedPool.returnObject(obj); // overflow to shared
        }
    }
    
    static class LocalPool<T> {
        private final Object[] items;
        private int size;
        // No synchronization needed - thread-local
    }
}
```

---

## Variation 2: Soft Reference Pool
**Learning Value:** Introduces GC-aware resource management, soft references, and memory-pressure-sensitive pooling.

### Additional Requirements
- Allow GC to reclaim idle objects under memory pressure
- Maintain minimum hard references for guaranteed capacity
- Recreate objects on demand after GC collection
- Monitor GC collection rate for pool health

### Design Changes
- Add SoftReferencePool that wraps idle objects in SoftReference
- Maintain minSize hard references, rest as soft references
- Add ReferenceQueue to detect GC'd objects
- Add metrics for GC collection events

### Solution Approach
Idle objects beyond the minimum size are wrapped in SoftReference. The JVM's GC will reclaim these before throwing OutOfMemoryError. A ReferenceQueue is polled to detect when objects are collected, updating pool metrics. When borrowing, if a soft reference has been cleared, create a new object. This allows the pool to be large under normal conditions but gracefully shrink under memory pressure. Hard references guarantee a minimum pool size. Track the ratio of cleared references to detect memory pressure trends.

### Key Classes to Add
```java
public class SoftReferencePool<T> {
    private final Queue<T> hardPool;             // guaranteed minimum
    private final Queue<SoftReference<T>> softPool; // GC-reclaimable
    private final ReferenceQueue<T> refQueue;
    private final int minHardSize;
    private final ObjectFactory<T> factory;
    
    public T borrow() {
        T obj = hardPool.poll();
        if (obj != null) return obj;
        
        while (!softPool.isEmpty()) {
            SoftReference<T> ref = softPool.poll();
            obj = ref.get(); // may be null if GC'd
            if (obj != null) return obj;
            // else: GC'd, try next
        }
        return factory.create(); // all reclaimed, create new
    }
    
    public void returnObject(T obj) {
        if (hardPool.size() < minHardSize) {
            hardPool.offer(obj);
        } else {
            softPool.offer(new SoftReference<>(obj, refQueue));
        }
    }
    
    private void processCollectedReferences() {
        Reference<? extends T> ref;
        while ((ref = refQueue.poll()) != null) {
            metricsCollector.recordGCCollection();
        }
    }
}
```

---

## Variation 3: Pool with Warm-Up
**Learning Value:** Practices eager initialization strategies, warm-up sequencing, and cold-start mitigation.

### Additional Requirements
- Pre-create objects before they are needed
- Async background creation to not block first request
- Configurable warm-up size and strategy
- Lazy vs eager initialization options

### Design Changes
- Add WarmUpStrategy (eager, lazy, progressive)
- Add BackgroundCreator thread for async object creation
- Add ReadinessCheck to signal when pool is warm
- Add creation cost tracking for optimal warm-up timing

### Solution Approach
On pool initialization, start a background thread that creates objects up to the warm-up target. Use progressive warm-up: create a few immediately (for instant availability), then fill the rest in the background. The pool signals "ready" when minimum warm-up count is reached. This avoids the cold-start latency where the first N requests all pay object creation cost simultaneously. Track creation time to estimate warm-up duration. For JVM pools, warm-up also triggers JIT compilation of hot paths.

### Key Classes to Add
```java
public class WarmablePool<T> extends ObjectPool<T> {
    private final WarmUpConfig warmUpConfig;
    private final CountDownLatch readyLatch;
    private volatile boolean warmedUp;
    
    public WarmablePool(ObjectFactory<T> factory, PoolConfig config, WarmUpConfig warmUpConfig) {
        super(factory, config);
        this.warmUpConfig = warmUpConfig;
        this.readyLatch = new CountDownLatch(warmUpConfig.getMinReadyCount());
        startWarmUp();
    }
    
    private void startWarmUp() {
        new Thread(() -> {
            for (int i = 0; i < warmUpConfig.getTargetSize(); i++) {
                T obj = factory.create();
                addToPool(obj);
                readyLatch.countDown();
                if (warmUpConfig.getCreationDelayMs() > 0) {
                    Thread.sleep(warmUpConfig.getCreationDelayMs());
                }
            }
            warmedUp = true;
        }, "pool-warmup").start();
    }
    
    public void awaitReady(long timeout, TimeUnit unit) throws InterruptedException {
        readyLatch.await(timeout, unit);
    }
}
```

---

## Variation 4: Bounded vs Unbounded Pool
**Learning Value:** Explores trade-offs between resource consumption and request rejection in pool boundary policies.

### Additional Requirements
- Hard limit (reject/block when full) vs grow-on-demand
- Overflow policy (block with timeout, reject, create-and-discard)
- High watermark alerts
- Configurable growth strategy (linear, exponential)

### Design Changes
- Add BoundedPool and UnboundedPool implementations
- Add OverflowPolicy interface (Block, Reject, CreateTemporary)
- Add GrowthStrategy for unbounded pools
- Add HighWatermarkListener for alerts

### Solution Approach
Bounded pool has a fixed maximum; when all objects are active, apply the overflow policy: block (wait with timeout for a return), reject (throw exception immediately), or create-temporary (create an object that is destroyed on return, not pooled). Unbounded pool grows on demand but tracks a high watermark. When the active count exceeds the watermark, fire an alert. Growth can be linear (add 1) or exponential (double). Combine with eviction to shrink back during low usage. Track peak usage to size the pool appropriately.

### Key Classes to Add
```java
public interface OverflowPolicy<T> {
    T onPoolExhausted(ObjectPool<T> pool, long timeoutMs) throws PoolExhaustedException;
}

public class BlockingOverflow<T> implements OverflowPolicy<T> {
    public T onPoolExhausted(ObjectPool<T> pool, long timeoutMs) {
        // Wait for an object to be returned
        return pool.waitForReturn(timeoutMs);
    }
}

public class CreateTemporaryOverflow<T> implements OverflowPolicy<T> {
    public T onPoolExhausted(ObjectPool<T> pool, long timeoutMs) {
        T temp = pool.getFactory().create();
        pool.markTemporary(temp); // will be destroyed on return
        return temp;
    }
}

public class UnboundedPool<T> extends ObjectPool<T> {
    private final GrowthStrategy growthStrategy;
    private final int highWatermark;
    private final List<HighWatermarkListener> listeners;
    
    public T borrow() {
        T obj = tryBorrowFromIdle();
        if (obj == null) {
            obj = factory.create();
            if (getActiveCount() > highWatermark) notifyListeners();
        }
        return obj;
    }
}
```

---

## Variation 5: Pool Monitoring
**Learning Value:** Deepens understanding of pool observability, health metrics, and diagnostic instrumentation.

### Additional Requirements
- JMX/metrics exposure for pool health
- Leak detection (objects borrowed but never returned)
- Usage histogram (borrow duration distribution)
- Real-time alerts on pool exhaustion

### Design Changes
- Add PoolMetrics with counters and histograms
- Add LeakDetector with timeout-based detection
- Add JMX MBean interface for monitoring
- Add AlertManager for threshold-based notifications

### Solution Approach
Track every borrow/return with timestamps. LeakDetector runs a background thread that scans active borrows; if any object has been active longer than a configurable threshold, flag it as a potential leak (log stack trace captured at borrow time). Expose metrics via JMX: active count, idle count, wait time histogram, borrow duration histogram, creation count, eviction count. Set up alerts for: pool utilization > 90%, average wait time > threshold, leak detected. HikariCP-style: store the Thread and stack trace at borrow time for leak reporting.

### Key Classes to Add
```java
public class MonitoredPool<T> extends ObjectPool<T> {
    private final LeakDetector<T> leakDetector;
    private final PoolMetrics metrics;
    
    public T borrow() {
        long waitStart = System.nanoTime();
        T obj = super.borrow();
        metrics.recordWaitTime(System.nanoTime() - waitStart);
        leakDetector.recordBorrow(obj, Thread.currentThread().getStackTrace());
        return obj;
    }
    
    public void returnObject(T obj) {
        leakDetector.recordReturn(obj);
        super.returnObject(obj);
    }
}

public class LeakDetector<T> {
    private final Map<T, BorrowRecord> activeObjects = new ConcurrentHashMap<>();
    private final long leakThresholdMs;
    
    class BorrowRecord {
        long borrowTime;
        StackTraceElement[] stackTrace;
        Thread borrowerThread;
    }
    
    public void detectLeaks() {
        long now = System.currentTimeMillis();
        activeObjects.forEach((obj, record) -> {
            if (now - record.borrowTime > leakThresholdMs) {
                log.warn("Potential leak: {} borrowed {}ms ago by {}\n{}",
                    obj, now - record.borrowTime, record.borrowerThread, 
                    formatStackTrace(record.stackTrace));
            }
        });
    }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

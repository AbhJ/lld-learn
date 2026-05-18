# Cache System - Variations

## Variation 1: Distributed Cache (Partitioned)
**Learning Value:** Teaches distributed data partitioning, consistent hashing, and cluster coordination.

### Additional Requirements
- Consistent hashing for key distribution across nodes
- Data replication for fault tolerance
- Partition tolerance and conflict resolution
- Node addition/removal with minimal key redistribution
- Client-side routing or proxy-based routing
- Cluster health monitoring and failover

### Design Changes
- Add `ConsistentHashRing` for key-to-node mapping
- Add `CacheNode` representing individual cache instances
- Add `ReplicationManager` for write replication
- Add `PartitionStrategy` (master-slave, multi-master)
- Add `ClusterManager` for node lifecycle and health
- Add `RoutingClient` for directing requests to correct node

### Solution Approach
Keys are distributed across `CacheNode` instances using a `ConsistentHashRing` with virtual nodes (150+ per physical node for even distribution). Each key is replicated to N successive nodes on the ring for fault tolerance. Writes go to the primary node and are replicated asynchronously (AP) or synchronously (CP) to replicas based on `ReplicationManager` configuration. When a node fails, the `ClusterManager` detects the failure via heartbeats and promotes a replica. When a new node joins, only keys in its range are redistributed. The `RoutingClient` maintains an updated ring topology and routes requests directly to the responsible node.

### Key Classes to Add
```java
public class ConsistentHashRing {
    private TreeMap<Long, CacheNode> ring = new TreeMap<>();
    private int virtualNodesPerNode = 150;

    public void addNode(CacheNode node) {
        for (int i = 0; i < virtualNodesPerNode; i++) {
            long hash = hash(node.getId() + "-" + i);
            ring.put(hash, node);
        }
    }

    public CacheNode getNode(String key) {
        long hash = hash(key);
        Map.Entry<Long, CacheNode> entry = ring.ceilingEntry(hash);
        return (entry != null) ? entry.getValue() : ring.firstEntry().getValue();
    }

    public List<CacheNode> getReplicaNodes(String key, int replicaCount) {
        // Return next N distinct physical nodes after the primary
        CacheNode primary = getNode(key);
        List<CacheNode> replicas = new ArrayList<>();
        // traverse ring collecting unique nodes...
        return replicas;
    }
}
```

---

## Variation 2: Write-through/Write-back
**Learning Value:** Introduces write consistency strategies and trade-offs between latency and data freshness.

### Additional Requirements
- Write-through: synchronous write to cache and DB
- Write-back (write-behind): async write to DB, immediate cache update
- Dirty bit tracking for write-back entries
- Periodic flush of dirty entries
- Write coalescing (batch multiple writes to same key)
- Failure handling (what if DB write fails?)

### Design Changes
- Add `WritePolicy` interface with WriteThrough and WriteBack implementations
- Add `DirtyTracker` for marking modified entries
- Add `FlushScheduler` for periodic dirty entry persistence
- Add `WriteCoalescer` for batching writes
- Add `DataStore` interface representing the backing store
- Modify cache `put()` to route through write policy

### Solution Approach
The `WritePolicy` determines how writes are handled. `WriteThrough` writes to both cache and `DataStore` synchronously in the same operation - if the DB write fails, the cache write is rolled back. `WriteBack` writes only to the cache immediately, marking the entry as dirty via `DirtyTracker`. The `FlushScheduler` periodically scans for dirty entries and persists them to the `DataStore` in batches. The `WriteCoalescer` ensures that if a key is updated multiple times between flushes, only the latest value is written to the DB. On cache eviction of a dirty entry, a forced flush to DB occurs before the entry is removed.

### Key Classes to Add
```java
public interface WritePolicy<V> {
    void write(String key, V value, Cache<V> cache, DataStore<V> store);
    void onEviction(String key, V value, DataStore<V> store);
}

public class WriteBackPolicy<V> implements WritePolicy<V> {
    private DirtyTracker dirtyTracker;
    private FlushScheduler flushScheduler;

    public void write(String key, V value, Cache<V> cache, DataStore<V> store) {
        cache.put(key, value);
        dirtyTracker.markDirty(key);
        // DB write is deferred
    }

    public void onEviction(String key, V value, DataStore<V> store) {
        if (dirtyTracker.isDirty(key)) {
            store.write(key, value); // must persist before eviction
            dirtyTracker.markClean(key);
        }
    }

    public void flush(Cache<V> cache, DataStore<V> store) {
        for (String dirtyKey : dirtyTracker.getDirtyKeys()) {
            V value = cache.get(dirtyKey);
            store.write(dirtyKey, value);
            dirtyTracker.markClean(dirtyKey);
        }
    }
}
```

---

## Variation 3: Multi-level Cache (L1/L2)
**Learning Value:** Practices hierarchical caching, promotion/demotion policies, and cache coherence across levels.

### Additional Requirements
- L1: In-process (fast, small, per-instance)
- L2: Remote/shared (slower, larger, shared across instances)
- Promotion (L2 hit moves to L1) and demotion (L1 eviction doesn't delete from L2)
- Consistency between levels (invalidation propagation)
- Different TTLs per level
- Cache-aside vs. read-through at each level

### Design Changes
- Add `MultiLevelCache` composing L1 and L2 caches
- Add `PromotionPolicy` for moving entries between levels
- Add `InvalidationBroadcaster` for cross-instance L1 invalidation
- Add `CacheLevel` enum with level-specific configuration
- Modify read path to check L1 first, then L2, then source

### Solution Approach
The `MultiLevelCache` wraps L1 (local `ConcurrentHashMap`-based, small capacity, low latency) and L2 (remote distributed cache, large capacity, network latency). Read path: check L1 -> if miss, check L2 -> if hit, promote to L1 and return -> if miss, fetch from source, populate both L1 and L2. Write path: invalidate L1 across all instances (via `InvalidationBroadcaster` using pub-sub), then update L2. L1 has short TTL (30 seconds) to limit staleness window. L2 has longer TTL (5 minutes). The `InvalidationBroadcaster` uses a pub-sub channel so all app instances clear their local L1 when data changes.

### Key Classes to Add
```java
public class MultiLevelCache<V> implements Cache<V> {
    private Cache<V> l1Cache; // in-process, small, fast
    private Cache<V> l2Cache; // remote, large, shared
    private InvalidationBroadcaster broadcaster;
    private DataSource<V> source;

    public V get(String key) {
        // L1 check
        V value = l1Cache.get(key);
        if (value != null) return value;

        // L2 check
        value = l2Cache.get(key);
        if (value != null) {
            l1Cache.put(key, value); // promote to L1
            return value;
        }

        // Source fetch
        value = source.fetch(key);
        if (value != null) {
            l2Cache.put(key, value);
            l1Cache.put(key, value);
        }
        return value;
    }

    public void invalidate(String key) {
        l1Cache.remove(key);
        l2Cache.remove(key);
        broadcaster.broadcast(key); // tell other instances to clear L1
    }
}
```

---

## Variation 4: Cache Warming
**Learning Value:** Explores trade-offs between cold-start latency and resource consumption in proactive caching.

### Additional Requirements
- Pre-load frequently accessed data on startup
- Predictive caching based on access patterns
- Warm-up from snapshot (previous cache state)
- Gradual traffic shifting during warm-up
- Priority-based warming (most critical data first)
- Warming progress monitoring and readiness signal

### Design Changes
- Add `CacheWarmer` with pluggable warming strategies
- Add `WarmingStrategy` interface (Snapshot, Predictive, TopK)
- Add `AccessPatternAnalyzer` for predictive warming
- Add `WarmupProgressTracker` for readiness monitoring
- Add `TrafficGate` for gradual traffic admission during warmup
- Add `CacheSnapshot` for persisting/restoring cache state

### Solution Approach
On startup, the `CacheWarmer` pre-populates the cache before accepting traffic. Warming strategies include: `SnapshotWarming` (restore from a periodically saved cache dump), `TopKWarming` (pre-load the top-K most frequently accessed keys from access logs), and `PredictiveWarming` (use time-of-day patterns to pre-load data likely needed soon). The `WarmupProgressTracker` reports completion percentage and signals readiness to the load balancer. The `TrafficGate` initially rejects traffic, then gradually increases the admission rate as the cache warms (0% -> 25% -> 50% -> 100%). This prevents a cold-start thundering herd against the database.

### Key Classes to Add
```java
public class CacheWarmer<V> {
    private Cache<V> cache;
    private List<WarmingStrategy<V>> strategies;
    private WarmupProgressTracker progressTracker;
    private TrafficGate trafficGate;

    public void warmUp() {
        trafficGate.setAdmissionRate(0.0);
        int totalKeys = strategies.stream().mapToInt(WarmingStrategy::estimatedKeys).sum();
        progressTracker.setTotal(totalKeys);

        for (WarmingStrategy<V> strategy : strategies) {
            strategy.warm(cache, key -> progressTracker.increment());
            trafficGate.increaseAdmission(0.25); // gradual ramp
        }
        trafficGate.setAdmissionRate(1.0);
        progressTracker.markReady();
    }
}

public interface WarmingStrategy<V> {
    void warm(Cache<V> cache, Consumer<String> progressCallback);
    int estimatedKeys();
}

// Example: TopKWarmingStrategy loads the N most popular keys
public class TopKWarmingStrategy<V> implements WarmingStrategy<V> {
    private DataSource<V> source;
    private AccessLog accessLog;
    private int topK;

    public void warm(Cache<V> cache, Consumer<String> callback) {
        List<String> topKeys = accessLog.getTopKeys(topK);
        for (String key : topKeys) {
            cache.put(key, source.fetch(key));
            callback.accept(key);
        }
    }
}
```

---

## Variation 5: Cache Stampede Prevention
**Learning Value:** Deepens understanding of thundering herd prevention, probabilistic early expiration, and locking strategies.

### Additional Requirements
- Prevent thundering herd when popular key expires
- Singleflight pattern (only one goroutine/thread fetches, others wait)
- Probabilistic early expiry (some clients refresh before actual expiry)
- Mutex/lock-based protection per key
- Stale-while-revalidate (serve stale, refresh in background)
- Request coalescing for identical in-flight fetches

### Design Changes
- Add `StampedeProtection` interface with multiple strategies
- Add `SingleFlight` for request deduplication
- Add `ProbabilisticEarlyExpiry` for staggered refresh
- Add `LockPerKey` for mutex-based protection
- Add `StaleWhileRevalidate` for background refresh with stale serving
- Modify cache `get()` to route through stampede protection

### Solution Approach
When a cached key expires, multiple threads may simultaneously try to recompute it (thundering herd). The `SingleFlight` pattern ensures only one thread fetches the value while others block and wait for the result. `ProbabilisticEarlyExpiry` has each accessing thread independently decide to refresh early with probability that increases as expiry approaches (e.g., at 90% of TTL, 10% chance of proactive refresh). `StaleWhileRevalidate` returns the stale value immediately while triggering an async background refresh, ensuring zero-latency reads. `LockPerKey` uses a striped lock array (to limit memory) to mutex cache population per key.

### Key Classes to Add
```java
public class SingleFlight<V> {
    private ConcurrentHashMap<String, CompletableFuture<V>> inFlight = new ConcurrentHashMap<>();

    public V execute(String key, Supplier<V> loader) {
        CompletableFuture<V> future = inFlight.computeIfAbsent(key, k -> {
            CompletableFuture<V> f = CompletableFuture.supplyAsync(loader);
            f.whenComplete((result, error) -> inFlight.remove(k));
            return f;
        });
        return future.join(); // all callers wait on same future
    }
}

public class ProbabilisticEarlyExpiry<V> {
    private Cache<V> cache;
    private Random random = new Random();
    private double beta = 1.0; // controls refresh aggressiveness

    public V get(String key, Supplier<V> loader) {
        CacheEntry<V> entry = cache.getEntry(key);
        if (entry == null) return loadAndCache(key, loader);

        double remainingTTLRatio = entry.getRemainingTTLRatio();
        double refreshProbability = Math.exp(-beta * remainingTTLRatio * 10);
        if (random.nextDouble() < refreshProbability) {
            // Proactively refresh before expiry
            return loadAndCache(key, loader);
        }
        return entry.getValue();
    }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

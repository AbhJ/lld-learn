# Connection Pool - Variations

## Variation 1: Multi-Database Pool
**Learning Value:** Teaches multi-resource pool management, routing strategies, and heterogeneous backend support.

### Additional Requirements
- Separate pools for read replicas and write primary
- Route queries based on type (SELECT -> replica, INSERT/UPDATE -> primary)
- Pool per database instance with independent configurations
- Health checking per pool (remove unhealthy replicas)
- Load balancing across read replicas (round-robin, least-connections)

### Design Changes
- Add `MultiDatabasePool` managing multiple underlying pools
- Add `QueryRouter` that inspects query type and routes accordingly
- Add `ReplicaLoadBalancer` for distributing reads across replicas
- Add `PoolHealthChecker` per pool to detect and remove unhealthy instances
- Add `DatabaseConfig` per instance (primary, replica1, replica2, ...)

### Solution Approach
The system maintains separate connection pools for the primary (write) database and each read replica. A QueryRouter intercepts each query, determines if it is a read or write operation, and borrows a connection from the appropriate pool. Read queries are distributed across replica pools using a load balancing strategy (round-robin or least-connections). Each pool has independent health checking: if a replica becomes unhealthy (failed health check queries), its pool is temporarily removed from the rotation. When it recovers, it is re-added. This pattern maximizes read throughput while ensuring writes always go to the primary.

### Key Classes to Add
```java
public class MultiDatabasePool {
    private ConnectionPool primaryPool;
    private List<ConnectionPool> replicaPools;
    private ReplicaLoadBalancer loadBalancer;
    private QueryRouter router;
    
    public Connection borrowForQuery(String sql) {
        if (router.isWriteQuery(sql)) {
            return primaryPool.borrow();
        }
        ConnectionPool replicaPool = loadBalancer.selectReplica();
        return replicaPool.borrow();
    }
}

public class QueryRouter {
    public boolean isWriteQuery(String sql) {
        String upper = sql.trim().toUpperCase();
        return upper.startsWith("INSERT") || upper.startsWith("UPDATE")
            || upper.startsWith("DELETE") || upper.startsWith("CREATE");
    }
}

public class ReplicaLoadBalancer {
    private List<ConnectionPool> healthyReplicas;
    private AtomicInteger roundRobinIndex;
    
    public ConnectionPool selectReplica() {
        int idx = roundRobinIndex.getAndIncrement() % healthyReplicas.size();
        return healthyReplicas.get(idx);
    }
}
```

---

## Variation 2: Connection Warm-up
**Learning Value:** Introduces eager initialization, health validation, and startup latency optimization.

### Additional Requirements
- Pre-create connections on application startup
- Avoid cold-start latency on first requests
- Lazy vs eager initialization strategies
- Background warm-up without blocking startup
- Validation of pre-created connections before serving traffic

### Design Changes
- Add `WarmupStrategy` interface (EAGER, LAZY, BACKGROUND)
- Add `ConnectionValidator` to verify connections are usable
- Add `StartupWarmup` that pre-fills pool to minimum size
- Add `BackgroundWarmer` for non-blocking async warm-up
- Modify pool initialization to support pluggable warm-up strategies

### Solution Approach
Eager initialization creates all minimum-size connections during pool construction, blocking until ready. This guarantees zero cold-start latency but slows application startup. Background warm-up creates connections asynchronously after the pool is constructed; the pool serves requests immediately but early requests may need to wait for connections being created. Lazy initialization creates connections on-demand only. A hybrid approach pre-creates a subset eagerly and warms remaining connections in the background. All pre-created connections are validated (e.g., execute a test query) before being placed in the available pool to ensure they are truly usable.

### Key Classes to Add
```java
public interface WarmupStrategy {
    void warmup(ConnectionPool pool, PoolConfig config);
}

public class EagerWarmup implements WarmupStrategy {
    public void warmup(ConnectionPool pool, PoolConfig config) {
        for (int i = 0; i < config.getMinSize(); i++) {
            Connection conn = createAndValidate(config);
            pool.addToAvailable(conn);
        }
    }
}

public class BackgroundWarmup implements WarmupStrategy {
    private ExecutorService executor;
    
    public void warmup(ConnectionPool pool, PoolConfig config) {
        executor.submit(() -> {
            for (int i = pool.size(); i < config.getMinSize(); i++) {
                Connection conn = createAndValidate(config);
                pool.addToAvailable(conn);
            }
        });
    }
}

public class ConnectionValidator {
    private String validationQuery; // e.g., "SELECT 1"
    
    public boolean validate(Connection conn) {
        // Execute validation query, return true if successful
    }
}
```

---

## Variation 3: Adaptive Pool Sizing
**Learning Value:** Practices auto-scaling resource pools, load-based sizing, and feedback-driven capacity adjustment.

### Additional Requirements
- Auto-scale pool size based on current load
- Grow when demand exceeds available connections
- Shrink during idle periods to free resources
- Min/max bounds to prevent runaway growth
- Metrics-driven scaling decisions

### Design Changes
- Add `PoolSizer` that monitors utilization and adjusts pool size
- Add `ScalingPolicy` with grow/shrink thresholds
- Add `UtilizationMetrics` tracking borrow rate and wait times
- Add `IdleReaper` to close connections idle beyond threshold
- Add `CooldownPeriod` to prevent scaling oscillation

### Solution Approach
The pool monitors its utilization ratio (active connections / total connections) and average borrow wait time. When utilization exceeds a high threshold (e.g., 80%) for a sustained period, new connections are created up to the max size. When utilization drops below a low threshold (e.g., 20%) and connections have been idle beyond a timeout, excess connections above the minimum are closed. A cooldown period prevents rapid oscillation between growing and shrinking. The IdleReaper runs periodically, closing connections that have been idle longer than the configured idle timeout. Metrics (utilization, wait time, pool size over time) are exposed for monitoring.

### Key Classes to Add
```java
public class AdaptivePoolSizer {
    private ConnectionPool pool;
    private ScalingPolicy policy;
    private ScheduledExecutorService scheduler;
    
    public void start() {
        scheduler.scheduleAtFixedRate(this::evaluate, 0, 10, TimeUnit.SECONDS);
    }
    
    private void evaluate() {
        double utilization = pool.getUtilization();
        if (utilization > policy.getGrowThreshold()) grow();
        else if (utilization < policy.getShrinkThreshold()) shrink();
    }
    
    private void grow() { /* Add connections up to max */ }
    private void shrink() { /* Remove idle connections down to min */ }
}

public class ScalingPolicy {
    private double growThreshold; // e.g., 0.8
    private double shrinkThreshold; // e.g., 0.2
    private Duration idleTimeout;
    private Duration cooldownPeriod;
    private int growBatchSize;
}

public class IdleReaper implements Runnable {
    private ConnectionPool pool;
    private Duration maxIdleTime;
    
    public void run() {
        // Close connections idle longer than maxIdleTime
    }
}
```

---

## Variation 4: Statement Caching
**Learning Value:** Explores trade-offs between memory usage and execution speed in prepared statement caching.

### Additional Requirements
- Pool of prepared statements per connection
- LRU eviction when statement cache is full
- Transparent statement reuse (same SQL reuses cached statement)
- Cache invalidation on schema changes
- Per-connection vs global statement cache

### Design Changes
- Add `StatementCache` with LRU eviction per connection
- Add `CachedStatement` wrapper around PreparedStatement
- Add `StatementKey` (SQL string hash) for cache lookup
- Modify connection `prepareStatement()` to check cache first
- Add `CacheInvalidator` triggered on schema changes

### Solution Approach
Each pooled connection maintains its own statement cache (since prepared statements are connection-scoped in JDBC). When `prepareStatement(sql)` is called, the cache is checked first using the SQL string as key. On cache hit, the existing PreparedStatement is returned (after clearing parameters). On miss, a new statement is prepared and cached. The cache uses LRU eviction: when full, the least recently used statement is closed and removed. This avoids the overhead of repeated statement preparation (parsing, optimization) for frequently executed queries. Cache size is configurable per connection. Schema changes (DDL) invalidate affected cached statements.

### Key Classes to Add
```java
public class StatementCache {
    private int maxSize;
    private LinkedHashMap<String, CachedStatement> cache; // LRU order
    
    public PreparedStatement get(String sql) {
        CachedStatement cached = cache.get(sql);
        if (cached != null) {
            cached.getStatement().clearParameters();
            return cached.getStatement();
        }
        return null;
    }
    
    public void put(String sql, PreparedStatement stmt) {
        if (cache.size() >= maxSize) evictLRU();
        cache.put(sql, new CachedStatement(stmt));
    }
    
    private void evictLRU() { ... }
}

public class CachedStatement {
    private PreparedStatement statement;
    private Instant lastUsed;
    private int useCount;
}

public class PooledConnection extends Connection {
    private StatementCache statementCache;
    
    @Override
    public PreparedStatement prepareStatement(String sql) {
        PreparedStatement cached = statementCache.get(sql);
        if (cached != null) return cached;
        PreparedStatement stmt = super.prepareStatement(sql);
        statementCache.put(sql, stmt);
        return stmt;
    }
}
```

---

## Variation 5: Connection Leak Detection
**Learning Value:** Deepens understanding of resource leak detection, timeout monitoring, and diagnostic tooling.

### Additional Requirements
- Track how long each connection has been checked out
- Alert/log if connection held beyond threshold (probable leak)
- Auto-reclaim connections held too long
- Stack trace capture at borrow time for debugging
- Leak report with frequency and offending code locations

### Design Changes
- Add `LeakDetector` monitoring borrowed connection duration
- Add `BorrowRecord` capturing checkout time and stack trace
- Add `LeakPolicy` with threshold, action (LOG, RECLAIM, EXCEPTION)
- Add `LeakReport` aggregating leak occurrences by call site
- Modify `borrow()` to record checkout metadata

### Solution Approach
When a connection is borrowed, the pool records the current time and captures the stack trace of the borrowing thread. A background LeakDetector thread periodically scans all borrowed connections. If any connection has been held longer than the configured threshold (e.g., 30 seconds), it is flagged as a suspected leak. Depending on the LeakPolicy, the system can: log a warning with the borrow stack trace, forcibly reclaim the connection (close and return to pool), or throw an exception in the holding thread. The LeakReport aggregates detections by stack trace, making it easy to identify which code paths consistently leak connections. This is critical for production stability.

### Key Classes to Add
```java
public class LeakDetector implements Runnable {
    private Map<Connection, BorrowRecord> activeConnections;
    private LeakPolicy policy;
    private ScheduledExecutorService scheduler;
    
    public void start() {
        scheduler.scheduleAtFixedRate(this, 0, 5, TimeUnit.SECONDS);
    }
    
    public void run() {
        Instant now = Instant.now();
        for (var entry : activeConnections.entrySet()) {
            Duration held = Duration.between(entry.getValue().getBorrowTime(), now);
            if (held.compareTo(policy.getThreshold()) > 0) {
                handleLeak(entry.getKey(), entry.getValue());
            }
        }
    }
    
    private void handleLeak(Connection conn, BorrowRecord record) { ... }
}

public class BorrowRecord {
    private Instant borrowTime;
    private StackTraceElement[] borrowStackTrace;
    private String threadName;
    
    public Duration getHeldDuration() { ... }
}

public class LeakPolicy {
    private Duration threshold; // e.g., 30 seconds
    private LeakAction action; // LOG, RECLAIM, EXCEPTION
    private boolean captureStackTrace; // performance trade-off
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

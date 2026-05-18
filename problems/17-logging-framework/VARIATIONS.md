# Logging Framework - Variations

## Variation 1: Distributed Tracing
**Learning Value:** Teaches request correlation, span propagation, and cross-service trace assembly.

### Additional Requirements
- Correlation IDs propagated across service boundaries
- Span tracking (parent-child relationships between operations)
- Trace context injection into HTTP headers
- Trace visualization (waterfall view of spans)
- Sampling strategy (trace 1% of requests in production)
- Baggage items (key-value pairs propagated across services)

### Design Changes
- Add `TraceContext` holding traceId, spanId, parentSpanId
- Add `Span` representing a unit of work with timing
- Add `TraceContextPropagator` for cross-service context passing
- Add `Sampler` interface for controlling trace collection rate
- Add `SpanExporter` for sending traces to collection backend
- Modify `Logger` to automatically include trace context

### Solution Approach
Each incoming request creates a `TraceContext` with a unique traceId (or extracts one from incoming headers). As the request flows through the system, `Span` objects are created for each significant operation (DB query, HTTP call, processing step). Spans have parent-child relationships forming a tree. The `TraceContextPropagator` injects context into outgoing HTTP headers (e.g., `X-Trace-Id`, `X-Span-Id`) so downstream services can continue the trace. The `Sampler` decides which traces to actually collect (head-based sampling at entry point). The logger automatically adds traceId to all log entries for correlation.

### Key Classes to Add
```java
public class TraceContext {
    private String traceId;
    private String spanId;
    private String parentSpanId;
    private Map<String, String> baggage;
    private boolean sampled;

    private static final ThreadLocal<TraceContext> CURRENT = new ThreadLocal<>();

    public static TraceContext current() { return CURRENT.get(); }

    public Span startSpan(String operationName) {
        Span span = new Span(traceId, generateSpanId(), spanId, operationName);
        span.start();
        return span;
    }
}

public class Span {
    private String traceId;
    private String spanId;
    private String parentSpanId;
    private String operationName;
    private long startTimeMs;
    private long endTimeMs;
    private Map<String, String> tags;
    private List<SpanLog> logs;

    public void finish() {
        this.endTimeMs = System.currentTimeMillis();
        SpanExporter.export(this);
    }

    public long getDurationMs() { return endTimeMs - startTimeMs; }
}
```

---

## Variation 2: Log Aggregation
**Learning Value:** Introduces centralized log collection, multi-source ingestion, and structured log querying.

### Additional Requirements
- Ship logs to centralized systems (ELK stack, Splunk)
- Structured JSON log format for machine parsing
- Log sampling for high-volume services
- Log rotation and archival
- Buffered/batched shipping to reduce network overhead
- Fallback to local file when aggregator is unreachable

### Design Changes
- Add `LogShipper` interface with implementations for different backends
- Add `StructuredLogEntry` with mandatory fields (timestamp, service, level, traceId)
- Add `SamplingFilter` for probabilistic log reduction
- Add `BatchBuffer` for efficient network shipping
- Add `RotationPolicy` for local file management
- Add `FailoverAppender` with local fallback

### Solution Approach
Logs are formatted as `StructuredLogEntry` objects (JSON with standardized fields). The `BatchBuffer` collects entries and flushes to the `LogShipper` every N entries or M seconds (whichever comes first). The `SamplingFilter` reduces volume by probabilistically dropping non-error logs (e.g., keep 10% of DEBUG, 100% of ERROR). The `LogShipper` has implementations for different backends (Elasticsearch, Splunk HEC, CloudWatch). If the shipper fails to deliver, the `FailoverAppender` writes to a local file with a `RotationPolicy` (rotate at 100MB, keep 5 files). A recovery process re-ships local fallback files when the aggregator recovers.

### Key Classes to Add
```java
public class LogAggregator {
    private LogShipper shipper;
    private BatchBuffer buffer;
    private FailoverAppender failover;
    private SamplingFilter sampler;

    public void ship(StructuredLogEntry entry) {
        if (!sampler.shouldInclude(entry)) return;
        buffer.add(entry);
        if (buffer.shouldFlush()) {
            List<StructuredLogEntry> batch = buffer.drain();
            try {
                shipper.ship(batch);
            } catch (ShippingException e) {
                failover.write(batch);
            }
        }
    }
}

public class BatchBuffer {
    private List<StructuredLogEntry> entries = new ArrayList<>();
    private int maxSize = 100;
    private long maxAgeMs = 5000;
    private long lastFlushTime = System.currentTimeMillis();

    public boolean shouldFlush() {
        return entries.size() >= maxSize ||
            (System.currentTimeMillis() - lastFlushTime) >= maxAgeMs;
    }
}
```

---

## Variation 3: Audit Logging
**Learning Value:** Practices immutable audit trails, compliance logging, and tamper-evident record design.

### Additional Requirements
- Immutable audit trail (append-only, no modification or deletion)
- Who/what/when/where for every action
- Compliance requirements (SOX, GDPR, HIPAA)
- Tamper detection (hash chaining or digital signatures)
- Retention policies (7 years for financial, etc.)
- Query and reporting for auditors

### Design Changes
- Add `AuditEvent` with actor, action, resource, timestamp, outcome
- Add `AuditStore` interface (append-only, immutable)
- Add `HashChain` for tamper detection (each entry hashes the previous)
- Add `RetentionPolicy` for time-based archival rules
- Add `AuditQuery` for auditor search and reporting
- Add `ComplianceFormatter` for regulatory report generation

### Solution Approach
Every significant system action creates an `AuditEvent` containing: who (authenticated user/service), what (action performed), on what (resource identifier), when (UTC timestamp with milliseconds), where (IP/service), and outcome (success/failure with reason). Events are written to an append-only `AuditStore` with hash chaining: each event includes a hash of the previous event, forming a blockchain-like chain that detects tampering. The store rejects any attempt to modify or delete entries. `RetentionPolicy` manages archival (move to cold storage after 1 year, delete after 7 years). The `AuditQuery` API provides search by actor, time range, resource, or action type for compliance auditors.

### Key Classes to Add
```java
public class AuditEvent {
    private String eventId;
    private String actor;        // who performed the action
    private String action;       // what was done (CREATE, READ, UPDATE, DELETE)
    private String resource;     // what was acted upon
    private String resourceId;
    private LocalDateTime timestamp;
    private String sourceIP;
    private AuditOutcome outcome; // SUCCESS, FAILURE, DENIED
    private Map<String, String> metadata;
    private String previousHash; // hash chain link
    private String eventHash;    // SHA-256 of this event + previousHash
}

public class ImmutableAuditStore implements AuditStore {
    private List<AuditEvent> events; // append-only
    private String lastHash = "GENESIS";

    public void append(AuditEvent event) {
        event.setPreviousHash(lastHash);
        event.setEventHash(computeHash(event));
        lastHash = event.getEventHash();
        events.add(event); // no update/delete methods exist
    }

    public boolean verifyIntegrity() {
        String expectedPrevHash = "GENESIS";
        for (AuditEvent event : events) {
            if (!event.getPreviousHash().equals(expectedPrevHash)) return false;
            if (!event.getEventHash().equals(computeHash(event))) return false;
            expectedPrevHash = event.getEventHash();
        }
        return true;
    }
}
```

---

## Variation 4: Performance Logging
**Learning Value:** Explores trade-offs between observability overhead and performance in metric collection.

### Additional Requirements
- Method execution timing with annotations
- Percentile calculation (p50, p95, p99)
- Slow query/operation detection and alerting
- Execution time histograms
- Overhead budget (logging should add < 1% latency)
- Correlation between operations and their durations

### Design Changes
- Add `PerformanceLogger` with timer management
- Add `Timer` class for measuring operation duration
- Add `PercentileCalculator` using reservoir sampling
- Add `SlowOperationDetector` with configurable thresholds
- Add `PerformanceReport` for periodic statistics
- Add `@Timed` annotation concept for declarative timing

### Solution Approach
The `PerformanceLogger` provides a `Timer` API: start a timer before an operation, stop it after. The timer records the duration and sends it to a `MetricAggregator`. The aggregator maintains a reservoir sample per operation name, computing running percentiles. The `SlowOperationDetector` fires alerts when an operation exceeds its threshold (e.g., DB query > 100ms, API call > 500ms). Thresholds can be static or dynamic (e.g., > p99 of the last hour). A `PerformanceReport` is generated periodically showing top slow operations, degradation trends, and percentile summaries. The implementation uses thread-local timer stacks to handle nested timing.

### Key Classes to Add
```java
public class PerformanceLogger {
    private Map<String, MetricAggregator> metrics;
    private SlowOperationDetector slowDetector;
    private static final ThreadLocal<Deque<Timer>> timerStack = ThreadLocal.withInitial(ArrayDeque::new);

    public Timer startTimer(String operationName) {
        Timer timer = new Timer(operationName);
        timerStack.get().push(timer);
        timer.start();
        return timer;
    }

    public void stopTimer(Timer timer) {
        timer.stop();
        timerStack.get().pop();
        MetricAggregator agg = metrics.computeIfAbsent(timer.getName(), MetricAggregator::new);
        agg.record(timer.getDurationMs());
        slowDetector.check(timer);
    }

    public PercentileSnapshot getPercentiles(String operationName) {
        return metrics.get(operationName).getPercentiles(); // p50, p95, p99
    }
}

public class MetricAggregator {
    private String name;
    private ReservoirSample reservoir; // fixed-size sample for percentiles
    private long count;
    private double sum;
    private double max;

    public void record(double valueMs) {
        count++;
        sum += valueMs;
        max = Math.max(max, valueMs);
        reservoir.add(valueMs);
    }
}
```

---

## Variation 5: Dynamic Log Level
**Learning Value:** Deepens understanding of runtime configuration changes, hot-reload patterns, and level propagation.

### Additional Requirements
- Change log level at runtime without restarting the service
- Per-class/package level configuration
- Temporary level changes with auto-revert (e.g., DEBUG for 5 minutes)
- API/JMX endpoint for level management
- Level inheritance (package level applies to all classes under it)
- Change audit trail (who changed what level when)

### Design Changes
- Add `DynamicLevelManager` for runtime level changes
- Add `LevelOverride` with optional TTL for temporary changes
- Add `LevelHierarchy` for package-based inheritance
- Add `LevelChangeListener` for observing changes
- Add `ManagementEndpoint` for API-based level control
- Modify `Logger` to check dynamic level before static config

### Solution Approach
The `DynamicLevelManager` maintains a map of logger name patterns to level overrides. When a log statement is executed, the logger checks: (1) Is there an active override for this specific class? (2) Is there a package-level override? (3) Fall back to configured level. Overrides can have an optional TTL (e.g., "set com.myapp.payment to DEBUG for 5 minutes"), after which the level auto-reverts. The `ManagementEndpoint` exposes REST/JMX endpoints for operations teams to adjust levels without deployment. A `LevelHierarchy` implements package-tree traversal (com.myapp.payment inherits from com.myapp if not specifically overridden). All changes are logged in a `ChangeAuditLog`.

### Key Classes to Add
```java
public class DynamicLevelManager {
    private Map<String, LevelOverride> overrides = new ConcurrentHashMap<>();
    private ScheduledExecutorService expiryChecker;

    public void setLevel(String loggerName, LogLevel level, Duration ttl) {
        LevelOverride override = new LevelOverride(level, ttl);
        overrides.put(loggerName, override);
        auditLog.record(loggerName, level, ttl);
        if (ttl != null) {
            expiryChecker.schedule(() -> revert(loggerName), ttl.toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    public LogLevel getEffectiveLevel(String loggerName) {
        // Check exact match
        LevelOverride override = overrides.get(loggerName);
        if (override != null && !override.isExpired()) return override.getLevel();
        // Check parent packages
        String parent = getParentPackage(loggerName);
        while (parent != null) {
            override = overrides.get(parent);
            if (override != null && !override.isExpired()) return override.getLevel();
            parent = getParentPackage(parent);
        }
        return null; // use configured default
    }
}

public class LevelOverride {
    private LogLevel level;
    private LocalDateTime createdAt;
    private Duration ttl; // null = permanent until explicitly changed

    public boolean isExpired() {
        if (ttl == null) return false;
        return LocalDateTime.now().isAfter(createdAt.plus(ttl));
    }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

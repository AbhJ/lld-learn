# Circuit Breaker - Variations

## Variation 1: Bulkhead Pattern
**Learning Value:** Teaches failure isolation, resource partitioning, and bulkhead-based fault containment.

### Additional Requirements
- Isolate failures per downstream service
- Separate thread pool (or semaphore) per dependency
- Prevent one failing service from consuming all resources
- Configurable bulkhead size per service

### Design Changes
- Add Bulkhead class with configurable concurrency limit
- Add BulkheadRegistry to manage per-service bulkheads
- Combine with CircuitBreaker (bulkhead inside circuit breaker)
- Add BulkheadFullException when limit reached

### Solution Approach
Assign each downstream dependency its own concurrency limiter (semaphore-based or thread-pool-based). When calling Service A, acquire a permit from A's bulkhead; if no permits available, fail fast with BulkheadFullException. This ensures that if Service A is slow (consuming all threads waiting for responses), Service B calls are unaffected because they use a separate pool. Semaphore-based bulkheads are lighter (share a common thread pool but limit concurrent calls). Thread-pool-based bulkheads provide stronger isolation but use more resources.

### Key Classes to Add
```java
public class Bulkhead {
    private final String name;
    private final Semaphore permits;
    private final long maxWaitMs;
    
    public Bulkhead(String name, int maxConcurrent, long maxWaitMs) {
        this.name = name;
        this.permits = new Semaphore(maxConcurrent);
        this.maxWaitMs = maxWaitMs;
    }
    
    public <T> T execute(Callable<T> call) throws Exception {
        if (!permits.tryAcquire(maxWaitMs, TimeUnit.MILLISECONDS)) {
            throw new BulkheadFullException(name + " bulkhead is full");
        }
        try {
            return call.call();
        } finally {
            permits.release();
        }
    }
}

public class ResilientClient {
    private final CircuitBreaker circuitBreaker;
    private final Bulkhead bulkhead;
    
    public <T> T call(ServiceCall<T> serviceCall) throws Exception {
        return circuitBreaker.execute(() -> bulkhead.execute(serviceCall::call));
    }
}
```

---

## Variation 2: Retry with Circuit Breaker
**Learning Value:** Introduces retry orchestration, backoff strategies, and retry budget management to prevent storms.

### Additional Requirements
- Retry transient failures before counting as circuit breaker failure
- Configurable retry count, backoff strategy
- Distinguish retryable vs non-retryable exceptions
- Retry budget (limit retries across all callers)

### Design Changes
- Add RetryPolicy with max attempts and backoff
- Add RetryableException marker interface
- Combine retry logic before circuit breaker failure counting
- Add global RetryBudget to prevent retry storms

### Solution Approach
Wrap the service call in a retry layer that sits inside the circuit breaker. When a call fails with a retryable exception, retry up to N times with exponential backoff before reporting failure to the circuit breaker. Non-retryable exceptions (e.g., 400 Bad Request) immediately count as a failure. A global retry budget limits the total retry rate (e.g., max 20% of calls can be retries) to prevent retry storms where all clients retry simultaneously and overwhelm the recovering service.

### Key Classes to Add
```java
public class RetryPolicy {
    private final int maxAttempts;
    private final long baseDelayMs;
    private final double backoffMultiplier;
    private final Set<Class<? extends Exception>> retryableExceptions;
    
    public boolean isRetryable(Exception e) {
        return retryableExceptions.stream().anyMatch(cls -> cls.isInstance(e));
    }
    
    public long getDelay(int attempt) {
        return (long)(baseDelayMs * Math.pow(backoffMultiplier, attempt));
    }
}

public class RetryingCircuitBreaker {
    private final CircuitBreaker circuitBreaker;
    private final RetryPolicy retryPolicy;
    private final RetryBudget budget;
    
    public <T> T execute(ServiceCall<T> call) throws Exception {
        return circuitBreaker.execute(() -> {
            Exception lastException = null;
            for (int attempt = 0; attempt <= retryPolicy.getMaxAttempts(); attempt++) {
                try {
                    return call.call();
                } catch (Exception e) {
                    if (!retryPolicy.isRetryable(e) || !budget.tryAcquire()) throw e;
                    lastException = e;
                    Thread.sleep(retryPolicy.getDelay(attempt));
                }
            }
            throw lastException;
        });
    }
}
```

---

## Variation 3: Fallback Strategies
**Learning Value:** Practices graceful degradation, fallback chains, and cached response strategies.

### Additional Requirements
- Return cached/stale data when circuit is open
- Default/degraded response as fallback
- Fallback chain (try cache, then default, then error)
- Metrics on fallback usage

### Design Changes
- Add Fallback interface with multiple strategies
- Add FallbackChain for ordered fallback attempts
- Add CacheFallback that returns last successful response
- Add DegradedServiceFallback for partial functionality

### Solution Approach
When the circuit breaker is OPEN (or the call fails), invoke a fallback instead of throwing an exception. The fallback is a chain: first try the cache (return the last successful response for this key), then try a default value, then try a degraded service (e.g., return fewer fields, use a backup service). Track which fallback was used in metrics. Cache fallback requires storing the last successful response per cache key with a TTL. This provides graceful degradation rather than hard failures.

### Key Classes to Add
```java
public interface Fallback<T> {
    T execute(Exception originalException);
    String getName();
}

public class CacheFallback<T> implements Fallback<T> {
    private final Map<String, CacheEntry<T>> cache = new ConcurrentHashMap<>();
    
    public void recordSuccess(String key, T value) {
        cache.put(key, new CacheEntry<>(value, System.currentTimeMillis()));
    }
    
    public T execute(Exception ex) {
        CacheEntry<T> entry = cache.get(currentKey());
        if (entry != null && !entry.isExpired()) return entry.getValue();
        throw new FallbackExhaustedException("No cached value available");
    }
}

public class ResilientServiceCall<T> {
    private final CircuitBreaker cb;
    private final List<Fallback<T>> fallbackChain;
    
    public T execute(ServiceCall<T> call) {
        try {
            return cb.execute(call);
        } catch (Exception e) {
            for (Fallback<T> fallback : fallbackChain) {
                try { return fallback.execute(e); } catch (Exception ignored) {}
            }
            throw new ServiceUnavailableException("All fallbacks exhausted", e);
        }
    }
}
```

---

## Variation 4: Health-Check Based Circuit Breaker
**Learning Value:** Explores trade-offs between probe overhead and recovery speed in health-check-driven state transitions.

### Additional Requirements
- Active health probing while circuit is OPEN
- Dedicated health endpoint polling (not just timeout-based)
- Gradual traffic ramp-up on recovery
- Health check intervals configurable

### Design Changes
- Add HealthChecker that periodically pings the service
- Replace timeout-based HALF_OPEN with health-check-driven transition
- Add TrafficRampUp for gradual recovery (10%, 25%, 50%, 100%)
- Add HealthEndpoint interface for service health probing

### Solution Approach
Instead of waiting for a timeout to transition from OPEN to HALF_OPEN, actively probe the service's health endpoint at regular intervals while the circuit is open. When health checks start succeeding, transition to a recovery state where traffic is gradually ramped up (canary approach): first send 10% of traffic, if success rate is good increase to 25%, then 50%, then fully close the circuit. This avoids the thundering herd problem where all requests hit a recovering service simultaneously. Health checks use a separate thread and lightweight ping (e.g., GET /health).

### Key Classes to Add
```java
public class HealthCheckCircuitBreaker extends CircuitBreaker {
    private final HealthChecker healthChecker;
    private final TrafficRampUp rampUp;
    
    class HealthChecker implements Runnable {
        private final String healthEndpoint;
        private final long intervalMs;
        
        public void run() {
            while (getState() == State.OPEN) {
                boolean healthy = pingHealthEndpoint();
                if (healthy) transitionToRecovery();
                Thread.sleep(intervalMs);
            }
        }
    }
    
    class TrafficRampUp {
        private double currentPercentage = 0.1; // start at 10%
        private final double[] stages = {0.1, 0.25, 0.5, 1.0};
        
        public boolean shouldAllowRequest() {
            return ThreadLocalRandom.current().nextDouble() < currentPercentage;
        }
        
        public void promoteStage() {
            // Move to next percentage if success rate is above threshold
        }
    }
}
```

---

## Variation 5: Sliding Window Metrics
**Learning Value:** Deepens understanding of sliding window algorithms, percentile tracking, and metrics-driven decisions.

### Additional Requirements
- Count-based sliding window (last N calls)
- Time-based sliding window (last N seconds)
- Percentile-based thresholds (p99 latency > 2s triggers open)
- Minimum call threshold before evaluation

### Design Changes
- Add SlidingWindow interface with count-based and time-based implementations
- Add LatencyTracker for percentile calculations
- Add MetricsSnapshot with failure rate, slow call rate, latency percentiles
- Configure thresholds as failure rate OR slow call rate

### Solution Approach
Replace a simple failure counter with a sliding window. Count-based: track the last N calls in a circular buffer, calculate failure rate over the window. Time-based: divide time into buckets (e.g., 1-second buckets over a 10-second window), aggregate metrics across active buckets. Track both failures and slow calls (calls exceeding a latency threshold). The circuit opens when failure rate exceeds the threshold (e.g., 50%) OR slow call rate exceeds its threshold (e.g., 80% of calls > 2s). Require a minimum number of calls before evaluating (avoid opening on a single failure).

### Key Classes to Add
```java
public class SlidingWindowMetrics {
    private final int windowSize;
    private final CallResult[] window; // circular buffer
    private int head;
    
    public MetricsSnapshot getSnapshot() {
        int failures = 0, slowCalls = 0, total = 0;
        for (CallResult r : window) {
            if (r == null) continue;
            total++;
            if (r.isFailed()) failures++;
            if (r.getDurationMs() > slowCallThreshold) slowCalls++;
        }
        return new MetricsSnapshot(
            (double) failures / total,
            (double) slowCalls / total,
            total
        );
    }
}

public class TimeBasedSlidingWindow {
    private final int windowSizeSeconds;
    private final AtomicReferenceArray<Bucket> buckets;
    
    class Bucket {
        long epochSecond;
        AtomicInteger totalCalls;
        AtomicInteger failedCalls;
        AtomicLong totalDuration;
    }
    
    private Bucket getCurrentBucket() {
        long now = Instant.now().getEpochSecond();
        int index = (int)(now % windowSizeSeconds);
        // Reset bucket if it's from a previous window rotation
        return buckets.get(index);
    }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

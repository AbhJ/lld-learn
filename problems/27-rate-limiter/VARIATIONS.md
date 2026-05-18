# Rate Limiter - Variations

## Variation 1: Distributed Rate Limiting
**Learning Value:** Teaches distributed coordination, shared state synchronization, and cluster-wide limit enforcement.

### Additional Requirements
- Consistent rate limiting across multiple server nodes
- Remote shared state backend (in-memory store)
- Handle network partitions gracefully
- Minimize synchronization overhead
- Support both exact and approximate counting

### Design Changes
- Add `DistributedRateLimiter` using remote-store-backed counters
- Add `RemoteStoreBackend` with atomic script operations
- Add `SyncStrategy` (synchronous vs eventual consistency)
- Add `LocalCache` for reducing remote store round-trips
- Add partition-tolerant fallback behavior

### Solution Approach
Use a centralized in-memory store with atomic operations (increment + expire or scripting) to maintain counters. Each request increments the counter atomically and checks against the limit. For high-throughput scenarios, a hybrid approach uses local counters that sync with the remote store periodically (e.g., every 100ms), trading exact accuracy for reduced latency. On network partition, nodes fall back to local-only limiting with a conservative fraction of the total limit (limit/node_count). Sliding window counters use sorted sets with timestamps. Token buckets use an atomic script that checks and decrements tokens.

### Key Classes to Add
```java
public class DistributedRateLimiter implements RateLimiter {
    private RemoteStoreBackend remoteStore;
    private String keyPrefix;
    private int limit;
    private Duration window;
    
    public boolean allowRequest(Request request) {
        String key = keyPrefix + ":" + request.getClientId();
        // Atomic script: INCREMENT key, SET expiry if new, check against limit
        long count = remoteStore.incrementAndGet(key, window);
        return count <= limit;
    }
}

public class RemoteStoreBackend {
    public long incrementAndGet(String key, Duration ttl) {
        // Atomic: BEGIN -> INCREMENT key -> SET EXPIRY key ttl -> COMMIT
    }
    
    public boolean tokenBucketAllow(String key, int capacity, int refillRate) {
        // Atomic script for token bucket
    }
}

public class HybridRateLimiter implements RateLimiter {
    private AtomicInteger localCounter;
    private DistributedRateLimiter distributed;
    private int syncBatchSize;
    
    public boolean allowRequest(Request request) { ... }
}
```

---

## Variation 2: Tiered Rate Limits
**Learning Value:** Introduces hierarchical policy management, user-tier differentiation, and configurable limit rules.

### Additional Requirements
- Different limits per API plan (free: 100/min, pro: 1000/min, enterprise: 10000/min)
- Dynamic plan lookup per request (API key -> plan)
- Upgrade/downgrade takes effect immediately
- Soft limits with warning headers vs hard limits with rejection
- Usage dashboard and approaching-limit notifications

### Design Changes
- Add `RateLimitTier` with configurable limits per tier
- Add `PlanResolver` mapping API keys to rate limit configurations
- Add `UsageTracker` recording consumption per client
- Add `WarningThreshold` for approaching-limit notifications
- Add HTTP headers (X-RateLimit-Limit, X-RateLimit-Remaining)

### Solution Approach
Each API key is associated with a plan/tier that defines its rate limits. On each request, the system resolves the API key to its tier, looks up the applicable limits, and checks consumption against those limits. Rate limit headers are included in every response showing the current limit, remaining quota, and reset time. When usage crosses a warning threshold (e.g., 80%), notification callbacks are triggered. Soft limits allow requests through but flag them (useful for gradual enforcement). Plan changes take effect immediately by updating the tier mapping; in-progress windows may allow existing consumption.

### Key Classes to Add
```java
public class TieredRateLimiter implements RateLimiter {
    private PlanResolver planResolver;
    private Map<String, RateLimiter> perClientLimiters;
    
    public boolean allowRequest(Request request) {
        RateLimitTier tier = planResolver.resolve(request.getApiKey());
        RateLimiter limiter = getOrCreateLimiter(request.getClientId(), tier);
        return limiter.allowRequest(request);
    }
}

public class RateLimitTier {
    private String name; // FREE, PRO, ENTERPRISE
    private int requestsPerMinute;
    private int requestsPerDay;
    private int burstCapacity;
    private double warningThreshold; // 0.8 = warn at 80%
}

public class RateLimitHeaders {
    private int limit;
    private int remaining;
    private Instant resetAt;
    
    public Map<String, String> toHttpHeaders() { ... }
}
```

---

## Variation 3: Adaptive Rate Limiting
**Learning Value:** Practices feedback-loop control, load-adaptive behavior, and system health-driven throttling.

### Additional Requirements
- Dynamically adjust limits based on server load/health
- Graceful degradation under high load
- Priority-based shedding (shed low-priority traffic first)
- Recovery: gradually restore limits as load decreases
- Health metrics integration (CPU, memory, latency)

### Design Changes
- Add `AdaptiveRateLimiter` with dynamic limit adjustment
- Add `LoadMonitor` tracking server health metrics
- Add `PriorityShedder` for preferential traffic handling
- Add `RecoveryController` for gradual limit restoration
- Add `BackpressureSignal` for upstream load communication

### Solution Approach
The system monitors server health metrics (CPU utilization, memory, response latency, error rate). When metrics exceed thresholds, the rate limiter automatically reduces allowed throughput. Traffic is shed by priority: non-essential/free-tier requests are rejected first, while critical/paid traffic continues. As load decreases, limits are gradually restored (not instantly, to prevent oscillation). The adaptation uses a feedback loop: measure load -> compare to target -> adjust limit -> observe effect. A PID controller or simpler proportional control adjusts the limit smoothly. Circuit-breaker-style isolation can fully stop traffic to degraded services.

### Key Classes to Add
```java
public class AdaptiveRateLimiter implements RateLimiter {
    private volatile int currentLimit;
    private int baseLimit;
    private LoadMonitor monitor;
    private RecoveryController recovery;
    
    public boolean allowRequest(Request request) {
        if (monitor.isOverloaded()) {
            currentLimit = calculateReducedLimit();
            if (request.getPriority() == Priority.LOW) return false;
        }
        return checkLimit(request, currentLimit);
    }
    
    private int calculateReducedLimit() {
        double loadFactor = monitor.getLoadFactor(); // 0.0 to 1.0+
        return (int)(baseLimit * Math.max(0.1, 1.0 - loadFactor));
    }
}

public class LoadMonitor {
    private double cpuThreshold;
    private double latencyThreshold;
    
    public boolean isOverloaded() { ... }
    public double getLoadFactor() { ... }
}

public class RecoveryController {
    private double recoveryRate; // How fast to restore limits
    
    public int getRecoveredLimit(int reducedLimit, int baseLimit) { ... }
}
```

---

## Variation 4: Per-Endpoint Limits
**Learning Value:** Explores trade-offs between granularity and complexity in per-resource rate limiting strategies.

### Additional Requirements
- Different limits for different API endpoints
- Higher limits for reads, lower for writes
- Expensive operations (search, export) have separate budgets
- Combined limit (global) plus per-endpoint limits
- Endpoint grouping (all /users/* endpoints share a limit)

### Design Changes
- Add `EndpointRateLimit` configuration per route
- Add `EndpointMatcher` (exact, prefix, regex) for route grouping
- Add `CompositeRateLimiter` checking multiple limits per request
- Add `CostFunction` assigning weight to expensive operations
- Modify request processing to extract endpoint information

### Solution Approach
Each API endpoint or endpoint group has its own rate limit configuration. A request is checked against multiple limits: the global per-client limit AND the per-endpoint limit (both must allow). Endpoint matching uses a hierarchical lookup: exact match first, then prefix match, then default. Expensive operations (e.g., full-text search, data export) can be assigned a higher "cost" so they consume more from the budget per request. Configuration is stored in a map of endpoint patterns to limit configs, loadable from a config file for easy updates without code changes.

### Key Classes to Add
```java
public class PerEndpointRateLimiter implements RateLimiter {
    private RateLimiter globalLimiter;
    private Map<String, RateLimiter> endpointLimiters;
    private EndpointMatcher matcher;
    
    public boolean allowRequest(Request request) {
        if (!globalLimiter.allowRequest(request)) return false;
        String endpoint = matcher.match(request.getPath());
        RateLimiter endpointLimiter = endpointLimiters.get(endpoint);
        return endpointLimiter == null || endpointLimiter.allowRequest(request);
    }
}

public class EndpointConfig {
    private String pattern; // "/api/users/*"
    private int requestsPerMinute;
    private int costPerRequest; // 1 for reads, 5 for writes, 10 for search
}

public class CostBasedLimiter implements RateLimiter {
    private int budget;
    private Map<String, Integer> endpointCosts;
    
    public boolean allowRequest(Request request) {
        int cost = endpointCosts.getOrDefault(request.getPath(), 1);
        return tryConsume(request.getClientId(), cost);
    }
}
```

---

## Variation 5: Rate Limit with Quotas
**Learning Value:** Deepens understanding of quota management, budget allocation, and periodic replenishment patterns.

### Additional Requirements
- Daily/monthly caps in addition to per-second/minute rate limits
- Burst allowance (short-term spike above steady-state rate)
- Quota reset at calendar boundaries (midnight, month start)
- Quota pools shared across API keys in same organization
- Usage reports and billing integration

### Design Changes
- Add `Quota` class with period, limit, and consumption tracking
- Add `BurstAllowance` on top of sustained rate limit
- Add `QuotaReset` scheduler for periodic reset
- Add `OrganizationQuota` shared across multiple API keys
- Add `UsageReport` for quota consumption history

### Solution Approach
Quotas are long-term budgets (daily: 10,000 requests, monthly: 100,000 requests) separate from short-term rate limits (10 requests/second). Both are checked per request; either can reject. Burst allowance permits temporary spikes above the sustained rate (e.g., rate limit is 10/sec but burst allows 50 in one second, borrowing from future capacity). Quotas reset at calendar boundaries using a scheduler. Organization-level quotas are shared pools where multiple API keys consume from the same budget. Usage reporting tracks consumption over time for billing, alerting, and capacity planning purposes.

### Key Classes to Add
```java
public class QuotaManager {
    private Map<String, Quota> dailyQuotas;
    private Map<String, Quota> monthlyQuotas;
    
    public boolean checkQuota(String clientId) {
        Quota daily = dailyQuotas.get(clientId);
        Quota monthly = monthlyQuotas.get(clientId);
        return daily.hasRemaining() && monthly.hasRemaining();
    }
    
    public void consume(String clientId, int amount) { ... }
    public void resetDaily() { ... }
    public void resetMonthly() { ... }
}

public class Quota {
    private long limit;
    private AtomicLong consumed;
    private Instant resetAt;
    
    public boolean hasRemaining() { return consumed.get() < limit; }
    public long getRemaining() { return limit - consumed.get(); }
}

public class BurstAllowance {
    private int burstCapacity;
    private int sustainedRate;
    private TokenBucket burstBucket;
    
    public boolean allowBurst(Request request) {
        return burstBucket.tryConsume(1);
    }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

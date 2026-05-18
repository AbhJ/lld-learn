# Task Scheduler - Variations

## Variation 1: Distributed Scheduler (DAG-Based)
**Learning Value:** Teaches distributed task coordination, DAG-based dependencies, and fault-tolerant execution.

### Additional Requirements
- DAG-based workflow definition (tasks with dependencies)
- Distributed worker nodes for execution
- Fault tolerance (worker failure detection and task re-assignment)
- Workflow versioning and history
- Task state persistence for recovery
- Central coordinator with worker heartbeats

### Design Changes
- Add `DAG` class representing directed acyclic graph of tasks
- Add `WorkerNode` with heartbeat and capacity tracking
- Add `Coordinator` for task distribution and failover
- Add `TaskStateStore` for persistent state (DB-backed)
- Add `WorkflowVersion` for DAG versioning
- Add `FailureDetector` using heartbeat timeouts

### Solution Approach
Workflows are defined as `DAG` objects with tasks as nodes and dependencies as edges. The `Coordinator` maintains the overall workflow state and distributes ready tasks (all dependencies satisfied) to available `WorkerNode` instances. Workers send periodic heartbeats; if a heartbeat is missed for N seconds, the `FailureDetector` marks the worker as dead and re-assigns its tasks to other workers. Task state is persisted in `TaskStateStore` so that on coordinator restart, in-progress workflows can resume from their last known state. The coordinator uses topological sort to determine execution order within a DAG, parallelizing independent branches.

### Key Classes to Add
```java
public class DAG {
    private String dagId;
    private Map<String, TaskNode> tasks;
    private Map<String, Set<String>> dependencies; // taskId -> set of dependency taskIds

    public List<String> getReadyTasks() {
        return tasks.keySet().stream()
            .filter(taskId -> tasks.get(taskId).getState() == TaskState.PENDING)
            .filter(taskId -> dependencies.getOrDefault(taskId, Collections.emptySet())
                .stream().allMatch(dep -> tasks.get(dep).getState() == TaskState.COMPLETED))
            .collect(Collectors.toList());
    }

    public boolean isComplete() {
        return tasks.values().stream().allMatch(t -> t.getState() == TaskState.COMPLETED);
    }
}

public class Coordinator {
    private Map<String, WorkerNode> workers;
    private TaskStateStore stateStore;
    private FailureDetector failureDetector;

    public void scheduleDAG(DAG dag) {
        while (!dag.isComplete()) {
            List<String> readyTasks = dag.getReadyTasks();
            for (String taskId : readyTasks) {
                WorkerNode worker = selectWorker();
                worker.assign(dag.getTask(taskId));
                stateStore.updateState(taskId, TaskState.RUNNING, worker.getId());
            }
            checkForFailures();
        }
    }

    private void checkForFailures() {
        for (WorkerNode worker : failureDetector.getFailedWorkers()) {
            List<TaskNode> orphanedTasks = worker.getAssignedTasks();
            for (TaskNode task : orphanedTasks) {
                task.setState(TaskState.PENDING); // re-queue
            }
        }
    }
}
```

---

## Variation 2: Cron Expression Parser
**Learning Value:** Introduces temporal expression parsing, interval calculation, and schedule validation.

### Additional Requirements
- Parse standard cron syntax (minute, hour, day, month, weekday)
- Calculate next fire time from current time
- Support special characters (*, /, -, ,)
- Handle timezone-aware scheduling
- Support non-standard extensions (@daily, @hourly)
- List next N execution times

### Design Changes
- Add `CronExpression` parser with field validation
- Add `CronField` for each time component (minute, hour, etc.)
- Add `NextFireTimeCalculator` for determining upcoming executions
- Add `CronScheduledTask` combining task with cron schedule
- Add `TimezoneHandler` for timezone-aware calculations

### Solution Approach
The `CronExpression` parser breaks the expression into 5 fields (minute, hour, day-of-month, month, day-of-week). Each `CronField` parses its component: `*` means all values, `5` means exactly 5, `1-5` means range, `*/15` means every 15th, `1,3,5` means specific values. The `NextFireTimeCalculator` starts from the current time and finds the next matching timestamp by iterating forward: first find next valid minute, then check hour, then day, etc. If a field doesn't match, roll forward to the next valid value and reset lower-order fields. The calculation is O(1) amortized for most expressions. Timezone handling converts calculations to the user's local time.

### Key Classes to Add
```java
public class CronExpression {
    private CronField minute;    // 0-59
    private CronField hour;      // 0-23
    private CronField dayOfMonth;// 1-31
    private CronField month;     // 1-12
    private CronField dayOfWeek; // 0-6 (Sunday=0)

    public static CronExpression parse(String expression) {
        String[] parts = expression.trim().split("\\s+");
        if (parts.length != 5) throw new InvalidCronException(expression);
        return new CronExpression(
            CronField.parse(parts[0], 0, 59),
            CronField.parse(parts[1], 0, 23),
            CronField.parse(parts[2], 1, 31),
            CronField.parse(parts[3], 1, 12),
            CronField.parse(parts[4], 0, 6)
        );
    }

    public LocalDateTime nextFireTime(LocalDateTime from) {
        LocalDateTime candidate = from.plusMinutes(1).withSecond(0).withNano(0);
        // Iterate forward until all fields match
        while (!matches(candidate)) {
            candidate = advanceToNextMatch(candidate);
        }
        return candidate;
    }

    public List<LocalDateTime> nextFireTimes(LocalDateTime from, int count) {
        List<LocalDateTime> times = new ArrayList<>();
        LocalDateTime current = from;
        for (int i = 0; i < count; i++) {
            current = nextFireTime(current);
            times.add(current);
        }
        return times;
    }
}
```

---

## Variation 3: Rate-limited Execution
**Learning Value:** Practices throughput control, token bucket integration, and fair scheduling under constraints.

### Additional Requirements
- Maximum concurrent tasks globally and per category
- Throttling per task category/type
- Token bucket or sliding window rate limiting
- Queue overflow handling (reject, drop oldest, backpressure)
- Priority-aware rate limiting (high priority gets more capacity)
- Dynamic rate adjustment based on downstream health

### Design Changes
- Add `RateLimiter` interface with Token Bucket and Sliding Window implementations
- Add `ConcurrencyLimiter` for max parallel execution
- Add `CategoryThrottle` for per-type limits
- Add `OverflowPolicy` (reject, drop, backpressure)
- Add `AdaptiveRateLimiter` that adjusts based on error rates
- Modify `Scheduler` to check rate limits before execution

### Solution Approach
Before executing any task, the scheduler consults the `RateLimiter` and `ConcurrencyLimiter`. The `TokenBucketRateLimiter` maintains a bucket per category that refills at a configured rate (e.g., 100 API calls per minute). If tokens are available, the task proceeds; otherwise, it's queued until tokens refill. The `ConcurrencyLimiter` tracks running tasks (using a semaphore) and blocks new submissions when at capacity. The `AdaptiveRateLimiter` monitors downstream error rates; if errors spike (circuit is half-open), it automatically reduces the rate. Priority-aware limiting reserves a portion of capacity for high-priority tasks (e.g., 30% reserved for CRITICAL).

### Key Classes to Add
```java
public class TokenBucketRateLimiter implements RateLimiter {
    private double tokens;
    private double maxTokens;
    private double refillRate; // tokens per second
    private long lastRefillTime;

    public synchronized boolean tryAcquire() {
        refill();
        if (tokens >= 1.0) {
            tokens -= 1.0;
            return true;
        }
        return false;
    }

    private void refill() {
        long now = System.currentTimeMillis();
        double elapsed = (now - lastRefillTime) / 1000.0;
        tokens = Math.min(maxTokens, tokens + elapsed * refillRate);
        lastRefillTime = now;
    }

    public long getWaitTimeMs() {
        if (tokens >= 1.0) return 0;
        return (long)((1.0 - tokens) / refillRate * 1000);
    }
}

public class CategoryThrottle {
    private Map<String, RateLimiter> categoryLimiters;

    public boolean canExecute(Task task) {
        String category = task.getCategory();
        RateLimiter limiter = categoryLimiters.get(category);
        return limiter == null || limiter.tryAcquire();
    }
}
```

---

## Variation 4: Task Retry with Backoff
**Learning Value:** Explores trade-offs between reliability and resource cost in retry and backoff strategies.

### Additional Requirements
- Exponential backoff with jitter for retries
- Maximum retry count per task
- Circuit breaker integration (stop retrying if system is down)
- Retry budget (max % of requests that can be retries)
- Per-exception retry decisions (retry on timeout, not on 4xx)
- Retry event logging and metrics

### Design Changes
- Add `RetryPolicy` with configurable backoff strategy
- Add `BackoffStrategy` interface (fixed, exponential, exponential with jitter)
- Add `CircuitBreaker` for system-level failure protection
- Add `RetryBudget` limiting total retry traffic
- Add `RetryableExceptionClassifier` for deciding which errors to retry
- Modify task execution to wrap with retry logic

### Solution Approach
When a task fails, the `RetryPolicy` determines whether to retry based on: (1) the exception type (via `RetryableExceptionClassifier` - timeout and 5xx are retryable, validation errors are not), (2) attempt count (below max retries?), (3) circuit breaker state (if open, don't retry), and (4) retry budget (if 20% of recent traffic is already retries, stop). If retryable, the `BackoffStrategy` calculates the delay: exponential (baseDelay * 2^attempt) with full jitter (random between 0 and calculated delay) to prevent synchronized retry storms. The task is re-queued with the calculated delay. All retry decisions and outcomes are logged for observability.

### Key Classes to Add
```java
public class RetryPolicy {
    private int maxAttempts;
    private BackoffStrategy backoffStrategy;
    private RetryableExceptionClassifier classifier;
    private CircuitBreaker circuitBreaker;
    private RetryBudget budget;

    public RetryDecision shouldRetry(Task task, Exception error, int attempt) {
        if (attempt >= maxAttempts) return RetryDecision.giveUp("Max attempts reached");
        if (!classifier.isRetryable(error)) return RetryDecision.giveUp("Non-retryable error");
        if (circuitBreaker.isOpen()) return RetryDecision.giveUp("Circuit is open");
        if (!budget.hasCapacity()) return RetryDecision.giveUp("Retry budget exhausted");

        long delay = backoffStrategy.calculateDelay(attempt);
        budget.recordRetry();
        return RetryDecision.retryAfter(delay);
    }
}

public class ExponentialBackoffWithJitter implements BackoffStrategy {
    private long baseDelayMs = 1000;
    private long maxDelayMs = 60000;
    private Random random = new Random();

    public long calculateDelay(int attempt) {
        long exponentialDelay = (long)(baseDelayMs * Math.pow(2, attempt));
        long cappedDelay = Math.min(exponentialDelay, maxDelayMs);
        // Full jitter: random between 0 and cappedDelay
        return (long)(random.nextDouble() * cappedDelay);
    }
}
```

---

## Variation 5: Priority Aging
**Learning Value:** Deepens understanding of starvation prevention, fairness algorithms, and dynamic priority adjustment.

### Additional Requirements
- Low priority tasks get priority boost over time to prevent starvation
- Configurable aging rate per priority level
- Maximum effective priority cap (aged tasks don't exceed HIGH)
- Aging reset on execution (back to base priority)
- Starvation metrics and alerting
- Fair scheduling guarantee (every task runs within bounded time)

### Design Changes
- Add `PriorityAger` that periodically boosts waiting tasks
- Add `EffectivePriority` calculated from base priority + age bonus
- Add `AgingPolicy` with configurable rates per level
- Add `StarvationDetector` for monitoring long-waiting tasks
- Add `FairnessGuarantee` with maximum wait time bounds
- Modify priority queue comparator to use effective priority

### Solution Approach
Each task has a base priority and an effective priority. The `PriorityAger` runs periodically (every N seconds) and increments the effective priority of waiting tasks based on their wait time. The aging formula: `effectivePriority = basePriority + (waitTime / agingInterval) * agingBoost`. The `AgingPolicy` configures how fast each level ages (LOW tasks age faster than MEDIUM to catch up). A cap prevents low-priority tasks from reaching CRITICAL level. When a task finally executes, its effective priority resets to base. The `StarvationDetector` monitors tasks waiting beyond a threshold and can force-boost them. The priority queue uses effective priority for ordering, recalculated on each dequeue.

### Key Classes to Add
```java
public class PriorityAger {
    private AgingPolicy policy;
    private StarvationDetector starvationDetector;

    public void ageWaitingTasks(List<Task> waitingTasks) {
        long currentTime = System.currentTimeMillis();
        for (Task task : waitingTasks) {
            long waitTimeMs = currentTime - task.getSubmittedAt();
            int ageBoost = calculateAgeBoost(task.getBasePriority(), waitTimeMs);
            int effectivePriority = Math.min(
                task.getBasePriority().getValue() + ageBoost,
                policy.getMaxEffectivePriority().getValue()
            );
            task.setEffectivePriority(effectivePriority);

            if (waitTimeMs > policy.getStarvationThresholdMs()) {
                starvationDetector.onStarvation(task);
            }
        }
    }

    private int calculateAgeBoost(TaskPriority basePriority, long waitTimeMs) {
        double agingRate = policy.getAgingRate(basePriority); // LOW=2.0, MEDIUM=1.0, HIGH=0.5
        return (int)(waitTimeMs / policy.getAgingIntervalMs() * agingRate);
    }
}

public class AgingPolicy {
    private Map<TaskPriority, Double> agingRates;
    private long agingIntervalMs = 10_000; // boost every 10 seconds
    private TaskPriority maxEffectivePriority = TaskPriority.HIGH; // cap, never reaches CRITICAL
    private long starvationThresholdMs = 300_000; // 5 minutes
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

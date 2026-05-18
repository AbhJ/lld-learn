# Thread Pool - Variations

## Variation 1: Work-Stealing Pool
**Learning Value:** Teaches work-stealing algorithms, load balancing across workers, and lock-free queue design.

### Additional Requirements
- Each worker thread has its own local deque of tasks
- Idle workers steal tasks from the tail of busy workers' deques
- Minimize contention between threads
- Support recursive task decomposition (fork/join)

### Design Changes
- Replace single shared BlockingQueue with per-worker Deque
- Add WorkStealingWorker that checks local deque first, then steals
- Add ForkJoinTask base class for recursive decomposition
- Modify ThreadPool to use work-stealing scheduling

### Solution Approach
Each worker maintains a local double-ended queue. Workers push/pop from the head of their own deque (LIFO for cache locality). When a worker's deque is empty, it randomly selects another worker and steals from the tail of their deque (FIFO for larger chunks). This reduces contention since most operations are local. For recursive tasks, subtasks are pushed to the local deque and may be stolen by idle workers, naturally load-balancing the work.

### Key Classes to Add
```java
public class WorkStealingPool {
    private final WorkerThread[] workers;
    
    class WorkerThread extends Thread {
        private final Deque<Runnable> localQueue = new ConcurrentLinkedDeque<>();
        
        public void run() {
            while (!shutdown) {
                Runnable task = localQueue.pollFirst(); // try local
                if (task == null) task = steal();       // try stealing
                if (task != null) task.run();
                else Thread.yield();
            }
        }
        
        private Runnable steal() {
            // randomly pick another worker and take from tail
            WorkerThread victim = workers[ThreadLocalRandom.current().nextInt(workers.length)];
            return victim.localQueue.pollLast();
        }
    }
}
```

---

## Variation 2: Scheduled Thread Pool
**Learning Value:** Introduces delayed execution, periodic tasks, and timer-wheel scheduling mechanisms.

### Additional Requirements
- Support delayed execution (run task after X ms)
- Support periodic/fixed-rate execution
- Handle timer wheel for efficient scheduling
- Cancellation of scheduled tasks

### Design Changes
- Replace FIFO queue with DelayQueue or timer wheel
- Add ScheduledTask wrapper with nextExecutionTime
- Add ScheduledFuture for cancellation
- Support both fixed-rate and fixed-delay semantics

### Solution Approach
Use a priority queue (min-heap) ordered by next execution time. A dedicated timer thread polls the heap; when the top task's time arrives, it submits the task to the worker pool. For periodic tasks, after execution, recalculate the next execution time and re-insert into the heap. A timer wheel optimization buckets tasks into time slots for O(1) insertion when precision is coarse (e.g., 100ms buckets). Cancellation marks the ScheduledFuture as cancelled; the timer thread skips cancelled entries.

### Key Classes to Add
```java
public class ScheduledThreadPool {
    private final PriorityBlockingQueue<ScheduledTask> delayQueue;
    private final ThreadPool workerPool;
    
    public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
        ScheduledTask st = new ScheduledTask(task, System.nanoTime() + unit.toNanos(delay));
        delayQueue.offer(st);
        return st.getFuture();
    }
    
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long period, TimeUnit unit) {
        ScheduledTask st = new PeriodicTask(task, period, unit);
        delayQueue.offer(st);
        return st.getFuture();
    }
}
```

---

## Variation 3: Priority Thread Pool
**Learning Value:** Practices priority-based task ordering, preemption policies, and starvation prevention.

### Additional Requirements
- Tasks have priority levels (HIGH, MEDIUM, LOW)
- Higher priority tasks execute before lower priority
- Optional preemption of running lower-priority tasks
- Starvation prevention for low-priority tasks (aging)

### Design Changes
- Replace FIFO queue with PriorityBlockingQueue
- Add priority field to Task interface
- Add aging mechanism (priority boost over wait time)
- Optional: preemption via thread interruption and re-queue

### Solution Approach
Use a PriorityBlockingQueue that orders tasks by priority. To prevent starvation, implement aging: each task tracks its enqueue time, and the effective priority increases with wait time. For preemption, when a high-priority task arrives and all workers are busy with lower-priority tasks, interrupt the lowest-priority running task, re-enqueue it, and assign the high-priority task. Track running tasks per worker to make preemption decisions.

### Key Classes to Add
```java
public class PriorityTask implements Comparable<PriorityTask> {
    private final Runnable task;
    private final int basePriority;
    private final long enqueueTime;
    
    public int getEffectivePriority() {
        long waitTime = System.currentTimeMillis() - enqueueTime;
        return basePriority + (int)(waitTime / AGING_INTERVAL_MS);
    }
    
    public int compareTo(PriorityTask other) {
        return Integer.compare(other.getEffectivePriority(), this.getEffectivePriority());
    }
}

public class PriorityThreadPool extends ThreadPool {
    private final PriorityBlockingQueue<PriorityTask> queue;
    private final Map<Thread, PriorityTask> runningTasks; // for preemption
}
```

---

## Variation 4: Elastic Thread Pool
**Learning Value:** Explores trade-offs between resource utilization and responsiveness in auto-scaling thread pools.

### Additional Requirements
- Auto-scale workers based on queue depth and load
- Scale-up quickly, scale-down with cooldown period
- Min and max bounds on pool size
- Metrics-driven scaling decisions

### Design Changes
- Add MonitorThread that periodically checks queue depth
- Add ScalingPolicy with scale-up/scale-down thresholds
- Add cooldown timer to prevent thrashing
- Track per-worker utilization metrics

### Solution Approach
A monitor thread runs every N milliseconds and checks: (1) queue depth vs threshold, (2) worker utilization (busy time / total time). If queue depth exceeds the high watermark and workers are above 80% utilized, spawn new workers up to max. If queue is empty and utilization is below 20% for longer than the cooldown period, remove idle workers down to min. Exponential scale-up (double workers) and linear scale-down (remove one at a time) prevents thrashing.

### Key Classes to Add
```java
public class ElasticThreadPool extends ThreadPool {
    private final ScalingPolicy policy;
    private final Thread monitorThread;
    
    class ScalingPolicy {
        int scaleUpQueueThreshold = 10;
        int scaleDownIdleSeconds = 60;
        double utilizationHighWatermark = 0.8;
        double utilizationLowWatermark = 0.2;
    }
    
    private void monitorAndScale() {
        while (!shutdown) {
            int queueDepth = taskQueue.size();
            double utilization = calculateUtilization();
            if (queueDepth > policy.scaleUpQueueThreshold && utilization > policy.utilizationHighWatermark) {
                scaleUp(Math.min(currentSize, maxSize - currentSize));
            } else if (utilization < policy.utilizationLowWatermark && idleDuration() > policy.scaleDownIdleSeconds) {
                scaleDown(1);
            }
            Thread.sleep(CHECK_INTERVAL_MS);
        }
    }
}
```

---

## Variation 5: Virtual Thread Pool (Project Loom)
**Learning Value:** Deepens understanding of lightweight concurrency, continuation-based scheduling, and modern threading models.

### Additional Requirements
- Lightweight virtual threads (millions possible)
- Virtual threads mounted on carrier (platform) threads
- Automatic unmounting on blocking operations
- Structured concurrency with task scopes

### Design Changes
- Replace platform Thread workers with virtual thread executor
- Add CarrierThreadPool as the underlying platform thread pool
- Handle pinning (synchronized blocks prevent unmounting)
- Add StructuredTaskScope for parent-child task relationships

### Solution Approach
Virtual threads are scheduled onto a small pool of carrier (platform) threads. When a virtual thread blocks (I/O, sleep, lock), it unmounts from the carrier, freeing it for other virtual threads. The scheduler is a work-stealing ForkJoinPool. Structured concurrency ensures child tasks complete before parent via StructuredTaskScope. The key insight is that you can create millions of virtual threads (one per task) without a bounded pool, since the runtime multiplexes them onto few carriers.

### Key Classes to Add
```java
public class VirtualThreadPool {
    private final ExecutorService executor;
    
    public VirtualThreadPool() {
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }
    
    public <T> Future<T> submit(Callable<T> task) {
        return executor.submit(task);
    }
    
    // Structured concurrency
    public <T> List<T> invokeAll(List<Callable<T>> tasks) throws Exception {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            List<StructuredTaskScope.Subtask<T>> subtasks = tasks.stream()
                .map(scope::fork)
                .toList();
            scope.join().throwIfFailed();
            return subtasks.stream().map(StructuredTaskScope.Subtask::get).toList();
        }
    }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

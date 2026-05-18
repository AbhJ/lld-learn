/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/PoolStats.java — Lock-free statistics using atomic counters
import java.util.concurrent.atomic.AtomicLong;

public class PoolStats {
    private final AtomicLong submitted = new AtomicLong(0); // AtomicLong = lock-free counter safe for many writers
    private final AtomicLong completed = new AtomicLong(0); // each worker increments without locking
    private final AtomicLong stolen = new AtomicLong(0);    // tracks work-stealing across workers
    private final AtomicLong failed = new AtomicLong(0);

    public void recordSubmit() { submitted.incrementAndGet(); }
    public void recordComplete() { completed.incrementAndGet(); }
    public void recordSteal() { stolen.incrementAndGet(); }
    public void recordFailure() { failed.incrementAndGet(); }

    public long getSubmitted() { return submitted.get(); }
    public long getCompleted() { return completed.get(); }
    public long getStolen() { return stolen.get(); }
    public long getFailed() { return failed.get(); }

    @Override
    public String toString() {
        return String.format("PoolStats[submitted=%d, completed=%d, stolen=%d, failed=%d]",
                submitted.get(), completed.get(), stolen.get(), failed.get());
    }
}

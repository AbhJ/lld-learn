/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/PrioritySchedulingStrategy.java — Highest priority first; ties broken by scheduled time
//
// Mirrors the original PriorityBlockingQueue ordering: Task.compareTo orders
// by priority level (descending) and then by scheduledTimeMs (ascending).

import java.util.Collection;

class PrioritySchedulingStrategy implements SchedulingStrategy {
    @Override
    public Task selectNext(Collection<Task> ready) {
        Task best = null;
        for (Task t : ready) {
            if (best == null || t.compareTo(best) < 0) {
                best = t;
            }
        }
        return best;
    }

    @Override
    public String name() { return "Priority"; }
}

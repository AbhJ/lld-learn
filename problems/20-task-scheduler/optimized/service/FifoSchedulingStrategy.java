/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/FifoSchedulingStrategy.java — Oldest-first scheduling, ignoring priority

import java.util.Collection;

class FifoSchedulingStrategy implements SchedulingStrategy {
    @Override
    public Task selectNext(Collection<Task> ready) {
        Task oldest = null;
        for (Task t : ready) {
            if (oldest == null || t.getScheduledTimeMs() < oldest.getScheduledTimeMs()) {
                oldest = t;
            }
        }
        return oldest;
    }

    @Override
    public String name() { return "FIFO"; }
}

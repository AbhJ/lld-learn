/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/SchedulingStrategy.java — Strategy contract for choosing the next ready task to dispatch

import java.util.Collection;

interface SchedulingStrategy {
    /** Pick which ready-to-run task should be dispatched next; null if none. */
    Task selectNext(Collection<Task> ready);

    /** Human-readable name for logging and demos. */
    String name();
}

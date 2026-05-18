/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/AppendOnlyLogPersistence.java — Strategy impl: simulates an append-only log
//
// In a real deployment this would fsync to disk on every append. For the
// problem demo we keep the log in memory but expose the same contract a
// disk-backed AOL would expose: append-only writes, full replay on read,
// and a compaction hook.

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class AppendOnlyLogPersistence implements PersistenceStrategy {
    private final List<Command> log = Collections.synchronizedList(new ArrayList<>());
    private long appendCount = 0;

    @Override
    public synchronized void append(Command command) {
        // Simulated durable append. A real impl would write to a file and fsync.
        log.add(command);
        appendCount++;
    }

    @Override
    public List<Command> getLog() {
        synchronized (log) {
            return Collections.unmodifiableList(new ArrayList<>(log));
        }
    }

    @Override
    public void replaceLog(List<Command> compacted) {
        // Simulated atomic swap: write compacted log to a temp file, then rename.
        synchronized (log) {
            log.clear();
            log.addAll(compacted);
        }
    }

    public long getAppendCount() { return appendCount; }
}

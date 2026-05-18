/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/PersistenceStrategy.java — Strategy contract for KV-store persistence backends
//
// Different deployments need different durability stories: in-memory only
// (for tests / caches), append-only log (for crash recovery), snapshot
// files, or networked storage. The store delegates persistence here so the
// core logic stays unchanged across backends.

import java.util.List;

interface PersistenceStrategy {
    /** Append a single command to the durable log. */
    void append(Command command);

    /** Return the full ordered command history (for replay / inspection). */
    List<Command> getLog();

    /** Replace the log with a compacted command list (e.g. after compaction). */
    void replaceLog(List<Command> compacted);
}

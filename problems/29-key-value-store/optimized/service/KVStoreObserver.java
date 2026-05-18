/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/KVStoreObserver.java — Observer contract for key-value store events
//
// Subscribers receive notifications when keys are written, deleted, or expire.
// Examples: a console logger, an audit/analytics sink, an invalidation hook.

interface KVStoreObserver {
    /** A key was set (newly created or overwritten). */
    void onSet(String key, String value, long ttlMs);

    /** A key was explicitly deleted. */
    void onDelete(String key);

    /** A key was removed because its TTL elapsed. */
    void onExpire(String key);
}

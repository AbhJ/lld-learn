/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/EvictionStrategy.java — Pluggable eviction strategies for the generic cache
// DESIGN PATTERN: Strategy

public interface EvictionStrategy<K> {           // interface = pluggable strategy; swap LRU/LFU without changing Cache
    void onAccess(K key);
    void onInsert(K key);
    K evict();
    void onRemove(K key);
    void clear();
    String getName();
}

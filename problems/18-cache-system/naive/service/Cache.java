/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/Cache.java — Cache interface with get/put/evict operations

public interface Cache<V> {                      // interface = contract all cache implementations must fulfill
    void put(String key, V value);
    V get(String key);
    boolean containsKey(String key);
    void remove(String key);
    int size();
    int capacity();
    void clear();
    CacheStats getStats();
    String getPolicyName();
}

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/DataSource.java — Source of truth that the cache fronts (Proxy target)

public interface DataSource<K, V> {              // generic so any backend (DB/HTTP/file) can be wrapped
    V load(K key);                               // load = fetch authoritative value for key
}

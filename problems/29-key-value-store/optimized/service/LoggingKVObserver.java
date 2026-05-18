/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/LoggingKVObserver.java — Console logger; demonstrates observing KV store events

class LoggingKVObserver implements KVStoreObserver {
    @Override
    public void onSet(String key, String value, long ttlMs) {
        if (ttlMs > 0) {
            System.out.println("  [event] SET " + key + "=" + value + " (ttl=" + ttlMs + "ms)");
        } else {
            System.out.println("  [event] SET " + key + "=" + value);
        }
    }

    @Override
    public void onDelete(String key) {
        System.out.println("  [event] DELETE " + key);
    }

    @Override
    public void onExpire(String key) {
        System.out.println("  [event] EXPIRE " + key);
    }
}

/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/KVStore.java — Naive: HashMap with TTL scan; pluggable persistence + observer fan-out
import java.util.*;

public class KVStore {
    private Map<String, Entry> data;                       // private = internal storage hidden; HashMap gives O(1) get/put

    // Strategy: pluggable persistence backend (in-memory list, AOL, etc.).
    private PersistenceStrategy persistence;

    // Observer: fan-out for KV events (set, delete, expire).
    private final List<KVStoreObserver> observers = new ArrayList<>();

    /** Default ctor uses an in-memory persistence strategy. */
    public KVStore() {
        this(new InMemoryPersistence());
    }

    /** Inject a different persistence strategy (e.g. AppendOnlyLogPersistence). */
    public KVStore(PersistenceStrategy persistence) {
        this.data = new HashMap<>();
        this.persistence = persistence;
    }

    public void set(String key, String value) { set(key, value, 0); }

    public void set(String key, String value, long ttlMs) {
        Entry entry = new Entry(key, value, ttlMs);
        data.put(key, entry);
        persistence.append(ttlMs > 0 ? Command.setWithTtl(key, value, ttlMs) : Command.set(key, value));
        for (KVStoreObserver o : observers) o.onSet(key, value, ttlMs);
    }

    public String get(String key) {
        Entry entry = data.get(key);
        if (entry == null) return null;
        // Naive: check expiration on every get (lazy expiration)
        if (entry.isExpired()) {
            data.remove(key);
            for (KVStoreObserver o : observers) o.onExpire(key);
            return null;
        }
        return entry.getValue();
    }

    public boolean delete(String key) {
        boolean existed = data.remove(key) != null;
        if (existed) {
            persistence.append(Command.delete(key));
            for (KVStoreObserver o : observers) o.onDelete(key);
        }
        return existed;
    }

    public boolean exists(String key) {
        Entry entry = data.get(key);
        if (entry == null) return false;
        if (entry.isExpired()) {
            data.remove(key);
            for (KVStoreObserver o : observers) o.onExpire(key);
            return false;
        }
        return true;
    }

    // Naive: full scan to find expired keys
    public void cleanupExpired() {
        Iterator<Map.Entry<String, Entry>> iter = data.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Entry> e = iter.next();
            if (e.getValue().isExpired()) {
                iter.remove();
                for (KVStoreObserver o : observers) o.onExpire(e.getKey());
            }
        }
    }

    public int size() { return data.size(); }
    public List<Command> getLog() { return persistence.getLog(); }

    // === Strategy plumbing ===
    public PersistenceStrategy getPersistence() { return persistence; }
    public void setPersistence(PersistenceStrategy persistence) { this.persistence = persistence; }

    // === Observer plumbing ===
    public void addObserver(KVStoreObserver observer) { observers.add(observer); }
    public void removeObserver(KVStoreObserver observer) { observers.remove(observer); }
}

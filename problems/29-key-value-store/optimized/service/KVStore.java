/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/KVStore.java — Optimized: ConcurrentHashMap + DelayQueue + pluggable persistence + observer fan-out
import java.util.*;
import java.util.concurrent.*;

public class KVStore {
    private ConcurrentHashMap<String, Entry> data;          // ConcurrentHashMap = lock-free reads/writes from multiple threads
    private DelayQueue<ExpiringKey> expirationQueue;        // DelayQueue = auto-surfaces expired entries; no full scan needed
    private volatile boolean compactionRunning;             // volatile = visible to all threads immediately when written

    // Strategy: pluggable persistence backend (in-memory list, AOL, etc.).
    private PersistenceStrategy persistence;

    // Observer: fan-out for KV events (set, delete, expire).
    private final List<KVStoreObserver> observers = new CopyOnWriteArrayList<>();

    /** Default ctor uses an in-memory persistence strategy. */
    public KVStore() {
        this(new InMemoryPersistence());
    }

    /** Inject a different persistence strategy (e.g. AppendOnlyLogPersistence). */
    public KVStore(PersistenceStrategy persistence) {
        this.data = new ConcurrentHashMap<>();
        this.expirationQueue = new DelayQueue<>();
        this.compactionRunning = false;
        this.persistence = persistence;

        // Background thread for expiration — no manual cleanup needed
        Thread expirationThread = new Thread(this::runExpirationLoop);
        expirationThread.setDaemon(true);
        expirationThread.start();
    }

    public void set(String key, String value) { set(key, value, 0); }

    public void set(String key, String value, long ttlMs) {
        Entry entry = new Entry(key, value, ttlMs);
        data.put(key, entry);
        persistence.append(ttlMs > 0 ? Command.setWithTtl(key, value, ttlMs) : Command.set(key, value));
        if (ttlMs > 0) {
            // WHY: DelayQueue handles expiration timing — O(log n) insert
            expirationQueue.offer(new ExpiringKey(key, System.currentTimeMillis() + ttlMs));
        }
        for (KVStoreObserver o : observers) o.onSet(key, value, ttlMs);
    }

    public String get(String key) {
        Entry entry = data.get(key);
        if (entry == null) return null;
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

    // WHY: Background compaction merges log entries to prevent unbounded log growth
    public void compact() {
        if (compactionRunning) return;
        compactionRunning = true;
        List<Command> compacted = new ArrayList<>();
        for (Map.Entry<String, Entry> e : data.entrySet()) {
            if (!e.getValue().isExpired()) {
                compacted.add(Command.set(e.getKey(), e.getValue().getValue()));
            }
        }
        persistence.replaceLog(compacted);
        compactionRunning = false;
    }

    // Expiration loop: DelayQueue.take() blocks until an entry expires
    private void runExpirationLoop() {
        while (true) {
            try {
                ExpiringKey expired = expirationQueue.take();
                Entry entry = data.get(expired.key);
                if (entry != null && entry.isExpired()) {
                    data.remove(expired.key);
                    for (KVStoreObserver o : observers) o.onExpire(expired.key);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
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

    private static class ExpiringKey implements Delayed {    // static = no outer class ref; implements Delayed for DelayQueue
        final String key;                                   // package-private final = immutable; accessed within class
        final long expireTimeMs;

        ExpiringKey(String key, long expireTimeMs) { this.key = key; this.expireTimeMs = expireTimeMs; }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(expireTimeMs - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed other) {
            return Long.compare(this.expireTimeMs, ((ExpiringKey) other).expireTimeMs);
        }
    }
}

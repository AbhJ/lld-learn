/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/KeyValueStore.java — ConcurrentHashMap with compute/merge for atomic read-modify-write

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class KeyValueStore {
    private final ConcurrentHashMap<String, AtomicReference<VersionedValue>> store = new ConcurrentHashMap<>(); // ConcurrentHashMap + AtomicReference = lock-free CAS per key

    public void put(String key, long value) {
        store.compute(key, (k, existing) -> {
            if (existing == null) {
                AtomicReference<VersionedValue> ref = new AtomicReference<>();
                ref.set(new VersionedValue(value, 1));
                return ref;
            } else {
                VersionedValue old = existing.get();
                existing.set(new VersionedValue(value, old.getVersion() + 1));
                return existing;
            }
        });
    }

    public Long get(String key) {
        AtomicReference<VersionedValue> ref = store.get(key);
        if (ref == null) return null;
        VersionedValue vv = ref.get();
        return vv != null ? vv.getValue() : null;
    }

    /**
     * Atomic increment using ConcurrentHashMap.compute.
     * This ensures no lost updates even under concurrent modification.
     */
    public long increment(String key) {
        AtomicReference<VersionedValue> ref = store.compute(key, (k, existing) -> {
            if (existing == null) {
                AtomicReference<VersionedValue> newRef = new AtomicReference<>();
                newRef.set(new VersionedValue(1, 1));
                return newRef;
            } else {
                VersionedValue old = existing.get();
                existing.set(new VersionedValue(old.getValue() + 1, old.getVersion() + 1));
                return existing;
            }
        });
        return ref.get().getValue();
    }

    /**
     * CAS-based update: only updates if current value matches expected.
     */
    public boolean compareAndSet(String key, long expected, long newValue) {
        AtomicReference<VersionedValue> ref = store.get(key); // AtomicReference = allows CAS on the value object
        if (ref == null) return false;
        while (true) {                                         // CAS loop = retry until success or value mismatch
            VersionedValue current = ref.get();
            if (current.getValue() != expected) return false;
            VersionedValue updated = new VersionedValue(newValue, current.getVersion() + 1);
            if (ref.compareAndSet(current, updated)) return true; // CAS = atomic swap; fails if another thread changed it
        }
    }

    public int size() { return store.size(); }

    public long getVersion(String key) {
        AtomicReference<VersionedValue> ref = store.get(key);
        if (ref == null) return 0;
        return ref.get().getVersion();
    }
}

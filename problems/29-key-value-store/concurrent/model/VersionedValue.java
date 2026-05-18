/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/VersionedValue.java — Immutable value with version number for CAS

public class VersionedValue {
    private final long value;       // final = immutable; safe to read from any thread without locks
    private final long version;     // final = version for CAS; enables optimistic concurrency control

    public VersionedValue(long value, long version) {
        this.value = value;
        this.version = version;
    }

    public long getValue() { return value; }
    public long getVersion() { return version; }

    @Override
    public String toString() {
        return "VersionedValue[value=" + value + ", version=" + version + "]";
    }
}

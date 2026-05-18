/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// command/Command.java — Store operation as executable object
public class Command {
    public enum Type { SET, DELETE }     // enum = fixed set of operations; type-safe command types
    private Type type;
    private String key;
    private String value;
    private long ttlMs;

    public Command(Type type, String key, String value, long ttlMs) {
        this.type = type; this.key = key; this.value = value; this.ttlMs = ttlMs;
    }

    public static Command set(String key, String value) { return new Command(Type.SET, key, value, 0); }
    public static Command setWithTtl(String key, String value, long ttlMs) { return new Command(Type.SET, key, value, ttlMs); }
    public static Command delete(String key) { return new Command(Type.DELETE, key, null, 0); }

    public Type getType() { return type; }
    public String getKey() { return key; }
    public String getValue() { return value; }
    public long getTtlMs() { return ttlMs; }
    @Override public String toString() { return type == Type.SET ? "SET " + key + " " + value : "DELETE " + key; }
}

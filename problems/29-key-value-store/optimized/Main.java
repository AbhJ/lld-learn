/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Demonstrates KV store with ConcurrentHashMap, DelayQueue, Strategy + Observer
public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Key-Value Store Demo (Optimized) ===");
        System.out.println("Optimizations: ConcurrentHashMap, DelayQueue expiration, background compaction\n");

        // Strategy is injected; here we use an append-only-log persistence backend.
        KVStore store = new KVStore(new AppendOnlyLogPersistence());
        // Observer: console logger receives set/delete/expire events.
        store.addObserver(new LoggingKVObserver());

        System.out.println("--- Basic Operations (lock-free via ConcurrentHashMap) ---");
        store.set("name", "Alice");
        store.set("age", "30");
        System.out.println("GET name -> " + store.get("name"));
        System.out.println("GET age -> " + store.get("age"));
        store.delete("name");
        System.out.println("DELETE name -> GET name = " + store.get("name"));

        System.out.println("\n--- TTL via DelayQueue (auto-expires, no scan) ---");
        store.set("session", "abc123", 1500);
        System.out.println("SET session (TTL 1.5s) -> " + store.get("session"));
        Thread.sleep(1600);
        System.out.println("After 1.6s -> " + (store.get("session") == null ? "(expired by background thread)" : store.get("session")));

        System.out.println("\n--- Background Compaction ---");
        store.set("x", "1"); store.set("x", "2"); store.set("x", "3");
        System.out.println("Log before compaction: " + store.getLog().size() + " entries");
        store.compact();
        System.out.println("Log after compaction: " + store.getLog().size() + " entries");

        System.out.println("\n=== Demo Complete ===");
    }
}

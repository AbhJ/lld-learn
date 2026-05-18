/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/LogEntry.java — Immutable log entry with sequence number

import java.util.concurrent.atomic.AtomicLong;

enum LogLevel { DEBUG, INFO, WARN, ERROR }

class LogEntry {
    private static final AtomicLong SEQ_GEN = new AtomicLong(1); // AtomicLong = thread-safe sequence generator; no duplicates

    private final long sequenceNumber;           // final = immutable after construction; safe for threads
    private final LogLevel level;                // final = set once; guaranteed visible to all threads
    private final String threadName;             // final = captures originating thread; immutable
    private final String message;                // final = safe publication; no synchronization needed
    private final long timestamp;                // final = set once in constructor

    public LogEntry(LogLevel level, String message) {
        this.sequenceNumber = SEQ_GEN.getAndIncrement();
        this.level = level;
        this.threadName = Thread.currentThread().getName();
        this.message = message;
        this.timestamp = System.nanoTime();
    }

    public long getSequenceNumber() { return sequenceNumber; }
    public LogLevel getLevel() { return level; }
    public String getThreadName() { return threadName; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }

    public String format() {
        return String.format("[%05d] [%-5s] [%-15s] %s",
                sequenceNumber, level, threadName, message);
    }

    @Override
    public String toString() { return format(); }
}

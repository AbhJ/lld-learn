/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/Logger.java — Thread-safe logger delegating to AsyncAppender

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe Logger that accepts log calls from any thread
 * and delegates to AsyncAppender for serialized output.
 */
class Logger {
    private final String name;                   // final = immutable; safe for threads to read
    private final AsyncAppender appender;        // final = reference never changes; safe publication
    private final LogLevel minLevel;             // final = immutable config; no synchronization needed
    private final AtomicInteger totalLogged = new AtomicInteger(0); // AtomicInteger = thread-safe log counter

    public Logger(String name, AsyncAppender appender, LogLevel minLevel) {
        this.name = name;
        this.appender = appender;
        this.minLevel = minLevel;
    }

    public void log(LogLevel level, String message) {
        if (level.ordinal() >= minLevel.ordinal()) {
            LogEntry entry = new LogEntry(level, message);
            if (appender.append(entry)) {
                totalLogged.incrementAndGet();
            }
        }
    }

    public void debug(String message) { log(LogLevel.DEBUG, message); }
    public void info(String message) { log(LogLevel.INFO, message); }
    public void warn(String message) { log(LogLevel.WARN, message); }
    public void error(String message) { log(LogLevel.ERROR, message); }

    public int getTotalLogged() { return totalLogged.get(); }
    public String getName() { return name; }
}

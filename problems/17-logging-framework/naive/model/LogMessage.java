/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/LogMessage.java — Structured log entry with level, message, timestamp, and source

import java.time.LocalDateTime;

public class LogMessage {
    private LogLevel level;                      // private = only this class can access; encapsulates data
    private String loggerName;
    private String message;
    private LocalDateTime timestamp;
    private String threadName;

    public LogMessage(LogLevel level, String loggerName, String message) {
        this.level = level;
        this.loggerName = loggerName;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.threadName = Thread.currentThread().getName();
    }

    public LogLevel getLevel() { return level; }
    public String getLoggerName() { return loggerName; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getThreadName() { return threadName; }

    @Override
    public String toString() {
        return String.format("[%s] [%s] [%s] %s", timestamp, level, loggerName, message);
    }
}

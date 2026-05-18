/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/Formatter.java — Interchangeable log message formatters (simple, JSON)

import java.time.format.DateTimeFormatter;

public interface Formatter {                      // interface = contract for any log message formatter
    String format(LogMessage message);
    String getName();
}

class SimpleFormatter implements Formatter {     // implements = fulfills Formatter contract
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // static final = one shared immutable instance

    @Override
    public String format(LogMessage message) {
        return String.format("[%s] [%-5s] [%s] %s",
                message.getTimestamp().format(DATE_FMT),
                message.getLevel(),
                message.getLoggerName(),
                message.getMessage());
    }

    @Override
    public String getName() { return "Simple"; }
}

class JSONFormatter implements Formatter {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Override
    public String format(LogMessage message) {
        return String.format("{\"timestamp\":\"%s\",\"level\":\"%s\",\"logger\":\"%s\",\"thread\":\"%s\",\"message\":\"%s\"}",
                message.getTimestamp().format(DATE_FMT),
                message.getLevel(),
                message.getLoggerName(),
                message.getThreadName(),
                escapeJson(message.getMessage()));
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    @Override
    public String getName() { return "JSON"; }
}

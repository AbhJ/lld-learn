/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/Formatter.java — Optimized formatters with StringBuilder pooling for reduced allocation

import java.time.format.DateTimeFormatter;

public interface Formatter {                      // interface = contract for any log message formatter
    String format(LogMessage message);
    String getName();
}

/**
 * Optimized: Uses ThreadLocal StringBuilder to avoid allocation per log message.
 * In high-throughput logging, this reduces GC pressure significantly.
 */
class SimpleFormatter implements Formatter {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ThreadLocal<StringBuilder> BUFFER = ThreadLocal.withInitial(() -> new StringBuilder(256)); // ThreadLocal = each thread gets its own StringBuilder; no contention

    @Override
    public String format(LogMessage message) {
        StringBuilder sb = BUFFER.get();
        sb.setLength(0);
        sb.append('[').append(message.getTimestamp().format(DATE_FMT)).append(']');
        sb.append(" [");
        appendPadded(sb, message.getLevel().name(), 5);
        sb.append("] [").append(message.getLoggerName()).append("] ");
        sb.append(message.getMessage());
        return sb.toString();
    }

    private void appendPadded(StringBuilder sb, String s, int width) {
        sb.append(s);
        for (int i = s.length(); i < width; i++) sb.append(' ');
    }

    @Override
    public String getName() { return "Simple"; }
}

class JSONFormatter implements Formatter {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final ThreadLocal<StringBuilder> BUFFER = ThreadLocal.withInitial(() -> new StringBuilder(512)); // ThreadLocal = per-thread buffer avoids GC pressure

    @Override
    public String format(LogMessage message) {
        StringBuilder sb = BUFFER.get();
        sb.setLength(0);
        sb.append("{\"timestamp\":\"").append(message.getTimestamp().format(DATE_FMT));
        sb.append("\",\"level\":\"").append(message.getLevel());
        sb.append("\",\"logger\":\"").append(message.getLoggerName());
        sb.append("\",\"thread\":\"").append(message.getThreadName());
        sb.append("\",\"message\":\"");
        escapeJson(sb, message.getMessage());
        sb.append("\"}");
        return sb.toString();
    }

    private void escapeJson(StringBuilder sb, String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '"': sb.append("\\\""); break;
                case '\n': sb.append("\\n"); break;
                case '\t': sb.append("\\t"); break;
                default: sb.append(c);
            }
        }
    }

    @Override
    public String getName() { return "JSON"; }
}

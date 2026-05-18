/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/AsyncLogger.java — Non-blocking logger using lock-free ring buffer with async flush

import java.util.*;

/**
 * Optimized: Logger that never blocks the caller.
 * Messages are written to a lock-free ring buffer and flushed asynchronously
 * by a background thread. StringBuilder pooling reduces allocation.
 */
public class AsyncLogger {
    private static final Map<String, AsyncLogger> instances = new HashMap<>(); // static = shared singleton registry

    private String name;
    private LoggerConfig config;
    private RingBuffer ringBuffer;               // RingBuffer = O(1) lock-free queue; avoids blocking callers
    private Thread flushThread;
    private volatile boolean running;            // volatile = flush thread sees changes immediately

    private AsyncLogger(String name, int bufferSize) {
        this.name = name;
        this.config = new LoggerConfig()
                .setLevel(LogLevel.DEBUG)
                .setFormatter(new SimpleFormatter())
                .addAppender(new ConsoleAppender());
        this.ringBuffer = new RingBuffer(bufferSize);
        this.running = true;

        this.flushThread = new Thread(this::flushLoop, "LogFlusher-" + name);
        this.flushThread.setDaemon(true);
        this.flushThread.start();
    }

    public static synchronized AsyncLogger getLogger(String name) {
        return getLogger(name, 1024);
    }

    public static synchronized AsyncLogger getLogger(String name, int bufferSize) {
        return instances.computeIfAbsent(name, n -> new AsyncLogger(n, bufferSize));
    }

    public void setConfig(LoggerConfig config) { this.config = config; }

    /**
     * Non-blocking log: formats and enqueues in O(1).
     * Never blocks the calling thread on I/O.
     */
    public void log(LogLevel level, String message) {
        if (!level.isAtLeast(config.getLevel())) return;

        LogMessage logMessage = new LogMessage(level, name, message);

        if (config.getFilterChain() != null && !config.getFilterChain().filter(logMessage)) {
            return;
        }

        String formatted = config.getFormatter().format(logMessage);
        ringBuffer.offer(formatted);
    }

    public void debug(String message) { log(LogLevel.DEBUG, message); }
    public void info(String message) { log(LogLevel.INFO, message); }
    public void warn(String message) { log(LogLevel.WARN, message); }
    public void error(String message) { log(LogLevel.ERROR, message); }
    public void fatal(String message) { log(LogLevel.FATAL, message); }

    /**
     * Force flush: drain ring buffer and write to all appenders.
     */
    public void flush() {
        String[] batch = new String[64];
        int drained;
        do {
            drained = ringBuffer.drain(batch, batch.length);
            for (int i = 0; i < drained; i++) {
                for (Appender appender : config.getAppenders()) {
                    appender.append(batch[i]);
                }
            }
        } while (drained > 0);
    }

    private void flushLoop() {
        String[] batch = new String[32];
        while (running) {
            int drained = ringBuffer.drain(batch, batch.length);
            if (drained > 0) {
                for (int i = 0; i < drained; i++) {
                    for (Appender appender : config.getAppenders()) {
                        appender.append(batch[i]);
                    }
                }
            } else {
                try { Thread.sleep(1); } catch (InterruptedException e) { break; }
            }
        }
        flush(); // Final flush on shutdown
    }

    public void shutdown() {
        running = false;
        try { flushThread.join(1000); } catch (InterruptedException e) { /* ignore */ }
    }

    public String getName() { return name; }
    public int getPendingCount() { return ringBuffer.size(); }

    public static void resetAll() {
        for (AsyncLogger logger : instances.values()) logger.shutdown();
        instances.clear();
    }
}

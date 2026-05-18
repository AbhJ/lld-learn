/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/AsyncAppender.java — Single writer thread draining BlockingQueue to eliminate file contention

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Async log appender using BlockingQueue and single writer thread.
 *
 * Race condition solved: Multiple threads writing to same output — interleaved/corrupted entries.
 * Solution: All threads put entries into BlockingQueue (thread-safe), single writer thread
 * drains queue and writes sequentially (eliminates output contention).
 */
class AsyncAppender {
    private final BlockingQueue<LogEntry> queue;  // BlockingQueue = thread-safe; writer blocks when empty
    private final ConcurrentLinkedQueue<String> output; // ConcurrentLinkedQueue = lock-free output collection
    private final AtomicBoolean running = new AtomicBoolean(true); // AtomicBoolean = thread-safe shutdown flag
    private final AtomicInteger writtenCount = new AtomicInteger(0); // AtomicInteger = thread-safe counter
    private final Thread writerThread;           // final = single writer thread; eliminates interleaving

    public AsyncAppender(int queueCapacity) {
        this.queue = new LinkedBlockingQueue<>(queueCapacity); // LinkedBlockingQueue = bounded; backpressure if producers too fast
        this.output = new ConcurrentLinkedQueue<>();

        // Single writer thread — eliminates interleaving
        this.writerThread = new Thread(() -> {
            while (running.get() || !queue.isEmpty()) {
                try {
                    LogEntry entry = queue.poll(50, TimeUnit.MILLISECONDS);
                    if (entry != null) {
                        output.offer(entry.format());
                        writtenCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            // Drain remaining entries
            LogEntry entry;
            while ((entry = queue.poll()) != null) {
                output.offer(entry.format());
                writtenCount.incrementAndGet();
            }
        }, "LogWriter");
        this.writerThread.setDaemon(true);
    }

    public void start() {
        writerThread.start();
    }

    /**
     * Submit a log entry to the async queue. Non-blocking for callers.
     */
    public boolean append(LogEntry entry) {
        try {
            return queue.offer(entry, 100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public void shutdown() throws InterruptedException {
        running.set(false);
        writerThread.join(5000);
    }

    public int getWrittenCount() { return writtenCount.get(); }
    public ConcurrentLinkedQueue<String> getOutput() { return output; }
    public int getQueueSize() { return queue.size(); }
}

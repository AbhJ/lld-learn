/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/AsyncAppender.java — Decorator providing non-blocking log writing with buffering

import java.util.*;

public class AsyncAppender implements Appender { // implements = fulfills the Appender interface contract
    private Appender wrappedAppender;            // private = hides the decorated appender (decorator pattern)
    private List<String> queue;
    private int bufferSize;

    public AsyncAppender(Appender appender, int bufferSize) {
        this.wrappedAppender = appender;
        this.bufferSize = bufferSize;
        this.queue = new ArrayList<>();
    }

    @Override
    public void append(String formattedMessage) {
        queue.add(formattedMessage);
        if (queue.size() >= bufferSize) {
            flush();
        }
    }

    public void flush() {
        for (String msg : queue) {
            wrappedAppender.append(msg);
        }
        int flushed = queue.size();
        queue.clear();
        if (flushed > 0) {
            System.out.println("  [AsyncAppender] Flushed " + flushed + " messages to " + wrappedAppender.getName());
        }
    }

    @Override
    public String getName() { return "Async(" + wrappedAppender.getName() + ")"; }

    @Override
    public void close() {
        flush();
        wrappedAppender.close();
    }

    public int getPendingCount() { return queue.size(); }
}

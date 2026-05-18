/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating lock-free ring buffer async logging

import java.util.List;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Logging Framework (Optimized) ===\n");

        // --- Test 1: Async Logger with Ring Buffer ---
        System.out.println("--- Test 1: Async Logger (lock-free ring buffer) ---");
        AsyncLogger appLogger = AsyncLogger.getLogger("App", 256);
        appLogger.debug("Application initializing...");
        appLogger.info("Application started successfully");
        appLogger.warn("Config file not found, using defaults");
        appLogger.error("Failed to connect to cache server");
        Thread.sleep(50); // Let flush thread process
        System.out.println();

        // --- Test 2: JSON Formatter with StringBuilder pooling ---
        System.out.println("--- Test 2: JSON Formatter (StringBuilder pooling) ---");
        AsyncLogger apiLogger = AsyncLogger.getLogger("API", 256);
        LoggerConfig jsonConfig = new LoggerConfig()
                .setLevel(LogLevel.INFO)
                .setFormatter(new JSONFormatter())
                .addAppender(new ConsoleAppender());
        apiLogger.setConfig(jsonConfig);
        apiLogger.info("Request received: GET /api/users");
        apiLogger.error("Internal server error on POST /api/orders");
        apiLogger.debug("This should not appear (below INFO)");
        Thread.sleep(50);
        System.out.println();

        // --- Test 3: Level Filtering ---
        System.out.println("--- Test 3: Level Filtering (WARN+) ---");
        AsyncLogger dbLogger = AsyncLogger.getLogger("Database", 256);
        LoggerConfig warnConfig = new LoggerConfig()
                .setLevel(LogLevel.WARN)
                .setFormatter(new SimpleFormatter())
                .addAppender(new ConsoleAppender());
        dbLogger.setConfig(warnConfig);
        dbLogger.debug("Query executed in 5ms");
        dbLogger.info("Connection pool size: 10");
        dbLogger.warn("Connection pool running low");
        dbLogger.error("Query timeout after 30s");
        dbLogger.fatal("Database connection lost!");
        Thread.sleep(50);
        System.out.println();

        // --- Test 4: File Appender ---
        System.out.println("--- Test 4: File Appender ---");
        FileAppender fileAppender = new FileAppender("application.log");
        AsyncLogger fileLogger = AsyncLogger.getLogger("FileTest", 256);
        fileLogger.setConfig(new LoggerConfig()
                .setLevel(LogLevel.INFO)
                .setFormatter(new SimpleFormatter())
                .addAppender(fileAppender));
        fileLogger.info("This goes to the file");
        fileLogger.error("Error logged to file");
        fileLogger.warn("Warning logged to file");
        fileLogger.flush();
        System.out.println("File appender lines written: " + fileAppender.getLineCount());
        System.out.println();

        // --- Test 5: Ring Buffer Statistics ---
        System.out.println("--- Test 5: Ring Buffer Non-Blocking ---");
        AsyncLogger fastLogger = AsyncLogger.getLogger("Fast", 64);
        for (int i = 0; i < 20; i++) {
            fastLogger.info("Rapid message #" + i);
        }
        System.out.println("Pending in buffer: " + fastLogger.getPendingCount());
        fastLogger.flush();
        System.out.println("After flush: " + fastLogger.getPendingCount());
        System.out.println();

        // --- Cleanup ---
        AsyncLogger.resetAll();
        System.out.println("=== Logging Framework Demo Complete ===");
    }
}

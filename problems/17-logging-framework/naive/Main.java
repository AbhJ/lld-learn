/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating the logging framework

/*
 * VARIATIONS FREQUENTLY ASKED:
 * 1. Distributed tracing - Correlation IDs, span tracking across services
 * 2. Log aggregation - Ship to ELK/Splunk, structured JSON, sampling
 * 3. Audit logging - Immutable, who/what/when, compliance (SOX, GDPR)
 * 4. Performance logging - Method timing, percentiles, slow query detection
 * 5. Dynamic log level - Change level at runtime without restart, per-class level
 *
 * See VARIATIONS.md for full solution approaches.
 */
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Logging Framework Demo ===\n");

        // --- Test 1: Basic Logging with SimpleFormatter ---
        System.out.println("--- Test 1: Basic Logging (Console + SimpleFormatter) ---");
        Logger appLogger = Logger.getLogger("App");
        appLogger.debug("Application initializing...");
        appLogger.info("Application started successfully");
        appLogger.warn("Config file not found, using defaults");
        appLogger.error("Failed to connect to cache server");
        System.out.println();

        // --- Test 2: JSON Formatter ---
        System.out.println("--- Test 2: JSON Formatter ---");
        LoggerConfig jsonConfig = new LoggerConfig()
                .setLevel(LogLevel.INFO)
                .setFormatter(new JSONFormatter())
                .addAppender(new ConsoleAppender());

        Logger apiLogger = Logger.getLogger("API");
        apiLogger.setConfig(jsonConfig);
        apiLogger.info("Request received: GET /api/users");
        apiLogger.error("Internal server error on POST /api/orders");
        apiLogger.debug("This should not appear (below INFO level)");
        System.out.println();

        // --- Test 3: Level Filtering ---
        System.out.println("--- Test 3: Level Filtering (WARN and above only) ---");
        LoggerConfig warnConfig = new LoggerConfig()
                .setLevel(LogLevel.WARN)
                .setFormatter(new SimpleFormatter())
                .addAppender(new ConsoleAppender());

        Logger dbLogger = Logger.getLogger("Database");
        dbLogger.setConfig(warnConfig);
        dbLogger.debug("Query executed in 5ms");
        dbLogger.info("Connection pool size: 10");
        dbLogger.warn("Connection pool running low: 2 remaining");
        dbLogger.error("Query timeout after 30s");
        dbLogger.fatal("Database connection lost!");
        System.out.println();

        // --- Test 4: Chain of Responsibility Filter ---
        System.out.println("--- Test 4: Chain of Responsibility (Filter password messages) ---");
        LogFilter levelFilter = new LevelFilter(LogLevel.DEBUG);
        LogFilter keywordFilter = new KeywordFilter("password", true); // exclude password logs
        levelFilter.setNext(keywordFilter);

        LoggerConfig filteredConfig = new LoggerConfig()
                .setLevel(LogLevel.DEBUG)
                .setFormatter(new SimpleFormatter())
                .addAppender(new ConsoleAppender())
                .setFilterChain(levelFilter);

        Logger authLogger = Logger.getLogger("Auth");
        authLogger.setConfig(filteredConfig);
        authLogger.info("User login attempt: admin");
        authLogger.debug("Checking password for user admin"); // Filtered out!
        authLogger.warn("Invalid credentials for user admin");
        authLogger.info("User admin logged in successfully");
        System.out.println();

        // --- Test 5: File Appender ---
        System.out.println("--- Test 5: File Appender ---");
        FileAppender fileAppender = new FileAppender("application.log");
        LoggerConfig fileConfig = new LoggerConfig()
                .setLevel(LogLevel.INFO)
                .setFormatter(new SimpleFormatter())
                .addAppender(fileAppender);

        Logger fileLogger = Logger.getLogger("FileTest");
        fileLogger.setConfig(fileConfig);
        fileLogger.info("This goes to the file");
        fileLogger.error("Error logged to file");
        fileLogger.warn("Warning logged to file");

        System.out.println("File appender lines written: " + fileAppender.getLineCount());
        System.out.println("File content:");
        for (String line : fileAppender.getBufferedContent()) {
            System.out.println("  " + line);
        }
        System.out.println();

        // --- Test 6: Multiple Appenders ---
        System.out.println("--- Test 6: Multiple Appenders (Console + File) ---");
        FileAppender file2 = new FileAppender("error.log");
        LoggerConfig multiConfig = new LoggerConfig()
                .setLevel(LogLevel.ERROR)
                .setFormatter(new SimpleFormatter())
                .addAppender(new ConsoleAppender())
                .addAppender(file2);

        Logger multiLogger = Logger.getLogger("Multi");
        multiLogger.setConfig(multiConfig);
        multiLogger.info("This won't appear (below ERROR)");
        multiLogger.error("Critical error - written to both console and file");
        System.out.println("Error file lines: " + file2.getLineCount());
        System.out.println();

        // --- Test 7: Async Appender ---
        System.out.println("--- Test 7: Async Appender (buffer size=3) ---");
        FileAppender asyncFile = new FileAppender("async.log");
        AsyncAppender asyncAppender = new AsyncAppender(asyncFile, 3);
        LoggerConfig asyncConfig = new LoggerConfig()
                .setLevel(LogLevel.DEBUG)
                .setFormatter(new SimpleFormatter())
                .addAppender(asyncAppender);

        Logger asyncLogger = Logger.getLogger("Async");
        asyncLogger.setConfig(asyncConfig);
        asyncLogger.info("Message 1 (buffered)");
        System.out.println("Pending: " + asyncAppender.getPendingCount());
        asyncLogger.info("Message 2 (buffered)");
        System.out.println("Pending: " + asyncAppender.getPendingCount());
        asyncLogger.info("Message 3 (triggers flush)");
        System.out.println("File lines after flush: " + asyncFile.getLineCount());
        asyncLogger.info("Message 4 (buffered again)");
        asyncAppender.close(); // Force final flush
        System.out.println("File lines after close: " + asyncFile.getLineCount());
        System.out.println();

        // --- Test 8: Singleton Behavior ---
        System.out.println("--- Test 8: Singleton Verification ---");
        Logger logger1 = Logger.getLogger("App");
        Logger logger2 = Logger.getLogger("App");
        System.out.println("Same instance: " + (logger1 == logger2));
        System.out.println("Logger name: " + logger1.getName());
        System.out.println();

        System.out.println("=== Logging Framework Demo Complete ===");
    }
}

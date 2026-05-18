/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/Logger.java — Central logging facade routing messages through filters and appenders

import java.util.*;

public class Logger {
    private static final Map<String, Logger> instances = new HashMap<>(); // static = shared registry across all code; singleton pattern
    private static LoggerConfig defaultConfig;   // static = one default config for all loggers

    private String name;                         // private = each logger has its own identity
    private LoggerConfig config;

    static {
        defaultConfig = new LoggerConfig()
                .setLevel(LogLevel.DEBUG)
                .setFormatter(new SimpleFormatter())
                .addAppender(new ConsoleAppender());
    }

    private Logger(String name) {                 // private constructor = forces use of getLogger(); singleton pattern
        this.name = name;
        this.config = defaultConfig;
    }

    public static synchronized Logger getLogger(String name) { // synchronized = only one thread can create loggers at a time
        return instances.computeIfAbsent(name, Logger::new);
    }

    public static void setDefaultConfig(LoggerConfig config) {
        defaultConfig = config;
    }

    public void setConfig(LoggerConfig config) {
        this.config = config;
    }

    public void log(LogLevel level, String message) {
        // Check level
        if (!level.isAtLeast(config.getLevel())) return;

        LogMessage logMessage = new LogMessage(level, name, message);

        // Apply filter chain
        if (config.getFilterChain() != null && !config.getFilterChain().filter(logMessage)) {
            return;
        }

        // Format
        String formatted = config.getFormatter().format(logMessage);

        // Append to all appenders
        for (Appender appender : config.getAppenders()) {
            appender.append(formatted);
        }
    }

    public void debug(String message) { log(LogLevel.DEBUG, message); }
    public void info(String message) { log(LogLevel.INFO, message); }
    public void warn(String message) { log(LogLevel.WARN, message); }
    public void error(String message) { log(LogLevel.ERROR, message); }
    public void fatal(String message) { log(LogLevel.FATAL, message); }

    public String getName() { return name; }
    public LoggerConfig getConfig() { return config; }

    public static void resetAll() { instances.clear(); }
}

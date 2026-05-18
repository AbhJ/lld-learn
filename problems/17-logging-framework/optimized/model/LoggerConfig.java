/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/LoggerConfig.java — Configuration object holding logger settings

import java.util.ArrayList;
import java.util.List;

public class LoggerConfig {
    private LogLevel level;
    private Formatter formatter;                 // strategy pattern = pluggable formatting
    private List<Appender> appenders;            // List = multiple output destinations processed in order
    private LogFilter filterChain;               // chain-of-responsibility pattern for filtering

    public LoggerConfig() {
        this.level = LogLevel.DEBUG;
        this.formatter = new SimpleFormatter();
        this.appenders = new ArrayList<>();
        this.filterChain = null;
    }

    public LoggerConfig setLevel(LogLevel level) {
        this.level = level;
        return this;
    }

    public LoggerConfig setFormatter(Formatter formatter) {
        this.formatter = formatter;
        return this;
    }

    public LoggerConfig addAppender(Appender appender) {
        this.appenders.add(appender);
        return this;
    }

    public LoggerConfig setFilterChain(LogFilter filter) {
        this.filterChain = filter;
        return this;
    }

    public LogLevel getLevel() { return level; }
    public Formatter getFormatter() { return formatter; }
    public List<Appender> getAppenders() { return appenders; }
    public LogFilter getFilterChain() { return filterChain; }
}

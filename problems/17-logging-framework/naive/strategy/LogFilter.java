/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/LogFilter.java — Interchangeable log filtering rules (level, keyword, source)

public abstract class LogFilter {                // abstract = can't instantiate directly; must be subclassed
    private LogFilter next;                      // private = chain-of-responsibility link hidden from outside

    public LogFilter setNext(LogFilter next) {
        this.next = next;
        return next;
    }

    public boolean filter(LogMessage message) {
        if (!doFilter(message)) {
            return false;
        }
        if (next != null) {
            return next.filter(message);
        }
        return true;
    }

    protected abstract boolean doFilter(LogMessage message); // abstract = subclass MUST provide its own filter logic
}

class LevelFilter extends LogFilter {            // extends = inherits chain logic from abstract LogFilter
    private LogLevel minLevel;

    public LevelFilter(LogLevel minLevel) {
        this.minLevel = minLevel;
    }

    @Override
    protected boolean doFilter(LogMessage message) {
        return message.getLevel().isAtLeast(minLevel);
    }
}

class LoggerNameFilter extends LogFilter {       // extends = inherits chain logic; adds name-based filtering
    private String allowedPrefix;

    public LoggerNameFilter(String allowedPrefix) {
        this.allowedPrefix = allowedPrefix;
    }

    @Override
    protected boolean doFilter(LogMessage message) {
        return message.getLoggerName().startsWith(allowedPrefix);
    }
}

class KeywordFilter extends LogFilter {          // extends = inherits chain logic; adds keyword-based filtering
    private String keyword;
    private boolean exclude;

    public KeywordFilter(String keyword, boolean exclude) {
        this.keyword = keyword;
        this.exclude = exclude;
    }

    @Override
    protected boolean doFilter(LogMessage message) {
        boolean contains = message.getMessage().contains(keyword);
        return exclude ? !contains : contains;
    }
}

# Logging Framework


## Problem Statement
Design a logging framework similar to Log4j or SLF4J. The framework supports multiple log levels, configurable appenders (console, file), pluggable formatters, log filtering with chain of responsibility, and async logging capabilities.

The logger is a singleton that accepts log messages at various levels. Messages pass through filters (chain of responsibility) that determine whether they should be processed. Approved messages are formatted using the configured formatter strategy and sent to one or more appenders.

The async appender decorator wraps any appender to provide non-blocking log output using a buffer.

## Requirements
### Functional Requirements
- Multiple log levels (DEBUG, INFO, WARN, ERROR, FATAL)
- Console and file appenders
- Configurable formatting (simple text, JSON)
- Level-based filtering
- Async logging support
- Logger configuration management
- Multiple named loggers

### Non-functional Requirements
- Singleton pattern for logger access
- Minimal performance overhead
- Thread-safe logging
- Extensible appenders and formatters

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Singleton | Logger instance | Global access point, single configuration |
| Chain of Responsibility | Log level filtering | Flexible filtering pipeline |
| Strategy | Formatting | Pluggable message formatting |
| Decorator | Async appender | Add async behavior to any appender |

## Folder Structure
```
17-logging-framework/
├── naive/
│   ├── model/      -> LogLevel, LogMessage, LoggerConfig
│   ├── service/    -> Logger, AsyncAppender
│   ├── strategy/   -> Appender (Console, File), Formatter, LogFilter
│   └── Main.java
└── optimized/
    ├── model/
    ├── service/    -> AsyncLogger, RingBuffer (lock-free CAS)
    ├── strategy/   -> Formatter (ThreadLocal StringBuilder pooling)
    └── Main.java
```

### How to Run
```bash
# Run the naive (simple) version:
cd naive
mkdir -p out
javac -d out model/*.java strategy/*.java service/*.java Main.java
java -cp out Main

# Run the optimized version:
cd optimized
mkdir -p out
javac -d out model/*.java strategy/*.java service/*.java Main.java
java -cp out Main
```

### Naive vs Optimized
| Operation | Naive | Optimized |
|-----------|-------|-----------|
| Log write | Blocks caller on I/O | Lock-free ring buffer enqueue O(1) |
| String formatting | New StringBuilder per message | ThreadLocal pooled StringBuilder |
| Async flush | ArrayList buffer + synchronized | CAS-based ring buffer (no locks) |
| Throughput | Limited by appender speed | Decoupled: caller never waits |

---

## Class Diagram (Text)
```
Logger (Singleton)
 ├── LogLevel (Enum)
 ├── LogMessage
 ├── Appender (Interface)
 │    ├── ConsoleAppender
 │    ├── FileAppender
 │    └── AsyncAppender (Decorator)
 ├── Formatter (Interface)
 │    ├── SimpleFormatter
 │    └── JSONFormatter
 ├── LogFilter (Chain)
 └── LoggerConfig
```

## How to Compile and Run
```bash
cd problems/17-logging-framework
mkdir -p out
javac -d out *.java
java -cp out Main
```

## Expected Output
```
=== Logging Framework Demo ===
[2026-05-13 10:30:00] [INFO] [Main] Application started
[2026-05-13 10:30:00] [WARN] [DB] Connection pool running low
[2026-05-13 10:30:00] [ERROR] [Auth] Failed login attempt for user admin
{"timestamp":"...","level":"ERROR","logger":"Auth","message":"..."}
```

## Key Design Decisions
- Singleton ensures consistent logging configuration across the application
- Chain of Responsibility allows flexible filter stacking (by level, by source, etc.)
- Strategy pattern for formatters makes output format easily configurable
- Decorator pattern for async doesn't require modifying existing appenders

## Interview Tips
- Explain why Singleton is appropriate here (global configuration, single point of access)
- Discuss thread safety in the logger and async appender
- Talk about performance: buffering, async flushing
- Mention how real frameworks handle logger hierarchies (package-level configuration)
- Discuss log rotation and file management for FileAppender

---

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** Multiple threads writing to same log file — interleaved/corrupted log entries.

```bash
cd concurrent
mkdir -p out
find . -name '*.java' | xargs javac -d out
java -cp out Main
```

### Key Concurrency Techniques Used
| Technique | Where | Why |
|-----------|-------|-----|
| BlockingQueue | AsyncAppender.queue | Thread-safe buffer between log callers and writer |
| Single writer thread | AsyncAppender | Eliminates file contention — only one thread writes |
| AtomicBoolean | AsyncAppender.running | Clean shutdown signaling without synchronization |
| AtomicLong | LogEntry.sequenceNumber | Globally unique sequence numbers for ordering |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*

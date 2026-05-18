/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/ConnectionPool.java — Optimized: BlockingQueue + Semaphore + background health check
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionPool {
    private PoolConfig config;
    private AtomicInteger connCounter;                           // AtomicInteger = thread-safe ID counter
    private LinkedBlockingQueue<Connection> idleConnections;     // BlockingQueue = thread-safe FIFO; no explicit locking needed
    private Semaphore sizeLimiter;                               // Semaphore = bounds total connections; tryAcquire is non-blocking
    private Set<Connection> activeConnections;                   // ConcurrentHashMap.newKeySet = thread-safe Set
    private volatile boolean shutdown;                           // volatile = all threads see shutdown flag immediately
    private ScheduledExecutorService healthChecker;              // ScheduledExecutor = runs health check on a timer

    public ConnectionPool(PoolConfig config) {
        this.config = config;
        this.connCounter = new AtomicInteger(0);
        this.idleConnections = new LinkedBlockingQueue<>();
        this.sizeLimiter = new Semaphore(config.getMaxSize());
        this.activeConnections = ConcurrentHashMap.newKeySet();
        this.shutdown = false;

        // Pre-create minimum connections
        for (int i = 0; i < config.getMinSize(); i++) {
            Connection conn = createConnection();
            idleConnections.offer(conn);
            sizeLimiter.tryAcquire(); // Reserve permits for pre-created connections
        }

        // WHY: Background health check thread removes stale connections proactively
        this.healthChecker = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "pool-health-check");
            t.setDaemon(true);
            return t;
        });
        healthChecker.scheduleAtFixedRate(this::runHealthCheck, 5, 5, TimeUnit.SECONDS);
    }

    private Connection createConnection() {
        return new Connection("Conn-" + connCounter.incrementAndGet());
    }

    // WHY: No synchronized block — Semaphore + BlockingQueue handle concurrency
    public Connection borrow() {
        if (shutdown) throw new IllegalStateException("Pool is shut down");

        // Try to get an idle connection first (non-blocking)
        Connection conn = idleConnections.poll();
        if (conn != null && conn.isHealthy()) {
            activeConnections.add(conn);
            return conn;
        }

        // Try to create a new one if under limit
        // WHY: tryAcquire with timeout replaces busy-wait loop
        try {
            if (sizeLimiter.tryAcquire(config.getBorrowTimeoutMs(), TimeUnit.MILLISECONDS)) {
                conn = createConnection();
                activeConnections.add(conn);
                return conn;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for connection");
        }

        throw new RuntimeException("Pool exhausted: timeout after " + config.getBorrowTimeoutMs() + "ms");
    }

    public void returnConnection(Connection conn) {
        activeConnections.remove(conn);
        if (!shutdown && conn.isHealthy()) {
            idleConnections.offer(conn);
        } else {
            conn.close();
            sizeLimiter.release(); // Free the permit for a new connection
        }
    }

    // Background health check — removes unhealthy idle connections
    private void runHealthCheck() {
        int checked = 0;
        for (Connection conn : idleConnections) {
            if (!conn.isHealthy()) {
                if (idleConnections.remove(conn)) {
                    conn.close();
                    sizeLimiter.release();
                    checked++;
                }
            }
        }
        if (checked > 0) System.out.println("  [Health] Removed " + checked + " unhealthy connections");
    }

    public void shutdown() {
        shutdown = true;
        healthChecker.shutdown();
        Connection conn;
        while ((conn = idleConnections.poll()) != null) conn.close();
    }

    public PoolStats getStats() {
        return new PoolStats(activeConnections.size(), idleConnections.size(),
            activeConnections.size() + idleConnections.size());
    }
}
